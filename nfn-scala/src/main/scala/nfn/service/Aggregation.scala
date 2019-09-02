package nfn.service

/**
  * Created by Ali on 24.08.18.
  */

import akka.actor.ActorRef
import nfn.tools.Helpers

import scala.io.Source

//Added for contentfetch
import java.time.format.DateTimeFormatter

import ccn.packet.CCNName
import config.StaticConfig

import scala.language.postfixOps

class Aggregation() extends NFNService {
  val sacepicnEnv = StaticConfig.systemPath

  //handleAggregation: Will return the aggregation result.
  def handleAggregation(nodeName : String, deliveryFormat: String, sensor: String, numberOfEvents: Int, aggregateOperator: String, column: Int, ccnApi:ActorRef): NFNValue = {
    val sensorSource = Source.fromFile(s"$sacepicnEnv/sensors/" + sensor)
    var output = ""

    var sensorEvents = sensorSource.getLines.zipWithIndex.filter(item => item._2 < numberOfEvents).toList

    //Now, for each sensor event, apply aggregation on the column and return result

    if (sensorEvents.nonEmpty) {
      aggregateOperator match {
        case "COUNT" => output = sensorEvents.length.toString
        case "SUM" => output = sum(getColumnDataAsIntList(sensorEvents, column)).toString
        case "MIN" => output = min(getColumnDataAsIntList(sensorEvents, column)).toString
        case "MAX" => output = max(getColumnDataAsIntList(sensorEvents, column)).toString
        case "AVERAGE" => output = average(getColumnDataAsIntList(sensorEvents, column)).toString
        case _ =>
      }
    }

    sensorSource.close

    //Determine the return type:
    //If return type = data -> send the actual data back.
    //If return type = name -> send the named interest back. Which can be used by other operators to get the cached content.
    if (deliveryFormat.toLowerCase == "name") {
      output = Helpers.storeOutput(nodeName, output, "AGGREGATION", "onSensor", ccnApi)
    }
    else {
      LogMessage(nodeName, s"Inside Aggregation -> Aggregation name: NONE, Aggregation content: $output")
    }
    LogMessage(nodeName, s"AGGREGATION OP Completed\n");
    //Default return = false
    return NFNStringValue(output)
  }

  def getColumnDataAsIntList(sensorEvents: List[(String, Int)], column: Int): List[Int] = {
    for (x <- sensorEvents) yield x._1.split("/")(column - 1).stripPrefix("(").stripSuffix(")").trim.toInt
  }

  def sum(xs: List[Int]): Int = {
    xs match {
      case x :: tail => x + sum(tail) // if there is an element, add it to the sum of the tail
      case Nil => 0 // if there are no elements, then the sum is 0
    }
  }

  def min(list: List[Int]): Int = {
    list match {
      case Nil => 0;
      case xs => xs.min;
    }
  }

  def max(list: List[Int]): Int = {
    list match {
      case Nil => 0;
      case xs => xs.max;
    }
  }

  def average(list: List[Int]): Double = {
    list match {
      case Nil => 0;
      case xs => xs.foldLeft(0.0)(_ + _) / xs.length;
    }
  }

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {



    val nodeInfo = interestName.cmps.mkString(" ")
    val nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1)
    val DateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    def aggregateEventData(deliveryFormat: NFNStringValue, sensor: NFNStringValue, numberOfEvents: NFNIntValue, aggregateOperator: NFNStringValue, column: NFNIntValue): NFNValue = {
      LogMessage(nodeName, s"Aggregation OP Started\n");

      val aggregation = handleAggregation(nodeName, deliveryFormat.str, sensor.str, numberOfEvents.i, aggregateOperator.str, column.i,ccnApi)
      return aggregation
    }

    //def aggregateEventData(inputFormat: NFNStringValue, outputFormat: NFNStringValue,)

    //Aggregation - SUM, MIN, MAX, COUNT, AVERAGE
    args match {
      case Seq(timestamp: NFNStringValue, inputFormat: NFNStringValue, outputFormat: NFNStringValue, stream: NFNStringValue, numberOfEvents: NFNIntValue, aggregateOperator: NFNStringValue, column: NFNIntValue) => aggregateEventData(outputFormat, stream, numberOfEvents, aggregateOperator, column)
      //case Seq(timestamp: NFNStringValue, inputFormat: NFNStringValue, outputFormat: NFNStringValue, stream: NFNStringValue, aggregationOperator: NFNStringValue, column: NFNIntValue) => aggregateEventData(inputFormat, outputFormat, stream, aggregationOperator, column)
      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
    }

  }
}
