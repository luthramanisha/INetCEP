package nfn

import ccn.packet.Content
import com.typesafe.config.{Config, ConfigFactory}
import config.StaticConfig
import lambdacalculus.parser.ast.Call
import lambdacalculus.parser.ast.LambdaDSL.{stringToExpr, _}
import nfn.service._
import node.LocalNodeFactory
import org.scalatest.{BeforeAndAfterAll, SequentialNestedSuiteExecution}
import nfn.LambdaNFNImplicits._
import org.scalatest.time.{Millis, Seconds, Span}
import scala.sys.process._
import org.junit.Test

import scala.concurrent.Future

class SingleNodeWindowTest extends ExpressionTester
  with SequentialNestedSuiteExecution
  with BeforeAndAfterAll {

  def createAndExecCCNQuery(nodeName: String, query: String, port: String, IP: String): String = {
    //var cmd:String = getValueOrDefault("CCN.NFNPeek", "echo No Result!")

    var cmd = """$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u #IP#/#PORT# -w 30 "" "#QUERY#/NFN""""
    var cmdPacketFormatter = "$CCNL_HOME/bin/ccn-lite-pktdump -f 2"
    //Replace IP PORT and QUERY
    //With this we can run the remote queries on the remote nodes:
    cmd = cmd.replace("#IP#", s"${IP}").replace("#PORT#", s"${port}").replace("#QUERY#", query);
    LogMessage(nodeName, s"Query sent to network node: ${cmd} | ${cmdPacketFormatter}");
    var result = execcmd(cmd, cmdPacketFormatter)
    LogMessage(nodeName, s"Query Result from network node: ${result}");

    if (result.contains("timeout") || result.contains("interest") || result == "")
      result = "No Result!"
    return result.trim().stripLineEnd;
  }

  def execcmd(cmd1: String, cmd2: String): String = {
    val result = Seq("/bin/sh", "-c", s"${cmd1} | ${cmd2}").!!
    return result
  }

  @Test
  def singleWindowQueryTest: Unit ={
    implicit val conf: Config = ConfigFactory.load()
    val node1 = LocalNodeFactory.forId(1001)


    var sensor = NFNStringValue("plug0")
    val docname1 = node1.localPrefix.append("doc", "test1")

    val wc = new Window()

    node1.publishService(wc)
    val exp1 = wc call List(stringToExpr("plug0"), stringToExpr("16:22:00.000"), stringToExpr("16:27:00.000"))
    val exp2 = Call(node1.localPrefix + wc,List(stringToExpr("plug0"), stringToExpr("16:22:00.000"), stringToExpr("16:27:00.000")))
    val exp3 = Call(node1.localPrefix.toString + wc.toString,List(stringToExpr("plug0"), stringToExpr("16:22:00.000"), stringToExpr("16:27:00.000")))
    val res1 = "4"

    implicit val useThunks = false
    implicit val nodeToSendInterestsTo = node1
    implicit val patientConfig = PatienceConfig(Span(StaticConfig.defaultTimeoutDuration.toMillis, Millis), Span(2000, Millis))
    //doExp(exp1, testExpected(res1))
    val f: Future[Content] = node1 ? exp1
    /*whenReady(f){content =>
      System.out.println("AAAAAAAAAAAAAAAAAAH" + content.toString)
    }

    val g: Future[Content] = node1 ? exp2
    whenReady(g){content =>
      System.out.println("AAAAAAAAAAAAAAAAAAH" + content.toString)
    }

    val h: Future[Content] = node1 ? exp3
    whenReady(h){content =>
      System.out.println("AAAAAAAAAAAAAAAAAAH" + content.toString)
    }*/

    node1.shutdown()
  }


}
