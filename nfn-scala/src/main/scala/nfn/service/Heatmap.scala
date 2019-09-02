package nfn.service

/**
  * Created by Johannes on 14.01.2019
  */

//Added for contentfetch
import akka.actor.ActorRef

import scala.language.postfixOps

//Added for contentfetch
import ccn.packet.CCNName

//Added for CCN Command Execution:
import config.StaticConfig
import nfn.tools.Helpers

class Heatmap extends NFNService {
  val sacepicnEnv = StaticConfig.systemPath

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {
    def heatMapHandler(inputFormat: String, outputFormat: String, granularity: String, lowerBound: String, upperBound: String, leftBound: String, rightBound: String, stream: String): String = {
      val nodeInfo = interestName.cmps.mkString(" ")
      val nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1)
      LogMessage(nodeName,"HEATMAP OP started")

      var heatmap:Array[Array[Int]] = null
      //Sensor is not the preferred way to perform a heatmap. Suggested is 'name'
      if (inputFormat.toLowerCase().equals("sensor")) {
        LogMessage(nodeName, s"Performing Heatmap on Sensor data")
        heatmap = generateHeatmap(Helpers.parseData(inputFormat, stream), granularity.toDouble, lowerBound.toDouble, upperBound.toDouble, leftBound.toDouble, rightBound.toDouble)
      }
      else if (inputFormat.toLowerCase().equals("data")) {
        LogMessage(nodeName, s"Perform Heatmap on inline data")
        heatmap = generateHeatmap(Helpers.parseData(inputFormat, stream), granularity.toDouble, lowerBound.toDouble, upperBound.toDouble, leftBound.toDouble, rightBound.toDouble)
      }
      else if (inputFormat.toLowerCase().equals("name")) {
        LogMessage(nodeName, s"Perform Heatmap on named data")
        val intermediateResult = Helpers.handleNamedInputSource(nodeName, stream, ccnApi)
        LogMessage(nodeName,"Heatmap is after the fetch")
        var input = ""
        if(!intermediateResult.contains("redirect")){
          input += Helpers.trimData(intermediateResult)
          heatmap = generateHeatmap(Helpers.parseData(inputFormat, input), granularity.toDouble, lowerBound.toDouble, upperBound.toDouble, leftBound.toDouble, rightBound.toDouble)
        }
      }
      else {
        LogMessage(nodeName, s"Input Source Type not recognized. Doing nothing")
      }

      var output = ""
      if (outputFormat.toLowerCase == "name" && heatmap != null) {
        output = generateIntermediateHeatmap(heatmap)
        output = Helpers.storeOutput(nodeName, output, "HEATMAP", "onOperator", ccnApi)
      }
      else if(heatmap != null){
        output = heatmapPrinter(heatmap)
        LogMessage(nodeName, s"Inside Heatmap -> Heatmap name: NONE, Heatmap content: $output")
      }

      if (output != "")
        output = output.stripSuffix("\n").stripMargin('#')
      else
        output += "No Results!"


      LogMessage(nodeName, s"Heatmap OP Completed\n")
      output
    }

    NFNStringValue(
      args match {
        case Seq(timestamp: NFNStringValue, inputFormat: NFNStringValue, outputFormat: NFNStringValue, granularity: NFNStringValue, lowerBound: NFNStringValue, upperBound: NFNStringValue, leftBound: NFNStringValue, rightBound: NFNStringValue, inputSource: NFNStringValue) =>
          heatMapHandler(inputFormat.str, outputFormat.str, granularity.str, lowerBound.str, upperBound.str, leftBound.str, rightBound.str, inputSource.str)
        case _ =>
          throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
      }
    )
  }

  /**
    *
    * @param data             the raw data to process
    * @param granularity      the lenght and high of one cell in the resulting heatmap.
    * @param minimalLongitude the minimal Longitude value
    * @param minmalLattitude  the minimal latitude value
    * @param heatmap          a two dimensional array of integer values
    * @return the generated heatmap as a two dimensional array
    */
  def generateHeatmap(data: List[String], granularity: Double, minimalLongitude: Double, maximalLongitude:Double, minmalLattitude: Double, maximalLattitude:Double): Array[Array[Int]] = {
    var output = ""
    val horizontalSize = maximalLattitude.toDouble - minmalLattitude.toDouble
    val verticalSize = maximalLongitude.toDouble - minimalLongitude.toDouble
    val numberOfWidths = Math.ceil(horizontalSize / granularity.toDouble)
    val numberOfHeights = Math.ceil(verticalSize / granularity.toDouble)

    val heatmap = Array.ofDim[Int](numberOfWidths.toInt, numberOfHeights.toInt)
    var lat = 0.0
    var long = 0.0
    val delimiter = Helpers.getDelimiterFromLine(data.head)
    val longitudePosition = Helpers.getLongitudePosition(delimiter)
    val latitudePosition = Helpers.getLattitudePosition(delimiter)
    var correspondingRow = 0
    var correspondingCol = 0
    if (data.nonEmpty) {
      for (line <- data) {
        if (line != "No Results!") {
          lat = line.split(delimiter)(latitudePosition).toDouble
          long = line.split(delimiter)(longitudePosition).toDouble
          correspondingCol = Math.ceil((long - minimalLongitude) / granularity).toInt - 1
          correspondingRow = Math.ceil((lat - minmalLattitude) / granularity).toInt - 1
          if (!(correspondingRow < 0 || correspondingRow > heatmap.length))
            if (!(correspondingCol < 0 || correspondingCol > heatmap(correspondingRow).length))
              heatmap(correspondingRow)(correspondingCol) = heatmap(correspondingRow)(correspondingCol) + 1
        }
      }
    }
    heatmap
  }


  def generateIntermediateHeatmap(heatmap:Array[Array[Int]]) ={

    val sb = new StringBuilder
    val width = heatmap(0).length
    val height = heatmap.length
    for(j <- 0 until height -1){
      for (i <- 0 until width -1){
        if(heatmap(j)(i)!=0){
          sb.append(j+"|"+i+":"+heatmap(j)(i)+";")
        }
      }
    }
    sb.toString().stripSuffix(";")
  }
  /**
    *
    * @param heatmap A two-dimensional Array of Integer Values
    * @return A ASCII representation of the input
    */
  def heatmapPrinter(heatmap: Array[Array[Int]]): String = {
    var output = "Heatmap Start\n"
    val width = heatmap(0).length
    val height = heatmap.length
    var i = 0
    var upperAndLowerLines = "+---+"
    for (i <- 1 until width - 1) {
      upperAndLowerLines = upperAndLowerLines + "---+"
    }
    output = output + upperAndLowerLines + "\n"
    var j = 0
    var k = 0
    for (j <- 0 until height - 1) {
      output = output + "|"
      for (k <- 0 until width - 1) {
        val n = heatmap(j)(k)
        if (n == 0) {
          output = output + " 0" + " |"
        }
        else if ((Math.log10(n) + 1).toInt == 1) {
          output = output + " " + heatmap(j)(k).toString + " |"
        }
        else if ((Math.log10(n) + 1).toInt == 2) {
          output = output + " " + heatmap(j)(k).toString + "|"
        }
        else if ((Math.log10(n) + 1).toInt == 3) {
          output = output + "" + heatmap(j)(k).toString + "|"
        }
      }
      output = output + "\n" + upperAndLowerLines + "\n"
    }
    output = output + "Heatmap End\n"
    return output
  }
}
