package runnables.evaluation.usecase.cdn

import akka.actor.ActorRef
import ccn.packet._
import com.typesafe.config.ConfigFactory
import config.{ComputeNodeConfig, RouterConfig, AkkaConfig}
import lambdacalculus.parser.ast._
import monitor.Monitor
import nfn._
import nfn.service._
import node.{LocalNodeFactory, LocalNode}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util._

class ESIInclude() extends NFNService {

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {
    args match {
      case Seq(xmlDoc: NFNContentObjectValue, tagToReplace: NFNContentObjectValue, contentToReplaceTagWith: NFNContentObjectValue) => {
        val doc = new String(xmlDoc.data)
        val tag = new String(tagToReplace.data)
        val replaceWith = new String(contentToReplaceTagWith.data)
        val res = doc.replaceAllLiterally(tag, replaceWith)
        NFNStringValue(res)
      }
      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName requires to arguments of type: name of webpage, name of tag to replace, name of content to replace tag with and not $args")
    }
  }
}

class RandomAd() extends NFNService {

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {
    args match {
      case Seq() => {
        val randomAd = s"""<div class="ad">randomly chosen ad at: ${System.currentTimeMillis}</div>"""
        NFNStringValue(randomAd)
      }
      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName requires no arguments and not $args")
    }
  }
}

object CDN extends App {

  implicit val config = ConfigFactory.load()

  val node1Prefix = CCNName("node", "node2")
  val node1 = LocalNode(
    RouterConfig("127.0.0.1", 10020, node1Prefix, LocalNodeFactory.defaultMgmtSockNameForPrefix(node1Prefix)),
    Some(ComputeNodeConfig("127.0.0.1", 10021, node1Prefix))
  )
  val node2Prefix = CCNName("node", "node2")
  val node2 = LocalNode(
    RouterConfig("127.0.0.1", 10020, node2Prefix, LocalNodeFactory.defaultMgmtSockNameForPrefix(node2Prefix)),
    Some(ComputeNodeConfig("127.0.0.1", 10021, node2Prefix))
  )

  val esiTagname = node1Prefix.append("esi", "tag", "include", "randomad")
  val esiTag = "<esi:include:randomad/>"
  val esiTagData = esiTag.getBytes
  val esiTagContent = Content(esiTagname, esiTagData)

  val webpagename = node1Prefix.append("webpage", "test1")
  val webpagedata =
    s"""
      |<html>
      | <body>
      |   <div>
      |     <h1>html page</h1>
      |     <p>random content</p>
      |     $esiTag
      |   </div>
      | </body>
      |</html>
    """.stripMargin.getBytes
  val webpageContent = Content(webpagename, webpagedata)



  node1 <~> node2

  node1 += webpageContent
  node1 += esiTagContent

  node1.publishServiceLocalPrefix(new ESIInclude())

  node1.publishServiceLocalPrefix(new RandomAd())
  node2.publishServiceLocalPrefix(new RandomAd())

  import lambdacalculus.parser.ast.LambdaDSL._
  import nfn.LambdaNFNImplicits._
  implicit val useThunks = false

  val esiInclude = new ESIInclude()
  val randomAd = new RandomAd()

  val exIncludeAd: Expr = esiInclude call (webpagename, esiTagname, randomAd call() )

  val expr = exIncludeAd

  var startTime = System.currentTimeMillis()
  (node1 ? expr) andThen {
    case Success(content) => {
      val totalTime = System.currentTimeMillis - startTime
      println(s"Res($totalTime): ${new String(content.data)}")
    }
    case Failure(error) => throw error
  } andThen { case _ =>
    Monitor.monitor ! Monitor.Visualize()
    node1.shutdown()
    node2.shutdown()
  }


}

