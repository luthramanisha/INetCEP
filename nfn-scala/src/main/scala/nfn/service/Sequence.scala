package nfn.service

/**
  * Created by Ali on 20.08.18.
  */

import akka.actor.ActorRef
import nfn.tools.Helpers

import scala.io.Source

//Added for contentfetch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import ccn.packet.CCNName
import config.StaticConfig

import scala.language.postfixOps

//Implementation Guideline: [1] U. Bellur and S. Nimkar, “List of operators for comprehensive complex event processing language framework.”

class Sequence() extends NFNService {

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {

    val sacepicnEnv = StaticConfig.systemPath

    val nodeInfo = interestName.cmps.mkString(" ")
    val nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1)
    val DateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    def determineSequentiality(deliveryFormat: NFNStringValue, sensor1: NFNStringValue, sensor2: NFNStringValue, sensor1EventCount: NFNIntValue, sensor2EventCount: NFNIntValue): NFNValue = {
      LogMessage(nodeName, s"Sequence OP Started\n");

      val sequentiality = handleSequence(deliveryFormat.str, sensor1.str, sensor2.str, sensor1EventCount.i, sensor2EventCount.i)
      return sequentiality
    }

    //handleSequence: Will return a true of false (as string data type)
    def handleSequence(deliveryFormat: String, sensor1: String, sensor2: String, sensor1EventCount: Int, sensor2EventCount: Int): NFNValue = {
      val sensor1Source = Source.fromFile(s"$sacepicnEnv/sensors/" + sensor1)
      val sensor2Source = Source.fromFile(s"$sacepicnEnv/sensors/" + sensor2)
      var output: Boolean = true

      var sensor1Events = sensor1Source.getLines.zipWithIndex.filter(item => item._2 < sensor1EventCount).toList
      var sensor2Events = sensor2Source.getLines.zipWithIndex.filter(item => item._2 < sensor2EventCount).toList

      //Now, for each sensor1 event, check whether sensor2 events are later. Basically, sensor1 should be earlier than sensor2 (as per Sequence OP semantics)

      //Doing a cartesian product of the two sensor events
      def tsCrossProduct(x: List[(String, Int)], y: List[(String, Int)]) = {
        for (
          ev1 <- x;
          ev2 <- y
        ) yield (ev1, ev2)
      }

      //val tsCollection = sensor1Events flatMap {x => sensor2Events map {y => (x,y)}}

      val tsCollection = tsCrossProduct(sensor1Events, sensor2Events)

      tsCollection.foreach {
        case (s1, s2) => {
          var tsS1 = LocalTime.parse(s1._1.split("/")(0).toString.stripPrefix("(").stripSuffix(")").trim, DateTimeFormat)
          var tsS2 = LocalTime.parse(s2._1.split("/")(0).toString.stripPrefix("(").stripSuffix(")").trim, DateTimeFormat)
          //Check data using the log message below:
          //LogMessage(nodeName, s"Comparing => Sensor: 1 - Line: ${s1._2 + 1} | TimeStamp: ${tsS1} AND Sensor: 2 - Line: ${s2._2 + 1} | TimeStamp: ${tsS2} \n");
          if (tsS1.isAfter(tsS2)) {
            LogMessage(nodeName, s"Found a S2 TS (${tsS2}) that is earlier than S1 TS (${tsS1})\n");
            output = false
          }
        }
      }

      sensor1Source.close
      sensor2Source.close

      //Determine the return type:
      //If return type = data -> send the actual data back.
      //If return type = name -> send the named interest back. Which can be used by other operators to get the cached content.
      if (deliveryFormat.toLowerCase == "data") {
        LogMessage(nodeName, s"SEQUENCE OP Completed\n")
        return NFNStringValue(output.toString)
      }
      else if (deliveryFormat.toLowerCase == "name") {
        LogMessage(nodeName, s"SEQUENCE OP Completed\n")
        return NFNStringValue(Helpers.storeOutput(nodeName, output.toString, "SEQUENCE", "onOperator", ccnApi))
      }
      LogMessage(nodeName, s"SEQUENCE OP Completed\n")
      //Default return = false
      return NFNStringValue("false")
    }

    args match {
      //$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 10 "" "call 6 /node/nodeA/nfn_service_Sequence 'data' 'victims' 'survivors' 3 3 /NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3
      case Seq(timestamp: NFNStringValue, deliveryFormat: NFNStringValue, sensor1: NFNStringValue, sensor2: NFNStringValue, sensor1EventCount: NFNIntValue, sensor2EventCount: NFNIntValue) => determineSequentiality(deliveryFormat, sensor1, sensor2, sensor1EventCount, sensor2EventCount)
      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
    }

  }
}

