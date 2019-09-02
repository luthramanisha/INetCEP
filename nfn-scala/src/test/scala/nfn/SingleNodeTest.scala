package nfn

import ccn.packet.Content
import com.typesafe.config.{Config, ConfigFactory}
import config.StaticConfig
import nfn.service.WordCount
import node.LocalNodeFactory
import org.scalatest.{BeforeAndAfterAll, SequentialNestedSuiteExecution}
import nfn.LambdaNFNImplicits._
import org.scalatest.time.{Millis, Span}

import scala.concurrent.Future

class SingleNodeTest extends ExpressionTester
  with SequentialNestedSuiteExecution
  with BeforeAndAfterAll {

  implicit val conf: Config = ConfigFactory.load()
  val node1 = LocalNodeFactory.forId(1001)

  val docdata1 = "one one one one".getBytes
  val docname1 = node1.localPrefix.append("doc", "test1")
  val content1 = Content(docname1, docdata1)

  node1 += content1

  node1.publishService(new WordCount())


  val wc = new WordCount()

  val exp1 = wc call docname1

  val res1 = "4"

  implicit val nodeToSendInterestsTo = node1
  implicit val timeOutConfig = PatienceConfig(Span(StaticConfig.defaultTimeoutDuration.toMillis, Millis), Span(2000, Millis))
  implicit val useThunks: Boolean = false
  doExp(exp1, testExpected(res1))
  val f: Future[Content] = node1 ? exp1
  whenReady(f){content =>
    System.out.println("AAAAAAAAAAAAAAAAAAH" + content.toString)
  }

  node1.shutdown()
}
