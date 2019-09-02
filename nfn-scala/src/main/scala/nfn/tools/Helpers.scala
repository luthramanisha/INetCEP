package nfn.tools

import java.io.{File, FileOutputStream, PrintWriter}
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.{Base64, Calendar}

import SACEPICN.NodeMapping
import akka.actor.ActorRef
import ccn.packet._
import config.StaticConfig
import myutil.FormattedOutput
import nfn.NFNApi
import nfn.service.LogMessage
import nfn.tools.Networking.{fetchContentAndKeepAlive, fetchContentRepeatedly,fetchContent}

import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps
import scala.sys.process._

/**
  * Created by blacksheeep on 17/11/15.
  * Updated by Ali on 22/09/2018
  * Updated by Johannes on 31/01/2019
  */
object Helpers {
  val sacepicnEnv: String = StaticConfig.systemPath

  /**
    * Convert a Array[Byte] to a String (base64 encoding)
    * Inverse function of stringToByte(...)
    *
    * NOTE: Output String has more characters than length of data:Byte[Array] because of base64 encoding.
    *
    * @param   data Data to convert
    * @return Same data as String (base64 encoding)
    */
  def byteToString(data: Array[Byte]): String =
    Base64.getEncoder.encodeToString(data) // note: this is new in java 8


  /**
    * Convert a String (base64 encoding) to a Array[Byte].
    * Inverse function of byteToString(...)
    *
    * NOTE: data:String has more characters than length of returned Byte[Array] because of base64 encoding.
    *
    * @param   data Data to convert (base64 encoding)
    * @return Same data as Array[Byte]
    */
  def stringToByte(data: String): Array[Byte] =
    Base64.getDecoder.decode(data) // note: this is new in java 8

  /**
    * Writes two strings into a single file placementUtilityFunction
    *
    * @param Energy   the Energy data
    * @param Overhead the overhead used
    * @return void
    */
  def writeMetricsToStore(Energy: String, Overhead: String): Any = {
    val weights = s"$sacepicnEnv/nodeData/placementUtilityFunction"
    val file1 = new File(weights)
    file1.getParentFile.mkdirs()
    file1.createNewFile()

    val pw1 = new PrintWriter(new FileOutputStream(file1, true))

    var writeText = s"$Energy\\|$Overhead"
    pw1.println(writeText)
    pw1.close()
  }

  /**
    * Writes a string given as the first arguemtn in the queryOutput and a string given as the second argument in the queryWeightVariance
    *
    * @param runAnalysis    a string with all analysis data form the run
    * @param weightVariance a string containing the weight variance
    * @return void
    */
  def writeOutputFiles(runAnalysis: String, queryResult: String): Any = {

    var queryOutput = s"$sacepicnEnv/nodeData/queryOutput"
    var queryResultFile = s"$sacepicnEnv/nodeData/queryResult"
    val file1 = new File(queryOutput)
    val file4 = new File(queryResultFile)
    file1.getParentFile.mkdirs()
    file1.createNewFile()
    file4.getParentFile.mkdirs()
    file4.createNewFile()

    val pw1 = new PrintWriter(new FileOutputStream(file1, true))

    val pw4 = new PrintWriter(new FileOutputStream(file4,true))
    pw1.println(runAnalysis)
    pw1.close()
    pw4.println("Query Result:")
    pw4.println(queryResult)
    pw4.close()
  }

  /**
    * Writes a string given as the first arguemtn in the queryOutput and a string given as the second argument in the queryWeightVariance
    *
    * @param runAnalysis    a string with all analysis data form the run
    * @param weightVariance a string containing the weight variance
    * @return void
    */
  def writeOutputFiles(runAnalysis: String, weightVariance: String, queryResult: String): Any = {

    var queryOutput = s"$sacepicnEnv/nodeData/queryOutput"
    var queryWeightVariance = s"$sacepicnEnv/nodeData/queryWeightVariance"
    var queryResultFile = s"$sacepicnEnv/nodeData/queryResult"
    val file1 = new File(queryOutput)
    val file2 = new File(queryWeightVariance)
    val file4 = new File(queryResultFile)
    file1.getParentFile.mkdirs()
    file1.createNewFile()
    file2.getParentFile.mkdirs()
    file2.createNewFile()
    file4.getParentFile.mkdirs()
    file4.createNewFile()

    val pw1 = new PrintWriter(new FileOutputStream(file1, true))
    val pw2 = new PrintWriter(new FileOutputStream(file2, true))

    val pw4 = new PrintWriter(new FileOutputStream(file4,true))
    pw1.println(runAnalysis)
    pw1.close()
    pw2.println(weightVariance)
    pw2.close()
    pw4.println("Query Result:")
    pw4.println(queryResult)
    pw4.close()
  }

  def writeOutputFiles(runAnalysis: String, weightVariance: String, accuracyOutput: String, queryResult: String): Any = {

    var queryOutput = s"$sacepicnEnv/nodeData/queryOutput"
    var queryWeightVariance = s"$sacepicnEnv/nodeData/queryWeightVariance"
    var accuracyOutputFile = s"$sacepicnEnv/nodeData/queryAccuracy"
    var queryResultFile = s"$sacepicnEnv/nodeData/queryResult"
    val file1 = new File(queryOutput)
    val file2 = new File(queryWeightVariance)
    val file3 = new File(accuracyOutputFile)
    val file4 = new File(queryResultFile)
    file1.getParentFile.mkdirs()
    file1.createNewFile()
    file2.getParentFile.mkdirs()
    file2.createNewFile()
    file3.getParentFile.mkdirs()
    file3.createNewFile()
    file4.getParentFile.mkdirs()
    file4.createNewFile()
    val pw1 = new PrintWriter(new FileOutputStream(file1, true))
    val pw2 = new PrintWriter(new FileOutputStream(file2, true))
    val pw3 = new PrintWriter(new FileOutputStream(file3, true))
    val pw4 = new PrintWriter(new FileOutputStream(file4,true))
    pw1.println(runAnalysis)
    pw1.close()
    pw2.println(weightVariance)
    pw2.close()
    pw3.println(accuracyOutput)
    pw3.close()
    pw4.println("Query Result:")
    pw4.println(queryResult)
    pw4.close()
  }

  @deprecated("Was used for the old representatino of the querystore. use the new one instead since we also store the placement algorithm in it", "4.11.2018")
  def save_to_QueryStore(runID: String, sourceOfQuery: String, interestOrigin: String, clientID: String, query: String, region: String, timestamp: String): Boolean = {
    //Source is not QueryStore and DecentralizeQuery
    if (sourceOfQuery != "QS" && sourceOfQuery != "DQ") {
      var filename = s"$sacepicnEnv/nodeData/queryStore"
      val file = new File(filename)
      file.getParentFile.mkdirs()
      file.createNewFile()

      //clear the file for old queries
      var w = new PrintWriter(file)
      w.close()

      val pw = new PrintWriter(new FileOutputStream(file, true))

      var now = Calendar.getInstance()
      var q_TimeStamp = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE)

      var queryToStore = s"QID:${clientID}_$q_TimeStamp $runID $interestOrigin $clientID $query $region $timestamp"
      pw.println(queryToStore)
      pw.close()

      return true
    }

    false
  }


  //Overloaded method for QS Storage. The queryService.sh script will use this version and will read queries as per this signature. (11/03/18)
  /**
    * Stores the initial query in the query store
    *
    * @param algorithm      the algorithm of the placement service
    * @param runID          the id of the run (the one given in the publish remotely script I guess)
    * @param sourceOfQuery  the client on which the query runs
    * @param interestOrigin not used
    * @param clientID       the id of the client
    * @param query          the actual query
    * @param region         the region given in the query
    * @param timestamp      the timestamp given in the query
    * @return true if the source of the query was not the queryStore (so we do not flood it) or a decentralized query, false otherwise
    */
  def save_to_QueryStore(algorithm: String, runID: String, sourceOfQuery: String, interestOrigin: String, clientID: String, query: String, region: String, timestamp: String): Boolean = {
    //Source is not QueryStore and DecentralizeQuery
    if (sourceOfQuery != "QS" && sourceOfQuery != "DQ") {
      var filename = s"$sacepicnEnv/nodeData/queryStore"
      val file = new File(filename)
      file.getParentFile.mkdirs()
      file.createNewFile()

      //clear the file with old queries
      var w = new PrintWriter(file)
      w.close()

      val pw = new PrintWriter(new FileOutputStream(file, true))

      var now = Calendar.getInstance()
      var q_TimeStamp = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE)

      var queryToStore = s"QID:${clientID}_$q_TimeStamp $algorithm $runID $sourceOfQuery $clientID $query $region $timestamp"
      pw.println(queryToStore)
      pw.close()

      return true
    }

    false
  }

  //Overloaded method for QS Storage. The queryService.sh script will use this version and will read queries as per this signature. (11/03/18)
  /**
    * Stores the initial query in the query store
    *
    * @param algorithm      the algorithm of the placement service
    * @param processing     processing type: centralized or decentralized
    * @param runID          the id of the run (the one given in the publish remotely script I guess)
    * @param sourceOfQuery  the client on which the query runs
    * @param interestOrigin not used
    * @param clientID       the id of the client
    * @param query          the actual query
    * @param region         the region given in the query
    * @param timestamp      the timestamp given in the query
    * @return true if the source of the query was not the queryStore (so we do not flood it) or a decentralized query, false otherwise
    */
  def save_to_QueryStore(algorithm: String, processing:String, runID: String, sourceOfQuery: String, interestOrigin: String, clientID: String, query: String, region: String, timestamp: String): Boolean = {
    //Source is not QueryStore and DecentralizeQuery
    if (sourceOfQuery != "QS" && sourceOfQuery != "DQ") {
      var filename = s"$sacepicnEnv/nodeData/queryStore"
      val file = new File(filename)
      file.getParentFile.mkdirs()
      file.createNewFile()

      //clear the file with old queries
      var w = new PrintWriter(file)
      w.close()

      val pw = new PrintWriter(new FileOutputStream(file, true))

      var now = Calendar.getInstance()
      var q_TimeStamp = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE)

      var queryToStore = s"QID:${clientID}_$q_TimeStamp $algorithm $processing $runID $sourceOfQuery $clientID $query $region $timestamp"
      pw.println(queryToStore)
      pw.close()

      return true
    }

    false
  }

  def getMultiObjectiveFunctionMetrics: Array[Double] = {
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/nodeData/placementUtilityFunction")
    var returnData = new Array[Double](2); //0.8|0.2 (ENERGY|BANDWIDTH.DELAY.PRODUCT)
    //To ensure that we always have utility function. Else, we get it from the file
    returnData(0) = 0.5
    returnData(1) = 0.5

    //To ensure that we get a E|BDP value from the file, we iterate over all lines and check the last line in the config file.
    bufferedSource
      .getLines
      .foreach { line: String =>
        var lineSplit = line.split("\\|")
        if (lineSplit.length > 1) {
          //Load this information in the Array:
          returnData(0) = FormattedOutput.parseDouble(lineSplit(0))
          returnData(1) = FormattedOutput.parseDouble(lineSplit(1))
        }
      }

    bufferedSource.close

    returnData
  }

  /**
    * Returns the path for the node Information file
    *
    * @return the path for the node Information file
    */
  def getNodeInformationPath: String = {
    s"$sacepicnEnv/nodeData/nodeInformation"
  }

  /**
    * Returns the path for the decentralized k-hop value file
    *
    * @return the path for the decentralilzed k-hop value file
    */
  def getDecentralizedKHops: String = {
    s"$sacepicnEnv/nodeData/Decentralized_KHop"
  }

  /**
    * Returns the decentralized nodeinformation for the given node name
    *
    * @param nodeName the name of the node we want to have the decentralized node information for
    * @return the decentralized node information for the given node name
    */
  def getDecentralizedNodeInformation(nodeName: String): String = {
    s"$sacepicnEnv/nodeData/$nodeName"
  }

  /**
    *
    * @param query
    * @param nodeName
    * @param ccnApi
    * @return
    */
  def executeInterestQuery(query: String, nodeName: String, ccnApi: ActorRef): String = {
    val nameOfContentWithoutPrefixToAdd = CCNName(new String(query).split("/").tail: _*)
    LogMessage(nodeName, s"execute Interest query ${query} called with ${nodeName}")
    var result = new String(fetchContentRepeatedly(
      Interest(nameOfContentWithoutPrefixToAdd),
      ccnApi,
      15 seconds).get.data)
    LogMessage(nodeName, s"Query Result from network node: ${result}")
    if (result.contains("timeout") || result.contains("interest") || result == "")
      result = "No Result!"
    return result
  }

  /**
    * Was used to create a CCNQuery and the execute it on the command line
    *
    * @param nodeName the nodename that executes the command, used for debugging purposes only
    * @param query    the query to execute
    * @param port     the port of the remote node
    * @param IP       the ip address of the remote node
    * @return the result of the execution
    */
  @deprecated("Was used to bypass the face but is not used anymore. Use executeNFNQuery instead.", "02.02.2019")
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

  @deprecated("Was used to execute a comamnd in the command line. Not used anymore because we now use other ways to get data.", "02.02.2019")
  def execcmd(cmd1: String, cmd2: String): String = {
    val result = Seq("/bin/sh", "-c", s"${cmd1} | ${cmd2}").!!
    return result
  }

  /**
    * Not used at the moment
    * Is responsible to resolve a redirect and fetch larger content
    *
    * @param nodeName the nodename that executes the command, used for debugging purposes only
    * @param query    the query to execute
    * @param port     the port of the remote node
    * @param IP       the ip address of the remote node
    * @return the result of the execution
    */
  def createAndExecCCNQueryForRedirect(nodeName: String, query: String, port: String, IP: String): String = {
    var cmd ="""$CCNL_HOME/bin/ccn-lite-fetch -s ndn2013 -u #IP#/#PORT# "#QUERY#""""
    cmd = cmd.replace("#IP#", s"${IP}").replace("#PORT#", s"${port}").replace("#QUERY#", query);
    LogMessage(nodeName, s"Query sent to network node: ${cmd}")
    var result = execcmdwithoutformatter(cmd)
    LogMessage(nodeName, s"Query Result form network node: ${result}")
    if (result.contains("timeout") || result.contains("interest") || result == "")
      result = "No Result!"
    return result.trim().stripLineEnd
  }

  /**
    * Not used at the moment
    * Exevutes a single command
    *
    * @param cmd1 the command to execute
    * @return the result of the executed commaned
    */
  def execcmdwithoutformatter(cmd1: String): String = {
    val result = cmd1.!!
    return result
  }

  /**
    * Returns a LocalTime from a given string
    *
    * @param datePart  a string representing the date. either in the form of a unix timestamp or in the form of HH:mm:ss.SSS
    * @param delimiter the delimiter that separates the values of the tuple.
    * @return the parsed time in the LocalTime Format
    */
  def parseTime(datePart: String, delimiter: String): LocalTime = {
    val DateTimeFormat = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    var dateString = datePart

    if (delimiter.equals(",")) {
      val calendar = Calendar.getInstance()
      calendar.setTimeInMillis(datePart.toLong * 1000)
      val hour = calendar.get(Calendar.HOUR_OF_DAY).toString
      val minute = calendar.get(Calendar.MINUTE).toString
      val second = calendar.get(Calendar.SECOND).toString

      dateString = ("0" + hour).takeRight(2) + ":" + ("0" + minute).takeRight(2) + ":" + ("0" + second).takeRight(2) + ".000"
    }
    if(delimiter.equals(";")){
      dateString = datePart.split(" ")(1)+".000"
    }

    LocalTime.parse(dateString, DateTimeFormat)
  }

  /**
    * Returns the delimiter that separates a tuple of values when given a single line
    *
    * @param line one tuple of values
    * @return the delimiter specifically for this tuple
    */
  def getDelimiterFromLine(line: String): String = {
    var output: String = ""
    if (line.contains("/"))
      output = "/"
    else if (line.contains(","))
      output = ","
    else if (line.contains(";"))
      output = ";"
    output
  }

  /**
    * Returns the position of the date in a tuple when given a delimiter
    *
    * @param delimiter the delmiiter that separates the values of the tuple. based on this we can return the position of the date in the tuple
    * @return the position of the date in the tuple
    */
  def getDatePosition(delimiter: String): Int = {
    var output: Int = 0
    if (delimiter.equals("/"))
      output = 0
    else if (delimiter.equals(","))
      output = 1
    else if (delimiter.equals(";"))
      output = 0
    output
  }

  /**
    * Returns the position of the value in a tuple when given a delimiter
    *
    * @param delimiter the delimiter that separates the values of the tuple. based on this we can return the position of the value in the tuple
    * @return the position of the value in the tuple
    */
  def getValuePosition(delimiter: String): Int = {
    var output: Int = 0
    if (delimiter.equals("/"))
      output = 1
    else if (delimiter.equals(","))
      output = 2
    else if (delimiter.equals(";"))
      output = 2
    output
  }

  /**
    * Returns the delimiter that seperates a tuple of values when given a file path
    *
    * @param path the path to where the file is located.
    * @return the delimiter specifically for the tuples in this file
    */
  def getDelimiterFromPath(path: String): String = {
    val bufferedSource = Source.fromFile(s"$sacepicnEnv/sensors/" + path)
    var output: String = ""
    //val b = bufferedSource.getLines().find(_ => true).toString()
    if (bufferedSource.getLines().find(_ => true).toString().contains("/"))
      output = "/"
    else if (bufferedSource.getLines().find(_ => true).toString().contains(","))
      output = ","
    else if (bufferedSource.getLines().find(_ => true).toString().contains(";"))
      output = ";"
    return output
  }

  /**
    * Returns the input data as a list of strings
    *
    * @param inputSource either name, sensor or data. decides what type of data is used for the input
    * @param sourceValue either a named interest or a path to a file
    * @return The input data read from the sourceValue as a list of strings
    */
  def parseData(inputSource: String, sourceValue: String): List[String] = {
    var output: List[String] = null
    if (inputSource == "sensor") {
      val dataSource = Source.fromFile(s"$sacepicnEnv/sensors/" + sourceValue)
      output = dataSource.getLines.toList
      dataSource.close
    }
    if (inputSource == "data") {
      output = sourceValue.split("\n")
        .toSeq
        .map(_.trim)
        .filter(_ != "").toList
    }
    if (inputSource == "name") {
      output = sourceValue.split("\n")
        .toSeq
        .map(_.trim)
        .filter(_ != "").toList
    }

    output
  }

  /**
    *
    * @param delimiter the delimiter on which we base our decision on where to find the longitude position
    * @return the position on where to find the longitude in a given tuple
    */
  def getLongitudePosition(delimiter: String): Int = {
    var output: Int = 0
    if (delimiter.equals(";"))
      output = 3
    output
  }

  /**
    *
    * @param delimiter the delimiter on which we base our decision on where to find the latitude position
    * @return the position on where to find the latitude in a given tuple
    */
  def getLattitudePosition(delimiter: String): Int = {
    var output: Int = 0
    if (delimiter.equals(";"))
      output = 2
    output
  }

  /**
    * Handles a named interest which can be either a nested query or a named address. Resolves either and returns the wanted data
    *
    * @param nodeName The name of the node that has to handle the nfn call
    * @param stream   The stream of data that is to be handled
    * @param ccnApi   The actorRef of the caller
    * @return A string containing the resulting data
    */
  def handleNamedInputSource(nodeName: String, stream: String, ccnApi: ActorRef) = {
    var intermediateResult = ""
    if (stream.contains("[") && stream.contains("]")) {
      //This means, filter contains RAW window query (named-function): first resolve the window query:
      val interest = stream.replace("[", "").replace("]", "").replace("{", "\'").replace("}", "\'").replace("(","").replace(")","")
      LogMessage(nodeName, s"Intermediate Named-Function is: ${interest}")
      intermediateResult = Helpers.executeNFNQuery(interest, nodeName, ccnApi,25)
      LogMessage(nodeName, s"Intermediate Named-Function Result is: ${intermediateResult}")

      if (intermediateResult.contains("/") && intermediateResult.split("/").length > 1) {
        intermediateResult = executeInterestQuery(intermediateResult,nodeName,ccnApi)
      }
    }
    else {
      //This means that a named-interest was passed containing the intermediate window result. Just get it
      intermediateResult = fetchFromRemoteNode(nodeName, stream, ccnApi)
      LogMessage(nodeName, s"Intermediate Result: $intermediateResult")
    }
    if (intermediateResult.contains("redirect")) {
      LogMessage(nodeName, "Intermediate result contained redirect, Not trying to resolve it - does not work.")
      /*val str = intermediateResult.replace("\n","").trim
      LogMessage(nodeName, s"trimmed and stripped intermediateResult: ${str}")
      val rname = CCNName(str.splitAt(9)._2.split("/").toList.tail.map(_.replace("%2F", "/").replace("%2f", "/")), None)
      LogMessage(nodeName, s"new call is: ${rname}")
      val interest = new Interest(stream)
      LogMessage(nodeName, s"look up interest $interest")
      intermediateResult = executeInterestQuery(stream,nodeName,ccnApi)*/
      LogMessage(nodeName, s"unresolved redirect: ${intermediateResult}")
    }
    intermediateResult
  }

  /**
    *
    * @param nodeName The current nodename that wants to fetch the data (for debugging pruposes only)
    * @param address  the address/named interest to get
    * @param ccnApi   the actorRef of the current node
    * @return A string with the result
    */
  def fetchFromRemoteNode(nodeName: String, address: String, ccnApi: ActorRef) = {
    val mapping = new NodeMapping()
    LogMessage(nodeName, s"Intermediate Named-Interest is: ${address}")
    val intermediateResult = Helpers.executeNFNQuery(s"(call 2 /node/${mapping.getName(address.split("/")(1))}/nfn_service_GetContent '${address}')", mapping.getName(address.split("/")(1)), ccnApi,20)
    LogMessage(nodeName, s"Result after fetching: ${intermediateResult}")
    intermediateResult
  }

  /**
    *
    * @param query    the query to execute
    * @param nodeName the nodename for debugging purposes
    * @param ccnApi   the actorRef
    * @return the result of the execution of the NFNQuery
    */
  def executeNFNQuery(query: String, nodeName: String, ccnApi: ActorRef, timeoutAfter: Int): String = {

    LogMessage(nodeName, s"execute NFN query called with ${nodeName}")
    var result = new String(fetchContentRepeatedly(
      NFNInterest(query),ccnApi,
      timeoutAfter seconds).get.data)
    LogMessage(nodeName, s"Query Result from network node: ${result}");

    if (result.contains("timeout") || result.contains("interest") || result == "")
      result = "No Result!"
    return result
  }

  /**
    * Trims the data and adds a # in front of each line
    *
    * @param input A string of data
    * @return a string with a # as a leading symbol
    */
  def trimData(input: String) = {
    val output = new StringBuilder
    if (input != null && input != "") {
      val bufferedSource = input.split("\n")
        .toSeq
        .map(_.trim)
        .filter(_ != "")

      bufferedSource
        .foreach { line: String =>{
          if (!line.contains("redirect")){
            output.append(line.toString + "\n")
          }
        }
        }
    }
    output.toString()
  }

  /**
    * Stores content in the cache of a node
    *
    * @param nodeName The name of the node
    * @param input    The content to store
    * @param ccnApi   The Actor ref
    * @param operation The operation that took place
    * @param onWhat On what the opeartion was performed on
    * @return An named address that indicates where to find the content
    */
  def storeOutput(nodeName: String, input: String, operation: String, onWhat: String, ccnApi: ActorRef) = {
    //Return a name that contains the window data. This is a cached content.
    val now = Calendar.getInstance()
    //Get the correct mapped port for this node so that we can publish content through this..
    val prefixOfNode = new NodeMapping().getPort(nodeName)
    LogMessage(nodeName, s"${operation} OP: Prefix of node is: " + prefixOfNode)
    //This will be something like /900X/operation/onWhat/....
    val name = s"/$prefixOfNode/${operation}/${onWhat}/" + now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) + now.get(Calendar.SECOND)

    val nameOfContentWithoutPrefixToAdd = CCNName(new String(name).split("/").tail: _*)
    ccnApi ! NFNApi.AddToLocalCache(Content(nameOfContentWithoutPrefixToAdd, input.getBytes, MetaInfo.empty), prependLocalPrefix = false)
    LogMessage(nodeName, s"Inside ${operation} -> ${operation} name: ${name}, ${operation} content: ${input}")
    name
  }

}
