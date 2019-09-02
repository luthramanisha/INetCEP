package orgOpenmhealth.computeServer


import ccn.packet.{Content, CCNName}
import com.typesafe.scalalogging.slf4j.Logging
import config.{ComputeNodeConfig, RouterConfig, StaticConfig}
import nfn.service.GPS.GPX.GPXOriginFilter
import nfn.service.GPS.GPX.GPXDistanceAggregator
import nfn.service.GPS.GPX.GPXDistanceComputer
import nfn.service.Temperature.{ReadSensorData, ReadSensorDataSimu, StoreSensorData}
import nfn.service._
import node.LocalNode
import orgOpenmhealth.services.PointCount
import scopt.OptionParser
import sys.process._

import scala.io.Source


object ComputeServerConfigDefaultsHackaton {
  val mgmtSocket = "/tmp/ccn-lite-mgmt.sock"
  val ccnLiteAddr = "127.0.0.1"
  val ccnlPort = 9000
  val computeServerPort = 9001
  val isCCNLiteAlreadyRunning = false
  val logLevel = "warning"
  val prefix = CCNName("nfn", "node")
}

case class ComputeServerConfigHackaton(prefix: CCNName = ComputeServerConfigDefaultsHackaton.prefix,
                               mgmtSocket: Option[String] = None,
                               ccnLiteAddr: String = ComputeServerConfigDefaultsHackaton.ccnLiteAddr,
                               ccnlPort: Int = ComputeServerConfigDefaultsHackaton.ccnlPort,
                               computeServerPort: Int = ComputeServerConfigDefaultsHackaton.computeServerPort,
                               isCCNLiteAlreadyRunning: Boolean = ComputeServerConfigDefaultsHackaton.isCCNLiteAlreadyRunning,
                               logLevel: String = ComputeServerConfigDefaultsHackaton.logLevel,
                               suite: String = "")


object ComputeServerStarterHackaton extends Logging {

  val argsParser =  new OptionParser[ComputeServerConfigHackaton]("") {
    override def showUsageOnError = true

    head("nfn-scala: compute-server starter", "v0.2.0")
    opt[String]('m', "mgmtsocket") action { (ms, c) =>
      c.copy(mgmtSocket = Some(ms))
    } text s"unix socket name for ccnl mgmt ops or of running ccnl, if not specified ccnl UDP socket is used (example: ${ComputeServerConfigDefaultsHackaton.mgmtSocket})"
    opt[String]('a', "ccnl-addr") action { case (a, c) =>
      c.copy(ccnLiteAddr = a)
    } text s"address ccnl should use or address of running ccnl (default: ${ComputeServerConfigDefaultsHackaton.ccnLiteAddr})"
    opt[Int]('o', "ccnl-port") action { case (p, c) =>
      c.copy(ccnlPort = p)
    } text s"unused port ccnl should use or port of running ccnl (default: ${ComputeServerConfigDefaultsHackaton.ccnlPort})"
    opt[String]('s', "suite") action { case (s, c) =>
      c.copy(suite = s)
    } text s"wireformat to be used (default: ndntlv)"
    opt[Int]('p', "cs-port") action { case (p, c) =>
      c.copy(computeServerPort = p)
    } text s"port used by compute server, (default: ${ComputeServerConfigDefaultsHackaton.computeServerPort})"
    opt[Unit]('r', "ccnl-already-running") action { (_, c) =>
      c.copy(isCCNLiteAlreadyRunning = true)
    } text s"flag to indicate that ccnl is already running and should not be started internally by nfn-scala"
    opt[Unit]('v', "verbose") action { (_, c) =>
      c.copy(logLevel = "info")
    } text "loglevel 'info'"
    opt[Unit]('d', "debug") action { (_, c) =>
      c.copy(logLevel = "debug")
    } text "loglevel 'debug'"
    opt[Unit]('h', "help") action { (_, c) =>
      showUsage
      sys.exit
      c
    } text { "prints usage" }
    arg[String]("<node-prefix>") validate {
      p => if(CCNName.fromString(p).isDefined) success else failure(s"Argument <node-prefix> must be a valid CCNName (e.g. ${ComputeServerConfigDefaultsHackaton.prefix})")
    } action {
      case (p, c) => c.copy(prefix = CCNName.fromString(p).get)
    } text s"prefix of this node, all content and services are published with this name (example: ${ComputeServerConfigDefaultsHackaton.prefix})"
  }

  def main(args: Array[String]) = {
    argsParser.parse(args, ComputeServerConfigHackaton()) match {
      case Some(config) =>
        StaticConfig.setDebugLevel(config.logLevel)

        println("Suite is", config.suite)
        if(config.suite != ""){
          StaticConfig.setWireFormat(config.suite)
        }

        logger.debug(s"config: $config")

        // Configuration of the router, sro far always ccn-lite
        // It requires the socket to the management interface, isCCNOnly = false indicates that it is a NFN node
        // and isAlreadyRunning tells the system that it should not have to start ccn-lite
        val routerConfig = RouterConfig(config.ccnLiteAddr, config.ccnlPort, config.prefix, config.mgmtSocket.getOrElse("") ,isCCNOnly = false, isAlreadyRunning = config.isCCNLiteAlreadyRunning)


        // This configuration sets up the compute server
        // withLocalAm = false tells the system that it should not start an abstract machine alongside the compute server
        val computeNodeConfig = ComputeNodeConfig("127.0.0.1", config.computeServerPort, config.prefix, withLocalAM = false)

        // Abstraction of a node which runs both the router and the compute server on localhost (over UDP sockets)
        val node = LocalNode(routerConfig, Some(computeNodeConfig))

        // Publish services
        // This will internally get the Java bytecode for the compiled services, put them into jar files and
        // put the data of the jar into a content object.
        // The name of this service is infered from the package structure of the service as well as the prefix of the local node.
        // In this case the prefix is given with the commandline argument 'prefixStr' (e.g. /node/nodeA/nfn_service_WordCount)
        node.publishServiceLocalPrefix(new PointCount())


      case None => sys.exit(1)
    }
  }
}
