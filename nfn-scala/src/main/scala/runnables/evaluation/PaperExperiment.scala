package runnables.evaluation

import akka.actor.ActorRef
import ccn.packet._
import com.typesafe.config.{Config, ConfigFactory}
import config.StaticConfig
import lambdacalculus.parser.ast._
import monitor.Monitor
import myutil.IOHelper
import nfn._
import nfn.service._
import nfn.service._
import node.LocalNodeFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util._

object PaperExperiment extends App {

  implicit val conf: Config = ConfigFactory.load()

  val expNum = 5

  val node1 = LocalNodeFactory.forId(1)
  val node2 = LocalNodeFactory.forId(2, isCCNOnly = true)

  val node3 = LocalNodeFactory.forId(3)

  val node4 = LocalNodeFactory.forId(4)
  val node5 = LocalNodeFactory.forId(5, isCCNOnly = true)
  val nodes = List(node1, node2, node3, node4, node5)

  val docname1 = node1.localPrefix.append("doc", "test1")
  val docdata1 = "one".getBytes

  val docname2 = node2.localPrefix.append("doc", "test2")
  val docdata2 = "two two".getBytes

  val docname3 = node3.localPrefix.append("doc", "test3")
  val docdata3 = "three three three".getBytes

  val docname4 = node4.localPrefix.append("doc", "test4")
  val docdata4 = "four four four four".getBytes

  val docname5 = node5.localPrefix.append("doc", "test5")
  val docdata5 = "five five five five five".getBytes

  node1 <~> node2
  if(expNum != 3) {
    node1.registerPrefixToNodes(node2, List(node4))
    node2.registerPrefixToNodes(node1, List(node3))
  } else {
    node1.registerPrefixToNodes(node2, List(node3, node4, node5))
  }

  if(expNum != 3) {
    node1 <~> node3
    node1.registerPrefixToNodes(node3, List(node4))
  }

  node2 <~> node4
  node2.registerPrefixToNodes(node4, List(node3, node5))
  node4.registerPrefixToNodes(node2, List(node1))

  node3 <~> node4
  node3.registerPrefixToNodes(node4, List(node2))
  node4.registerPrefixToNodes(node3, List(node1))

  node3 <~> node5
  node5.registerPrefixToNodes(node3, List(node1))

  // remove for exp 6
  if(expNum != 6) {
    node4 <~> node5
    node5.registerPrefixToNodes(node4, List(node2))
  } else {
    node4.registerPrefixToNode(node5, node3)
  }
  node1 += Content(docname1, docdata1)
  node2 += Content(docname2, docdata2)
  node3 += Content(docname3, docdata3)
  node4 += Content(docname4, docdata4)
  node5 += Content(docname5, docdata5)


  // remove for exp6
  if(expNum != 6) {
    node3.publishServiceLocalPrefix(new WordCount())
  }

  node4.publishServiceLocalPrefix(new WordCount())

  val wcPrefix = new WordCount().ccnName

  // remove for exp3
  if(expNum != 3 && expNum != 7) {
    node1.registerPrefix(wcPrefix, node3)
  } else if(expNum != 7) {
    node1.registerPrefix(wcPrefix, node2)
  }

  node2.registerPrefix(wcPrefix, node4)
  if(expNum == 7) {
    node2.registerPrefix(wcPrefix, node1)
  }

  node5.registerPrefix(wcPrefix, node3)

  if(expNum != 6) {
    node5.registerPrefix(wcPrefix, node4)
  } else {

    node3.registerPrefix(wcPrefix, node4)
  }

//  val dynServ = new NFNDynamicService {
//    override def function: (Seq[NFNValue], ActorRef) => NFNValue = { (_, _) =>
//      println("yay")
//      NFNIntValue(42)
//    }
//  }
//  node1.publishService(dynServ)

  Thread.sleep(5000)

  import lambdacalculus.parser.ast.LambdaDSL._
  import nfn.LambdaNFNImplicits._
  implicit val useThunks: Boolean = false

  val ts = new Translate()
  val wc: CCNName = wcPrefix
  val nack = new NackServ()


  val exp1 = wc call ("foo bar", docname1, "foo bar")


  val variable: Expr = LambdaDSL.stringToExpr("a")
  val exp2 = wc call variable

  // cut 1 <-> 3:
  // remove <~>
  // remove prefixes
  // add wc face to node 2
  // remove wc face to node 3
  val exp3 = wc call docname5

  // thunks vs no thunks
  val exp4: Expr = (wc call docname3) + (wc call docname4)

  val exp5_1 = wc call docname3
  val exp5_2 = (wc call docname3) + (wc call docname4)

  // node 3 to ccn only (simluate "overloaded" router)
  // cut 4 <-> 5
  // wc face from 3 to 4
  val exp6 = wc call docname5

  // Adds the wordcountservice to node1 and adds routing from node2 to 1
  val exp7 = (wc call docname4) + (wc call docname3)

  val exp8 = nack.call

  expNum match {
    case 1 => doExp(exp1)
    case 2 => doExp(exp2)
    case 3 => doExp(exp3)
    case 4 => doExp(exp4)
    case 5 => doExp(exp5_1); Thread.sleep(2000); doExp(exp5_2)
    case 6 => doExp(exp6)
    case 7 => doExp(exp7)
    case 8 => doExp(exp8)
    case _ => throw new Exception(s"expNum can only be 1 to 8 and not $expNum")
  }

  def doExp(exprToDo: Expr) = {
    val startTime = System.currentTimeMillis()
    node1 ? exprToDo andThen {
      case Success(content) => {
        val totalTime = System.currentTimeMillis - startTime
        println(s"RESULT($totalTime): $content")
        exit
      }
      case Failure(error) =>
        exit
        throw error
//        Monitor.monitor ! Monitor.Visualize()
    }
  }

  def exit = {
    Monitor.monitor ! Monitor.Visualize()
    nodes foreach { _.shutdown() }
  }
//  Thread.sleep(StaticConfig.defaultTimeoutDuration.toMillis + 100)
}

