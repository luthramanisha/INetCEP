package nfn.service

/**
  * Created by Johannes on 24.10.18.
  */

import java.time.format.DateTimeFormatter

import nfn.tools.Helpers

//Added for contentfetch
import akka.actor.ActorRef

import scala.language.postfixOps

//Added for contentfetch
import ccn.packet.CCNName

import scala.language.postfixOps

//Added for CCN Command Execution:
import config.StaticConfig

class Prediction1() extends NFNService {
  val sacepicnEnv = StaticConfig.systemPath


  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {
    val nodeInfo = interestName.cmps.mkString(" ")
    val nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1)
    LogMessage(nodeName, "started Function")
    LogMessage(nodeName, "Arguments: " + args.toString())

    def predictionHandler(inputFormat: String, outputFormat: String, granularity: String, stream: String): String = {
      LogMessage(nodeName, s"Input Format: " + inputFormat + ", Stream found at: " + stream + ", granularity: " + granularity.toString + ", outputFormat:" + outputFormat.toString)
      val granularityInSeconds = granularity.takeRight(1).toLowerCase() match {
        case "s" => granularity.dropRight(1).toInt
        case "m" => granularity.dropRight(1).toInt * 60
        case "h" => granularity.dropRight(1).toInt * 60 * 60
        case _ => 0
      }

      val houseIdQuantity = 40
      val householdIdQuantity = 18
      val plugIdQuantity = 28

      val historyGranularity = Math.round(86400 / granularityInSeconds)
      val historyArray = Array.ofDim[Double](historyGranularity, houseIdQuantity, householdIdQuantity, plugIdQuantity)

      var output = ""
      //Sensor is not the preferred way to perform a prediction. Suggested is 'name' and 'data'
      LogMessage(nodeName, s"Decide on the inputFormat Format")
      if (inputFormat.toLowerCase().equals("sensor")) {
        LogMessage(nodeName, s"Performing Prediction on Sensor data")
        output = predict(Helpers.parseData(inputFormat, stream), houseIdQuantity, householdIdQuantity, plugIdQuantity, granularityInSeconds, historyArray)
      }
      else if (inputFormat.toLowerCase().equals("data")) {
        LogMessage(nodeName, s"Perform Prediction on inline data")
        output = predict(Helpers.parseData(inputFormat, stream), houseIdQuantity, householdIdQuantity, plugIdQuantity, granularityInSeconds, historyArray)
      }
      else if (inputFormat.toLowerCase().equals("name")) {
        LogMessage(nodeName, s"Perform Prediction on named data")
        val intermediateResult = Helpers.handleNamedInputSource(nodeName, stream, ccnApi)
        LogMessage(nodeName, s"IntermediateResult = " + intermediateResult)
        var input = ""
        input += Helpers.trimData(intermediateResult)
        if (!intermediateResult.contains("No Result")) {
          output = predict(Helpers.parseData(inputFormat, input), houseIdQuantity, householdIdQuantity, plugIdQuantity, granularityInSeconds, historyArray)
        }
        else {
          output = "No Results!"
        }
      }
      else {
        LogMessage(nodeName, s"Input Source Type not recognized. Doing nothing")
      }

      if (output != "")
        output = output.stripSuffix("\n").stripMargin('#')
      else
        output += "No Results!"

      if (outputFormat.toLowerCase == "name") {
        output = Helpers.storeOutput(nodeName, output, "PREDICT1", "onOperators", ccnApi)
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
    * Performs a prediction on a given list of strings. the list of strings is the content of a window
    *
    * @param data                 the data on which to perform the prediction on
    * @param houseIdQuantity      how many houses exist
    * @param householdIdQuantity  how many households at max exist
    * @param plugIdQuantity       how many plugs at max exist
    * @param granularityInSeconds the granularity at which rate predictions are made
    * @param historyArray         an array with past values
    * @return a string with predictions written in it. each prediction is in a new line and is a new tuple of information
    */
  def predict(data: List[String], houseIdQuantity: Int, householdIdQuantity: Int, plugIdQuantity: Int, granularityInSeconds: Int, historyArray: Array[Array[Array[Array[Double]]]]): String = {
    var output = new StringBuilder()
    if (data.nonEmpty) {
      val delimiter = Helpers.getDelimiterFromLine(data.head)
      val valuePosition = Helpers.getValuePosition(delimiter)
      val datePosition = Helpers.getDatePosition(delimiter)
      val propertyPosition = 3
      val plugIdPosition = 4
      val householdIdPosition = 5
      val houseIdPosition = 6
      var houseId = 0
      var householdId = 0
      var plugId = 0
      val initialSecondsOfDay = Helpers.parseTime(data.head.split(delimiter)(datePosition).stripPrefix("(").stripSuffix(")").trim, delimiter)
      val initialTimeStamp = initialSecondsOfDay.getHour * 60 * 60 + initialSecondsOfDay.getMinute * 60 + initialSecondsOfDay.getSecond
      var currentGranularity = Math.round(initialTimeStamp / granularityInSeconds)

      val temporaryArray = Array.ofDim[Double](houseIdQuantity, householdIdQuantity, plugIdQuantity)
      for (line <- data) {
        //Iterate through each line
        if (!line.contains("redirect") && line != "") {
          val timeStamp = Helpers.parseTime(line.split(delimiter)(datePosition).stripPrefix("(").stripSuffix(")").trim, delimiter) // get the time stamp of one tuple
          val secondsOfTheDay = timeStamp.getHour * 60 * 60 + timeStamp.getMinute * 60 + timeStamp.getSecond // calculates how many seconds have passed since midnight
          val correspondingGranularity = Math.round(secondsOfTheDay / granularityInSeconds) // maps the timestamp to a corresponding granularity in the history Array.

          //if(correspondingGranularity > 719)
          //  System.out.println("Test")
          //set the boundaryPassed variable to check if a time window in the granularity is passed and we have to emit a prediction
          //System.out.println("Current Granularity: " + currentGranularity)
          //System.out.println("Corresponding Granularity: "+correspondingGranularity)
          if (currentGranularity <= correspondingGranularity) { // if it is time for a new prediction
            val windowIdForPrediction = (correspondingGranularity + 2) % historyArray.size // the time we make a prediction for
            var hId = 0
            var hhId = 0
            var plgId = 0
            var predictedPlugLoad = 0.0
            var predictedHouseLoad = 0.0
            var validRecord = false
            //we now iterate through all houses, households and plugs and make a prediction for each and everyone.
            //System.out.println("Houses in TempArray: "+temporaryArray.size)
            //System.out.println("HouseHolds in TempArray: "+temporaryArray(0).size)
            //System.out.println("Plugs in TempArray: "+temporaryArray(0)(0).size)
            for (house <- temporaryArray) {
              hhId = 0
              for (household <- house) {
                plgId = 0
                for (plug <- household) {
                  if (plug != 0) {
                    validRecord = true
                    if (historyArray(windowIdForPrediction)(hId)(hhId)(plgId) != 0) {
                      // if there is historic data present in the history Array, we can make a prediciton based off of it.
                      predictedPlugLoad = (historyArray(windowIdForPrediction)(hId)(hhId)(plgId) + (plug)) / 2 // calculates the predicted plug load as the average of the corresponding past plug load and the current one
                      output.append(timeStamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")).toString + "," + windowIdForPrediction + "," + hId + "," + hhId + "," + plgId + "," + predictedPlugLoad + "\n") // writes the tuple of the prediction to the output
                      historyArray(currentGranularity)(hId)(hhId)(plgId) = (historyArray(currentGranularity)(hId)(hhId)(plgId) + (plug)) / 2
                    }
                    else {
                      // if there is no historic data we base the prediction just off of the current load
                      predictedPlugLoad = plug // the average current plug load
                      output.append(timeStamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")).toString + "," + windowIdForPrediction + "," + hId + "," + hhId + "," + plgId + "," + predictedPlugLoad + "\n") // writes the tuple of the prediction to the output
                      historyArray(currentGranularity)(hId)(hhId)(plgId) = predictedPlugLoad
                    }
                    if (predictedHouseLoad != 0) {
                      predictedHouseLoad = (predictedHouseLoad + predictedPlugLoad) / 2 // also adds the predicted plug load to the predicted house load and averages it
                    }
                    else {
                      predictedHouseLoad = predictedPlugLoad
                    }
                  }
                  //System.out.println("House: "+hId)
                  //System.out.println("HouseHold: "+hhId)
                  //System.out.println("Plug: "+plgId)
                  temporaryArray(hId)(hhId)(plgId) = 0 // reset the recording
                  plgId = plgId + 1
                }
                hhId = hhId + 1
              }
              if (validRecord) {
                //output.append(timeStamp.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS")).toString + "," + windowIdForPrediction + "," + hId + "," + predictedHouseLoad + "\n")
                validRecord = false
              } // write the tuple for the house load prediction to the output
              predictedHouseLoad = 0
              hId = hId + 1
            }
            currentGranularity = (currentGranularity + 1) % historyArray.size
          }

          // check if the property is the work or the load. 1 is load 0 is work, we are only interested in accumulating the load
          if (line.toString.split(delimiter)(propertyPosition).trim.toInt == 1) {
            // if it is not time for a prediction we store the data in between predictions in a temporary array. We use it later for when it is time for a prediction
            houseId = line.toString.split(delimiter)(houseIdPosition).trim.toInt
            householdId = line.toString.split(delimiter)(householdIdPosition).trim.toInt
            plugId = line.toString.split(delimiter)(plugIdPosition).trim.toInt
            var value = (temporaryArray(houseId)(householdId)(plugId) + line.toString.split(delimiter)(valuePosition).trim.toDouble) / 2
            temporaryArray(houseId)(householdId)(plugId) = value
          }
        }

      }
      output.toString()
    }
    else
      output.toString()
  }

}