package nfn.service

/**
  * Created by Ali on 06.02.18.
  */

import akka.actor.ActorRef
import nfn.tools.Helpers

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

//Added for contentfetch
import ccn.packet._
import config.StaticConfig

import scala.language.postfixOps

class Filter() extends NFNService {
  val sacepicnEnv = StaticConfig.systemPath

  def filter(source: String, sourceValue: String, aNDS: ArrayBuffer[String], oRS: ArrayBuffer[String], delimiter: String): String = {
    var data: List[String] = null
    var output = ""
    if (source == "sensor") {
      val dataSource = Source.fromFile(s"$sacepicnEnv/sensors/" + sourceValue)
      data = dataSource.getLines.toList
      dataSource.close
    }
    if (source == "data") {
      data = sourceValue.split("\n")
        .toSeq
        .map(_.trim)
        .filter(_ != "").toList
    }
    if (source == "name") {
      data =
        sourceValue.split("\n")
          .toSeq
          .map(_.trim)
          .filter(_ != "").toList
    }

    if (data.nonEmpty) {
      data.foreach(line => {
        var lineAdded = false
        var andConditionisValid = true
        aNDS.foreach(
          and =>
            if (andConditionisValid && !conditionHandler(and, line, delimiter)) {
              andConditionisValid = false
            })

        if (andConditionisValid && !lineAdded) {
          output += "#" + line.toString + "\n"
          lineAdded = true
        }

        if (!lineAdded) {
          oRS.foreach(
            or => {
              if (!lineAdded && conditionHandler(or, line, delimiter)) {
                output += "#" + line.toString + "\n"
                lineAdded = true
              }
            }
          )
        }
      })
    }
    if (output != "")
      output = output.stripSuffix("\n").stripMargin('#')
    else
      output += "No Results!"
    output
  }

  def conditionHandler(filter: String, line: String, delimiter: String): Boolean = {
    val valGreater = filter.toString.split('>').map(_.trim)
    val valLessthan = filter.toString.split('<').map(_.trim)
    val valEqual = filter.toString.split('=').map(_.trim)
    val valNotEqual = filter.toString.split("<>").map(_.trim)

    var operator = ""
    var queryVariable = ""
    var queryColumn = 0

    if (operator == "" && valNotEqual.length == 2) {
      //User has given query: value<>X
      operator = "<>";
      queryColumn = valNotEqual(0).stripPrefix("(").stripSuffix(")").trim.toInt
      queryVariable = valNotEqual(1).stripPrefix("(").stripSuffix(")").trim
      return matchCondition(operator, queryColumn, queryVariable, line, delimiter)
    }
    if (operator == "" && valGreater.length == 2) {
      //User has given query: value>X
      operator = ">";
      queryColumn = valGreater(0).stripPrefix("(").stripSuffix(")").trim.toInt
      queryVariable = valGreater(1).stripPrefix("(").stripSuffix(")").trim
      return matchCondition(operator, queryColumn, queryVariable, line, delimiter)
    }
    if (operator == "" && valLessthan.length == 2) {
      //User has given query: value<X
      operator = "<";
      queryColumn = valLessthan(0).stripPrefix("(").stripSuffix(")").trim.toInt
      queryVariable = valLessthan(1).stripPrefix("(").stripSuffix(")").trim
      return matchCondition(operator, queryColumn, queryVariable, line, delimiter)
    }
    if (operator == "" && valEqual.length == 2) {
      //User has given query: value=X
      operator = "=";
      queryColumn = valEqual(0).toString.stripPrefix("(").stripSuffix(")").trim.toInt;
      queryVariable = valEqual(1).toString.stripPrefix("(").stripSuffix(")").trim;
      return matchCondition(operator, queryColumn, queryVariable, line, delimiter)
    }
    //Default Case - Options can have anything
    if (operator == "") {
      return false
    }

    false
  }

  def matchCondition(operator: String, queryColumn: Int, queryVariable: String, line: String, delimiter: String): Boolean = {
    //First, split the link into schema:
    var schema = line.split(delimiter);

    var index = queryColumn - 1;

    if (schema.length > index) {
      try {
        operator match {
          case ">" => if (schema(index).toDouble > queryVariable.toString.toDouble) {
            true
          } else false
          case "<" => if (schema(index).toDouble < queryVariable.toString.toDouble) {
            true
          } else false
          case "=" => if (schema(index).toLowerCase() == queryVariable.toLowerCase()) {
            true
          } else false
          case "<>" => if (schema(index).toLowerCase() != queryVariable.toLowerCase()) {
            true
          } else false
          case "NULL" => false
          case _ => false
        }
      }
      catch {
        case e: Exception => false
      }
    }
    else {
      false
    }
  }

  def parseFilterArguments(filter:String)={
    var retVal = new Array[ArrayBuffer[String]](2)
    var allANDs = ArrayBuffer[String]()
    var allORs = ArrayBuffer[String]()

    val orFilters = filter.split('|').map(_.trim)
    if (orFilters.length > 0 && filter.contains("|")) {
      for (oF <- orFilters) {
        //This will give us a filter that contains ||
        if (oF.contains("&")) {
          //Further splits needed:
          //Split the AND first:
          val childAndOP = oF.split("&").map(_.trim)
          for (childOP <- childAndOP) {
            //Handle each condition and put it in a list of all AND conditions
            allANDs += childOP
          }
        }
        else {
          //This part of the OR Split does not contain and ANDs. Push all to OR list:
          allORs += oF
        }
      }
    }
    else {
      //Filter doesnt contain any ORs - so filter it with ANDs
      val andFilters = filter.split("&").map(_.trim)
      if (andFilters.length > 0 && filter.contains("&")) {
        for (aF <- andFilters) {
          //This will give us a filter that contains &&
          allANDs += aF
        }
      }
      else {
        //This is a single filter (e.g. 2>1001, so add it to andFilters and pass to handler)
        allANDs += filter
      }

    }

    retVal(0) = allANDs
    retVal(1) = allORs
    retVal
  }

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {
    var nodeInfo = interestName.cmps.mkString(" ")
    var nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1)

    //stream: Victims/Survivors
    //filter: 1>20||2=F&&3<40 etc. 1 denotes the first column, 2 denotes the second column.
    //- Here the query consumer must know what they are looking for. Else we will return a generic wrong schema message.
    //- Filter is done on OR (||) and AND (&&).
    //Victim Data: 22:18:38.841/1001/M/50 <- Schema: 1/2/3/4
    //Sample Query: 'Victims' '2=1001&&3=M||4>46'
    //First break OR and then get AND
    def filterStream(source: String, stream: String, filter: NFNStringValue, outputFormat: NFNStringValue): String = {

      //Interest will always be in the form of: call X /node/nodeX/nfn_service_X
      //Using this we can extract the node for this operation.

      LogMessage(nodeName, s"\nFilter OP Started")
      val filterParams = parseFilterArguments(filter.str)

      //By this time, we will have two stacks of filters - OR and AND. We will pass this to the handler.
      //return "=" + allANDs.mkString(",") + " - " + allORs.mkString(",");
      filterHandler(source, stream, filterParams(0), filterParams(1), outputFormat.str, nodeName)
    }

    def filterHandler(inputFormat: String, stream: String, aNDS: ArrayBuffer[String], oRS: ArrayBuffer[String], outputFormat: String, nodeName: String): String = {
      var output = ""
      var delimiter = ""

      if (inputFormat.toLowerCase == "sensor") {
        delimiter = Helpers.getDelimiterFromPath(stream)
        LogMessage(nodeName, s"Delimiter is $delimiter")
        LogMessage(nodeName, s"Performing filter on Sensor data")
        output = filter(inputFormat, stream, aNDS, oRS, delimiter)
      }
      else if (inputFormat.toLowerCase == "data") {
        delimiter = Helpers.getDelimiterFromPath(stream)
        LogMessage(nodeName, s"Delimiter is $delimiter")
        LogMessage(nodeName, s"Performing filter on inline data")
        output = filter(inputFormat, stream, aNDS, oRS, delimiter)
      }
      else if (inputFormat.toLowerCase == "name") {
        LogMessage(nodeName, "Handle Filter Stream")
        var intermediateResult = Helpers.handleNamedInputSource(nodeName, stream, ccnApi)
        //At this point, we will definitely have the intermediate window result.
        LogMessage(nodeName, s"Inside Filter -> Child Operator Result: $intermediateResult")
        delimiter = Helpers.getDelimiterFromLine(intermediateResult)
        LogMessage(nodeName, s"Delimiter is $delimiter")
        output = filter(inputFormat, intermediateResult, aNDS, oRS, delimiter)
      }

      //If outputFormat = name => we will return a named interest
      //If outputFormat = data => we will return the data
      if (outputFormat.toLowerCase == "name") {
        output = Helpers.storeOutput(nodeName, output, "FILTER", "onWindow", ccnApi)
      }
      else {
        LogMessage(nodeName, s"Inside Filter -> FILTER name: NONE, FILTER content: ${output}")
      }
      LogMessage(nodeName, s"Filter OP Completed\n")
      output
    }

    NFNStringValue(
      args match {
        //Output format: Either name (/node/Filter/Sensor/Time) or data (data value directly)
        //[data/sensor][string of data][filter][outputFormat]
        case Seq(timestamp: NFNStringValue, source: NFNStringValue, stream: NFNStringValue, filter: NFNStringValue, outputFormat: NFNStringValue) => filterStream(source.str, stream.str, filter, outputFormat)
        //[content][contentobject][filter][outputFormat]
        case Seq(timestamp: NFNStringValue, source: NFNStringValue, stream: NFNContentObjectValue, filter: NFNStringValue, outputFormat: NFNStringValue) => filterStream(source.str, new String(stream.data), filter, outputFormat)
        //[sensor][string of data][filter]
        case Seq(timestamp: NFNStringValue, stream: NFNStringValue, filter: NFNStringValue, outputFormat: NFNStringValue) => filterStream("sensor", stream.str, filter, outputFormat)
        case _ =>
          throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
      }
    )
  }
}

