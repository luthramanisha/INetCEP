package nfn.service

/**
  * Created by Ali on 06.02.18.
  */

import java.io.FileNotFoundException

import akka.actor.ActorRef
import nfn.tools.Helpers

import scala.io.{BufferedSource, Source}
//Added for contentfetch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import ccn.packet._
import config.StaticConfig
import myutil.FormattedOutput

import scala.language.postfixOps

class Window() extends NFNService {

  val sacepicnEnv = StaticConfig.systemPath
  val DateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
  var relativeTime: LocalTime = null

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {


    //Interest will always be in the form of: call X /node/nodeX/nfn_service_X
    //Using this we can extract the node for this operation.
    val nodeInfo = interestName.cmps.mkString(" ")
    val nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1)


    def processBoundWindow(deliveryFormat: NFNStringValue, sensor: NFNStringValue, lowerBound: NFNStringValue, upperBound: NFNStringValue): NFNValue = {
      LogMessage(nodeName, s"Window OP Started\n")

      //Working for timestamp
      val output = readBoundSensor(sensor.str, LocalTime.parse(lowerBound.str, DateTimeFormat), LocalTime.parse(upperBound.str, DateTimeFormat), nodeName)
      var contentWindow = NFNStringValue("No Results!")
      if (deliveryFormat.str.toLowerCase == "data") {
        return NFNStringValue(output)
      }
      else if (deliveryFormat.str.toLowerCase == "name") {
        val name = Helpers.storeOutput(nodeName, output, "Window", sensor.str, ccnApi)
        LogMessage(nodeName, s"Inside Window -> WINDOW name: ${name}, WINDOW content: ${output}")
        LogMessage(nodeName, s"Window OP Completed\n")
        contentWindow = NFNStringValue(name.toString)
      }
      contentWindow
    }

    def processTimeBoundWindow(deliveryFormat: NFNStringValue, sensor: NFNStringValue, timePeriod: NFNStringValue, timeUnit: NFNStringValue): NFNValue = {
      LogMessage(nodeName, s"Timed Window OP Started\n");

      val output = readRelativeTimedSensor(sensor.str, timePeriod.str.toLong, timeUnit.str, nodeName)
      var contentWindow = NFNStringValue("No Results!")
      if (deliveryFormat.str.toLowerCase == "data") {
        return NFNStringValue(output)
      }
      else if (deliveryFormat.str.toLowerCase == "name") {
        val name = Helpers.storeOutput(nodeName, output, "Window", sensor.str, ccnApi)
        LogMessage(nodeName, s"Inside Window -> WINDOW name: ${name}, WINDOW content: ${output}")
        LogMessage(nodeName, s"Window OP Completed\n")
        contentWindow = NFNStringValue(name.toString)
      }
      contentWindow
    }

    def processEventBoundWindow(deliveryFormat: NFNStringValue, sensor: NFNStringValue, numberOfEvents: NFNStringValue): NFNValue = {
      LogMessage(nodeName, s"Event limited Window OP Started\n");

      val output = readEventCountSensor(deliveryFormat.str, sensor.str, numberOfEvents.str.toInt, nodeName)
      var contentWindow = NFNStringValue("No Results!")
      if (deliveryFormat.str.toLowerCase == "data") {
        return NFNStringValue(output)
      }
      else if (deliveryFormat.str.toLowerCase == "name") {
        val name = Helpers.storeOutput(nodeName, output, "Window", sensor.str, ccnApi)
        LogMessage(nodeName, s"Inside Window -> WINDOW name: ${name}, WINDOW content: ${output}")
        LogMessage(nodeName, s"Window OP Completed\n")
        contentWindow = NFNStringValue(name.toString)
      }
      contentWindow
    }
    //NFNValue(
    args match {
      //Sample Queries with signatures:
      //$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 10 "" "call 5 /node/nodeA/nfn_service_Window 'data' 'victims' '22:18:38.841' '22:18:41.841'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3
      //04.11.2018: Commenting this window (bounded window) because this conflicts with the filter handler which always passes string based parameters in window sub-queries. Possibly, this can be removed later on when the new handler is implemented. (TODO Manisha)
      //case Seq(timestamp: NFNStringValue, outputFormat: NFNStringValue, sensor: NFNStringValue, lowerBound: NFNStringValue, upperBound: NFNStringValue) => processBoundWindow(outputFormat, sensor, lowerBound, upperBound)

      //$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 10 "" "call 5 /node/nodeA/nfn_service_Window 'data' 'victims' '5' 'M'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3
      //02.02.2018: Commenting this window because it conflicts with the bound window
      case Seq(timestamp: NFNStringValue, deliveryFormat: NFNStringValue, sensor: NFNStringValue, timerPeriod: NFNStringValue, timeUnit: NFNStringValue) => processTimeBoundWindow(deliveryFormat, sensor, timerPeriod, timeUnit)

      //$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 10 "" "call 4 /node/nodeA/nfn_service_Window 'data' 'victims' '3' /NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3
      case Seq(timestamp: NFNStringValue, deliveryFormat: NFNStringValue, sensor: NFNStringValue, numberOfEvents: NFNStringValue) => processEventBoundWindow(deliveryFormat, sensor, numberOfEvents)


      case _ =>
        throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
    }
    //)
  }

  //Return number of specified events from the top of the event stream
  def readEventCountSensor(outputFormat: String, path: String, numberOfEvents: Int, nodeName: String): String = {
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + path)

    val sb = new StringBuilder

    bufferedSource.getLines().zipWithIndex.foreach {
      case (line, index) => {
        if (index < numberOfEvents) //Index is the zero'th index, so we only do <, not <=. This will get the top x events from the sensor where x is the number passed to the op.
          sb.append( line + "\n")
      }
    }

    bufferedSource.close

    var output = sb.toString()
    if (output != "")
      output = output.stripSuffix("\n").stripMargin('#')
    else
      output += "No Results!"

    output
  }

  def readRelativeTimedSensor(path: String, timePeriod: Long, timeUnit: String, nodeName: String): String = {
    var bufferedSource: BufferedSource = null
    try {
      bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + path)
    }
    catch {
      case fileNotFoundException: FileNotFoundException => handleFileNotFoundException(fileNotFoundException, nodeName)
      case _: Throwable => LogMessage(nodeName, "Got some Kind of Exception")
    }
    val sb: StringBuilder = new StringBuilder
    var output: String = ""
    val lineList = bufferedSource.getLines().toList
    val delimiter: String = Helpers.getDelimiterFromLine(lineList.head)
    val datePosition = Helpers.getDatePosition(delimiter)

    if (relativeTime == null) {
      relativeTime = Helpers.parseTime(lineList.head.split(delimiter)(datePosition), delimiter)
    }
    val futureTime = FormattedOutput.getFutureTime(relativeTime, timePeriod, timeUnit)
    //LogMessage(nodeName, s"Read Sensor from Current Time: ${relativeTime.toString}")
    //LogMessage(nodeName, s"Unitl Future Time: ${futureTime.toString}")
    for (line <- lineList) {
      val timeStamp = Helpers.parseTime(line.split(delimiter)(datePosition), delimiter)
      if ((relativeTime.isBefore(timeStamp) || relativeTime.equals(timeStamp)) && futureTime.isAfter(timeStamp)) {
        sb.append(line + "\n")
      }
    }

    bufferedSource.close()
    relativeTime = futureTime
    output = sb.toString()
    if (output != "")
      output = output.stripSuffix("\n").stripMargin('#')
    else
      output += "No Results!"
    output
  }

  //For actual TimeStamp:
  def readBoundSensor(path: String, lbdate: LocalTime, ubdate: LocalTime, nodeName: String): String = {

    var bufferedSource: BufferedSource = null
    try {
      bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + path)
    }
    catch {
      case fileNotFoundException: FileNotFoundException => handleFileNotFoundException(fileNotFoundException, nodeName)
      case _: Throwable => LogMessage(nodeName, "Got some Kind of Exception")
    }
    var output = ""
    val sb = new StringBuilder
    val lineList = bufferedSource.getLines().toList
    val delimiter: String = Helpers.getDelimiterFromLine(lineList.head)
    val datePosition = Helpers.getDatePosition(delimiter)
    //val valuePosition = getValuePosition(delimiter)

    for (line <- lineList) {
      //value part is never used
      //val valuePart = line.split(delimiter)(valuePosition).stripPrefix("(").stripSuffix(")").trim.toInt

      //For TS
      /*
      Added by Johannes
      */
      val timeStamp = Helpers.parseTime(line.split(delimiter)(datePosition), delimiter)
      /*
      End Edit
       */
      if ((lbdate.isBefore(timeStamp) || lbdate.equals(timeStamp)) && (ubdate.isAfter(timeStamp) || ubdate.equals(timeStamp))) {
        //output = output + valuePart.toString + ","
        sb.append(line.toString() + "\n")
      }
    }

    bufferedSource.close
    output = sb.toString()
    if (output != "")
      output = output.stripSuffix("\n").stripMargin('#')
    else
      output += "No Results!"
    output
  }

  //Time Unit values: 'S', 'M', 'H'
  def readTimedSensor(path: String, timePeriod: Long, timeUnit: String, nodeName: String): String = {
    var bufferedSource: BufferedSource = null
    try {
      bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + path)
    }
    catch {
      case fileNotFoundException: FileNotFoundException => handleFileNotFoundException(fileNotFoundException, nodeName)
      case _: Throwable => LogMessage(nodeName, "Got some Kind of Exception")
    }
    var sb = new StringBuilder
    var output: String = ""
    val lineList = bufferedSource.getLines().toList
    val delimiter: String = Helpers.getDelimiterFromLine(lineList.head)
    val datePosition = Helpers.getDatePosition(delimiter)
    //Get current time:
    var currentTime: LocalTime = LocalTime.now()
    var lbDate = currentTime.format(DateTimeFormat)
    //Time Unit values: 'S', 'M', 'H'

    val pastTime = FormattedOutput.getPastTime(currentTime, timePeriod, timeUnit)
    //LogMessage(nodeName, s"Past Time: ${pastTime.toString}")
    //LogMessage(nodeName, s"Current Time: ${currentTime.toString}")
    for (line <- lineList) {
      //process each event line
      val timeStamp = Helpers.parseTime(line.split(delimiter)(datePosition), delimiter)
      if ((pastTime.isBefore(timeStamp) || pastTime.equals(timeStamp)) && (currentTime.isAfter(timeStamp) || currentTime.equals(timeStamp))) {
        sb.append(line.toString() + "\n")
      }

    }

    bufferedSource.close
    output = sb.toString()
    if (output != "")
      output = output.stripSuffix("\n").stripMargin('#')
    else
      output += "No Results!"
    output
  }

  def handleFileNotFoundException(fileNotFoundException: FileNotFoundException, nodeName: String): Unit = {
    LogMessage(nodeName, fileNotFoundException.toString)
  }
}

