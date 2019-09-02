package runnables.evaluation

import java.io.File

import ccn.packet.{Content, Interest}
import com.typesafe.config.{ConfigFactory, Config}
import lambdacalculus.parser.ast.Expr
import monitor.Monitor
import nfn.service.{WordCount, PandocTestDocuments, Pandoc}
import node.LocalNodeFactory
import concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

object PandocApp extends App {

  implicit val conf: Config = ConfigFactory.load()

  val node1 = LocalNodeFactory.forId(1)
//  val node2 = LocalNodeFactory.forId(2)
//  val nodes = List(node1, node2)
  val nodes = List(node1)
//  node1 <~> node2

  val pandocServ = new Pandoc()
  val wcServ = new WordCount()

  val pandoc = node1.localPrefix.append(pandocServ.ccnName)
  val wc = node1.localPrefix.append(wcServ.ccnName)

  val tutorialContent = PandocTestDocuments.tutorialMd(node1.localPrefix)
  val tinyContent = PandocTestDocuments.tinyMd(node1.localPrefix)
  node1.publishServiceLocalPrefix(pandocServ)
  node1.publishServiceLocalPrefix(wcServ)
  node1 += tutorialContent
  node1 += tinyContent

//  node2.addPrefixFace(pandocServ.ccnName, node1)

  Thread.sleep(1000)
  import lambdacalculus.parser.ast.LambdaDSL._
  import nfn.LambdaNFNImplicits._
  implicit val useThunks: Boolean = false

  val x = 'x @: 'x + 1 ! 2
  val exprTut: Expr = pandoc call(tutorialContent.name, "markdown_github", "html")
  val exprTiny: Expr = pandoc call(tinyContent.name, "markdown_github", "html")
  val exprWc: Expr = wc call (tinyContent.name)

  sendAndPrintForName(exprTut)

  //Thread.sleep(2000)

  //sendAndPrintForName(exprTut)



  def sendAndPrintForName(interest: Interest) = {
    val startTime = System.currentTimeMillis
    println(s"Sending request: $interest")
    node1 ? interest onComplete {
      case Success(resultContent) => {
        val runTime = System.currentTimeMillis - startTime
        println(s"RESULT:\n${new String(resultContent.data)}\nTIME: (${runTime}ms)")
        Monitor.monitor ! Monitor.Visualize()
        nodes foreach { _.shutdown() }
      }
      case Failure(e) => println(s"Could not receive content for $interest")
        Monitor.monitor ! Monitor.Visualize()
        nodes foreach { _.shutdown() }
    }

  }
}
