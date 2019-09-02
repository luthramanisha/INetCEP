package nfn.service

import nfn.tools.Helpers

//Added for contentfetch
import akka.actor.ActorRef

import scala.language.postfixOps

//Added for contentfetch
import ccn.packet.CCNName

//Added for CCN Command Execution:
import config.StaticConfig

class Prediction2 extends NFNService {
  val sacepicnEnv = StaticConfig.systemPath
  var historyArray: Array[Double] = null
  var currentGranularity = -1
  var currentAverage = 0.0

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {

    def predictionHandler(inputFormat: String, outputFormat: String, granularity: String, stream: String): String = {

      val nodeInfo = interestName.cmps.mkString(" ")
      val nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1);

      var output = ""

      //Sensor is not the preferred way to perform a prediction. Suggested is 'name'
      if (inputFormat.toLowerCase().equals("sensor")) {
        LogMessage(nodeName, s"Performing Prediction on Sensor data")
        output = predict(Helpers.parseData(inputFormat, stream), granularity)
      }
      else if (inputFormat.toLowerCase().equals("data")) {
        LogMessage(nodeName, s"Perform Prediction on inline data")
        output = predict(Helpers.parseData(inputFormat, stream), granularity)
      }
      else if (inputFormat.toLowerCase().equals("name")) {
        LogMessage(nodeName, s"Perform Prediction on named data")
        val intermediateResult = Helpers.handleNamedInputSource(nodeName, stream, ccnApi)
        output = predict(Helpers.parseData(inputFormat, intermediateResult), granularity)
      }
      if (output != "")
        output = output.stripSuffix("\n").stripMargin('#')
      else
        output += "No Results!"

      if (outputFormat.toLowerCase == "name") {
        output = Helpers.storeOutput(nodeName, output, "PREDICT2", "onWindow", ccnApi)
      }
      else {
        LogMessage(nodeName, s"Inside Predict -> Predict name: NONE, Predict content: $output")
      }
      LogMessage(nodeName, s"Predict OP Completed\n")

      output
    }

    NFNStringValue(
      args match {
        case Seq(timestamp: NFNStringValue, inputFormat: NFNStringValue, outputFormat: NFNStringValue, granularity: NFNStringValue, inputSource: NFNStringValue) => predictionHandler(inputFormat.str, outputFormat.str, granularity.str, inputSource.str)
        case _ =>
          throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
      }
    )
  }

  /**
    *
    * @param data                 the data on which to perform the prediction on
    * @param granularityInSeconds the granularity at which rate predictions are made
    * @param historyArray         an array with past values
    * @return a string with predictions written in it. each prediction is in a new line and is a new tuple of information
    */
  def predict(data: List[String], granularity: String): String = {
    val granularityInSeconds = granularity.takeRight(1).toLowerCase() match {
      case "s" => granularity.dropRight(1).toInt
      case "m" => granularity.dropRight(1).toInt * 60
      case "h" => granularity.dropRight(1).toInt * 60 * 60
      case _ => 0
    }
    val historyGranularity = Math.round(86400 / granularityInSeconds)
    if (historyArray == null) {
      historyArray = Array.ofDim[Double](historyGranularity)
    }
    val output = new StringBuilder
    if (data.nonEmpty && !data.head.contains("No Results!")) {
      val delimiter = Helpers.getDelimiterFromLine(data.head)
      val valuePosition = Helpers.getValuePosition(delimiter)
      val datePosition = Helpers.getDatePosition(delimiter)
      val propertyPosition = 3
      val plugIdPosition = 4
      val householdIdPosition = 5
      val houseIdPosition = 6
      val initialSecondsOfDay = Helpers.parseTime(data.head.split(delimiter)(datePosition).stripPrefix("(").stripSuffix(")").trim, delimiter)
      val initialTimeStamp = initialSecondsOfDay.getHour * 60 * 60 + initialSecondsOfDay.getMinute * 60 + initialSecondsOfDay.getSecond
      if(currentGranularity == -1){
        currentGranularity = Math.round(initialTimeStamp / granularityInSeconds)
      }

      for (line <- data) {
        //Iterate through each line
        if (line != "") {
          val timeStamp = Helpers.parseTime(line.split(delimiter)(datePosition).stripPrefix("(").stripSuffix(")").trim, delimiter) // get the time stamp of one tuple
          val secondsOfTheDay = timeStamp.getHour * 60 * 60 + timeStamp.getMinute * 60 + timeStamp.getSecond // calculates how many seconds have passed since midnight
          val correspondingGranularity = Math.round(secondsOfTheDay / granularityInSeconds) // maps the timestamp to a corresponding granularity in the history Array.

          //set the boundaryPassed variable to check if a time window in the granularity is passed and we have to emit a prediction
          if (currentGranularity < correspondingGranularity) { // if it is time for a new prediction
            val windowIdForPrediction = (correspondingGranularity + 2) % historyArray.size // the time we make a prediction for
            val hId = line.toString.split(delimiter)(houseIdPosition).trim.toInt
            val hhId = line.toString.split(delimiter)(householdIdPosition).trim.toInt
            val plgId = line.toString.split(delimiter)(plugIdPosition).trim.toInt
            //we now iterate through all houses, households and plugs and make a prediction for each and everyone.

              if (historyArray(windowIdForPrediction) != 0) {
                output.append(timeStamp.toString + "," + windowIdForPrediction + "," + hId + "," + hhId + "," + plgId + "," + ((historyArray(windowIdForPrediction) + currentAverage) / 2) + "\n")
              }
              else {
                output.append(timeStamp.toString + "," + windowIdForPrediction + "," + hId + "," + hhId + "," + plgId + "," + currentAverage + "\n")
              }

            historyArray(correspondingGranularity) = (historyArray(correspondingGranularity) + currentAverage) / 2
            currentGranularity = (currentGranularity + 1) % historyArray.size
            currentAverage = 0
          }

          // check if the property is the work or the load. 1 is load 0 is work, we are only interested in accumulating the load
          if (line.toString.split(delimiter)(propertyPosition).trim.toInt == 1) {
            // if it is not time for a prediction we store the data in between predictions in a temporary array. We use it later for when it is time for a prediction
            currentAverage = (currentAverage + line.toString.split(delimiter)(valuePosition).trim.toDouble) / 2
          }
        }

      }
      output.toString()
    }
    else
      output.toString()
  }

  def handlePrediction() = {

  }


}
