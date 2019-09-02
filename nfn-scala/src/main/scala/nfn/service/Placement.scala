package nfn.service
/**
  * Created by Ali on 06.02.18.
  * This is the centralized query placement while using the fetch based network discovery approach
  */
import akka.actor.ActorRef

import scala.io.Source
import scala.util.control._
import ccn.packet.CCNName
import nfn.tools._
import nfn.tools.Networking._

import scala.concurrent.duration._
import scala.language.postfixOps
import java.util._
import java.io._
import java.lang.String
import java.text.SimpleDateFormat

import SACEPICN.{NodeMapping, _}
import myutil.FormattedOutput

import scala.annotation.tailrec
import scala.collection.mutable
import scala.List
import scala.collection.mutable.ArrayBuffer
import scala.collection.immutable.Vector
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Stack
import scala.util.control.Exception._

//Added for contentfetch
import lambdacalculus.parser.ast.{Constant, Str}
import nfn.service._
import java.util.Calendar
import java.time
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import ccn.packet.{CCNName, Content, MetaInfo, NFNInterest, Interest}
import nfn.NFNApi
import config.StaticConfig

//Added for CCN Command Execution:
import sys.process._
import filterAccess.tools.ConfigReader._

class Placement() extends NFNService {

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {


    //Algorithm: Centralized or Decentralized
    //RunID: Used primarily for the query service. Client passes runID = 1, query service then increments this. Runs with runID > 1 signify queries coming from the Query Service and not from consumers.
    //SourceOfQuery: Client will use 'Source', Query Service will use 'QS' or 'DQ'.
    //ClientID: Client who requested the query, usually specified by client id. Currently we don't have client identification, so we simply use 'Client1'
    //Query: The complex query to process
    //Region: User Region to hit for sensors (currently unused but can be used in future work)
    //Timestamp: Used to distinguish the time of arrival for the queries
    def processQuery(algorithm: String, runID: String, sourceOfQuery: String, clientID: String, query: String, region: String, timestamp: String): String = {

      //Run output creation:
      var runTime = s"${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance.getTime)}"
      var timeNow = Calendar.getInstance().getTimeInMillis
      var selectedPath = ""
      var selectedPathEnergy = 0.0
      var selectedPathOverhead = 0.0
      var overallPlacementOverhead = 0.0
      var output = ""
      var algorithmEnum = 0
      var predictionInvolved = false
      var predictionGranularity = ""
      var startTime = ""
      var endTime = ""

      //Sanity Check for Algorithm:
      if (algorithm.toLowerCase == "centralized" || algorithm.toLowerCase == "decentralized") {
        if (algorithm.toLowerCase == "centralized") algorithmEnum = 1 //1 = Centralized
        if (algorithm.toLowerCase == "decentralized") algorithmEnum = 2 //2 = Decentralized
      }
      else {
        return "Invalid Algorithm name. Accepted values are 'Centralized' or 'Decentralized'"
      }

      //Get current node from interest:
      var nodeInfo = interestName.cmps.mkString(" ")
      var nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1)
      LogMessage(nodeName, s"Query Execution Started")

      //Set Mapping:
      var mapping = new NodeMapping()
      //Get thisNodePort:
      val thisNode = mapping.getPort(nodeName)
      //Log Query for interval based trigger:
      if (Helpers.save_to_QueryStore(algorithm, runID, sourceOfQuery, thisNode, clientID, query, region, timestamp)) {
        LogMessage(nodeName, s"Query appended to Query Store - Source $sourceOfQuery")
      } else
        LogMessage(nodeName, s"Query NOT appended to Query Store - Source $sourceOfQuery")

      //Initialize ExpressionTree class:

      var et = new OperatorTree()
      //Create node stack or Node tree (current: Tree)
      LogMessage(nodeName, s"Operator Tree creation Started")
      var timeNow_OpTreeCreation = Calendar.getInstance().getTimeInMillis
      var root = et.createOperatorTree(query)
      var timeOffset_OpTreeCreation = Calendar.getInstance().getTimeInMillis - timeNow_OpTreeCreation
      LogMessage(nodeName, s"Operator Tree creation Completed")

      //Get current Network Status and Path information:
      var timeNow_NodeDiscovery = Calendar.getInstance().getTimeInMillis

      var allNodes = getNodeStatus(algorithmEnum, thisNode, nodeName)
      if (allNodes.length < root._stackSize) {
        return "Query processing stopped."
      }
      var paths = buildPaths(nodeName, thisNode, allNodes)

      var timeOffset_NodeDiscovery = Calendar.getInstance().getTimeInMillis - timeNow_NodeDiscovery
      var maxPath = 0
      LogMessage(nodeName, s"Checking paths:")
      for (path <- paths) {
        LogMessage(nodeName, s"${path.pathNodes.reverse.mkString(" ") + " - BDP: " + path.cumulativePathCost + " - Hops: " + path.hopCount}")
        if(maxPath < path.hopCount){
          maxPath = path.hopCount
        }
      }

      //Now that we have all the paths we need: Place the queries on these paths:
      //1) Find the number of operators in the query:
      var opCount = root._stackSize
      if (algorithmEnum == 1) {
        //2) For this size: Get the path matching the number of operators and the lowest BDP path:
        if(root._stackSize > maxPath)
          LogMessage(nodeName, s"Stack size of node is too big. Stacksize is ${opCount}, but we can only support ${maxPath} operators")
        var optimalPath = paths.filter(x => x.hopCount == opCount).minBy(_.cumulativePathCost)

        selectedPath = optimalPath.pathNodes.mkString("-").toString

        //Getting the cumulative path energy and bdp:

        selectedPathEnergy = FormattedOutput.round(FormattedOutput.parseDouble((optimalPath.cumulativePathEnergy.sum / optimalPath.cumulativePathEnergy.length).toString), 2)

        selectedPathOverhead = FormattedOutput.round(FormattedOutput.parseDouble((optimalPath.cumulativePathBDP.sum / optimalPath.cumulativePathBDP.length).toString), 2)
        overallPlacementOverhead = optimalPath.cumulativePathCost
        //Manage the adaptive path weights that changed over time
        LogMessage(nodeName, s"Checking optimal path energy weights:")
        for (weight <- optimalPath.hopWeights_Energy) {
          LogMessage(nodeName, s"$weight")
        }
        LogMessage(nodeName, s"Checking optimal path BDP weights:")
        for (weight <- optimalPath.hopWeights_BDP) {
          LogMessage(nodeName, s"$weight")
        }

        var timeNow_Placement_Deployment = Calendar.getInstance().getTimeInMillis

        LogMessage(nodeName, s"Operator Placement Started")
        @tailrec
        def processPlacementTree(currentNode: Node, optimalPath: mutable.Buffer[String]): Node = {
          if (currentNode._Cprocessed) {
            LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Placement complete.")
            currentNode
          }
          else {
            if (currentNode.right != null && !currentNode.right._Cprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Right Exists. Process Right")
              processPlacementTree(currentNode.right, optimalPath)
            }
            else if (currentNode.left != null && !currentNode.left._Cprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Left Exists. Process Left")
              processPlacementTree(currentNode.left, optimalPath)
            }
            else {
              if (optimalPath.nonEmpty) {
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Processing Placement")

                currentNode._executionNodePort = optimalPath.last
                currentNode._executionNode = mapping.getName(currentNode._executionNodePort)

                var name = currentNode._query.replace("nodeQuery", currentNode._executionNode)
                val query = currentNode._type match {
                  case Operator.WINDOW => name
                  case Operator.FILTER => name
                  case Operator.JOIN => name
                  case Operator.AGGREGATION => name
                  case Operator.SEQUENCE => name
                  case Operator.PREDICT1 => name
                  case Operator.PREDICT2 => name
                  case Operator.HEATMAP => name
                  case _ => name
                }
                currentNode._query = query
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Query: $query")
                LogMessage(nodeName, s"Current Optimal Path ${optimalPath.mkString(" ")}")
                optimalPath -= optimalPath.last

                //This is the deployment part - we will do it in the next tree iteration:
                //currentNode._value = new String(NFNDataValue(fetchContent(NFNInterest(s"${name}"), ccnApi, 30 seconds).get.data).toDataRepresentation);
                //LogMessage(s"computed ${currentNode._value}\n")
                //currentNode._value = "Temp"
                currentNode._Cprocessed = true
                LogMessage(nodeName, s"CurrentNode: Doing recursion, back to Parent!")

                if (currentNode.parent == null)
                  currentNode
                else
                  processPlacementTree(currentNode.parent, optimalPath)
              }
              else {
                currentNode
              }
            }
          }
        }

        //Here we will get the tree with placement done
        var placementRoot = processPlacementTree(root._root, optimalPath.pathNodes.reverse.toBuffer[String])
        LogMessage(nodeName, s"Operator Placement Completed")

        LogMessage(nodeName, s"Query Deployement Started")
        @tailrec
        def processDeploymentTree(currentNode: Node): Node = {
          if (currentNode._Vprocessed) {
            LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - is Deployed.")
            currentNode
          }
          else {
            if (currentNode.right != null && !currentNode.right._Vprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Right Exists. Process Right")
              processDeploymentTree(currentNode.right)
            }
            else if (currentNode.left != null && !currentNode.left._Vprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Left Exists. Process Left")
              processDeploymentTree(currentNode.left)
            }
            else {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Deploying Operator")

              var name = currentNode._query
              val query = currentNode._type match {
                case Operator.WINDOW => name
                case Operator.FILTER => name//.replace("[Q1]",currentNode.left._value)
                case Operator.JOIN => name.replace("[Q1]", currentNode.left._value).replace("[Q2]", currentNode.right._value)
                case Operator.AGGREGATION => name
                case Operator.SEQUENCE => name
                case Operator.PREDICT1 => name.replace("[Q1]",currentNode.left._value)
                case Operator.PREDICT2 => name//.replace("[Q1]",currentNode.left._value)
                case Operator.HEATMAP => name.replace("[Q1]",currentNode.left._value)
                case _ => name
              }
              if (currentNode._type == Operator.PREDICT1 ||currentNode._type ==  Operator.PREDICT2){

                //predictionInvolved = true
                predictionGranularity = currentNode._parameters(2)
                LogMessage(nodeName,s"Found Prediction Operation and saved parameter predictionGranularity: ${predictionGranularity}")
              }
              if(currentNode._type == Operator.WINDOW){

                startTime = currentNode._parameters(2)
                endTime = currentNode._parameters(3)
                LogMessage(nodeName,s"Found Window Operation and saved parameters startTime: ${startTime}, endTime:${endTime}")
              }

              currentNode._query = query
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Query: $query")
              //currentNode._value = new String(fetchContentRepeatedly(NFNInterest(s"${currentNode._query}"), ccnApi, 30 seconds).get.data);
              //currentNode._value = executeNFNQuery(currentNode._query)

              //Determine the location (name) where this query wwriteOutputFilesill be executed:
              var remoteNodeName = currentNode._query.substring(currentNode._query.indexOf("/node/node") + 6, currentNode._query.indexOf("nfn_service") - 1)

              //In order to simulate network results (which can fail due to node availability or etc - we will comment out actual deployment and introduce a delay of 1.5 seconds which is the average query response time for a distributed network node.
              //This delay is based on the average delay noted during the last 50 runs. Log information is present in NodeA_Log.
              //var intermediateResult = createAndExecCCNQuery(remoteNodeName, currentNode._query, mapping.getPort(remoteNodeName), mapping.getIPbyName(remoteNodeName))
              val intermediateResult = Helpers.executeNFNQuery(currentNode._query,remoteNodeName,ccnApi,60)
              currentNode._value = intermediateResult
              //currentNode._value = "TemporaryDeploymentValue";

              LogMessage(nodeName, s"Deployment result: ${currentNode._value}\n")
              currentNode._Vprocessed = true
              LogMessage(nodeName, s"CurrentNode: Execution completed. Doing recursion, back to Parent!")

              if (currentNode.parent == null)
                currentNode
              else
                processDeploymentTree(currentNode.parent)
            }
          }
        }

        //Here we will get the tree with deployment done
        var deployedRoot = processDeploymentTree(placementRoot)

        var timeOffset_Placement_Deployment = Calendar.getInstance().getTimeInMillis - timeNow_Placement_Deployment

        LogMessage(nodeName, s"Query Deployement Completed")

        //Output is what we send back as the final result:
        output = deployedRoot._value
        output = Helpers.executeInterestQuery(output,nodeName,ccnApi)
        if (output != null && !output.isEmpty)
          output = output.stripSuffix("\n").stripMargin('#')
        else
          output += "No Results!"

        LogMessage(nodeName, s"Query Execution Completed")
        LogMessage(nodeName, s"Query Output = ${output}")
        var outputForPrecision = s"${runID.toString}"
        if(predictionInvolved){
          LogMessage(nodeName,s"Prediction was Involved, calculating Measurements")
          var start = Helpers.parseTime(startTime,"")
          var end = Helpers.parseTime(endTime,"")
          outputForPrecision += MathHelper.getPrecisionRecallAccuracyFMeasure(start,end,predictionGranularity,output.split("\n").toList).toList.toString()
        }
        //Generate Output:
        var timeOffset = Calendar.getInstance().getTimeInMillis - timeNow
        //Format: runID, Time, ResponseTime, OpTreeTime, NodeDiscoveryTime, Placement_DeploymentTime, Path, CumulativePathEnergy, CumulativePathOverhead (BDP):
        var output_for_Run = s"${runID.toString},${runTime.toString},${timeOffset.toString},${timeOffset_OpTreeCreation.toString},${timeOffset_NodeDiscovery.toString},${timeOffset_Placement_Deployment.toString},${selectedPath.toString},${overallPlacementOverhead.toString},${selectedPathEnergy.toString},${selectedPathOverhead.toString}"

        var energyWeightString = ""
        var overheadWeightString = ""
        optimalPath.hopWeights_Energy.foreach {
          case (key, value) => energyWeightString += s"$key-$value "
        }
        energyWeightString.trim()
        optimalPath.hopWeights_BDP.foreach {
          case (key, value) => overheadWeightString += s"$key-$value "
        }
        overheadWeightString.trim()
        //Format: runID, Time, ResponseTime, Path, EnergyWeight, OverheadWeight
        var output_for_AdaptiveWeights = s"${runID.toString},${runTime.toString},${timeOffset.toString},${selectedPath.toString},${energyWeightString.toString},${overheadWeightString.toString}"
        if(!predictionInvolved)
          Helpers.writeOutputFiles(output_for_Run, output_for_AdaptiveWeights,output)
        else
          Helpers.writeOutputFiles(output_for_Run, output_for_AdaptiveWeights,outputForPrecision,output)

        return output

      }

      if (algorithmEnum == 2) {

        var selectedPathDecentral = ""
        var selectedPathEnergyVariance = new mutable.HashMap[String, String]()
        var selectedPathOverheadVariance = new mutable.HashMap[String, String]()
        //2) For this size: Get the path with atleast OpCount - 1 hops and the lowest BDP path:
        //In this case we will try to place an OP on each node directly next to the root.
        //E.g. If node 1 is only connected to node 2. And node 2 has 4 other neighbours, then we will send the query to node 2.
        //So, for all paths, look at the second hop. E.g. Sample paths: 9001-9002, 9001-9002-9003, 9001-9002-9005. Here the second hop is always 9002. Therefore we will send the query to 9002.
        //Else in the case of 9001-9002, 9001-9003-9004, 9001-9005-9006. It means that we have more than 1 node connected to this node. So we could have done the placement on this one.
        //Getting the distinct second hops
        var secondHopNodes = new ArrayBuffer[String]
        for (path <- paths) {
          if (path.pathNodes.length > 1) {
            secondHopNodes += path.pathNodes.reverse(1)
            //The reason why we do not sanitize this and access the array element directly is because we cannot have a Path with 1 node. Hence PathNodes(1) will always contain a node.
            // If this is not the case then this requires a rework of the entire path discovery process. Currently, this is not the case and we are forming proper paths.
          }
        }
        //Get the distinct of the second hops:
        secondHopNodes = secondHopNodes.distinct
        //Remove root from list:
        secondHopNodes -= thisNode
        LogMessage(nodeName, s"Distinct 2nd hop nodes: ${secondHopNodes.mkString(" ")}")

        //Now check if the number of these distinct nodes is equal to or greater than the operator count
        var timeNow_Placement_Deployment = Calendar.getInstance().getTimeInMillis

        if (secondHopNodes.length >= opCount) {
          LogMessage(nodeName, s"Second hop nodes are more than the OP Count. We can explore this node and its neighbors")
          //This node has more network information (more neighboring paths it can explore)
          //We can use this node for placement
          //Select a path with min cumulative path cost with the required number of hops:
          var selectedPath = paths.filter(x => x.hopCount == opCount).minBy(_.cumulativePathCost)

          selectedPathDecentral = selectedPath.pathNodes.mkString(" - ").toString
          //Getting the cumulative path energy and bdp:
          selectedPathEnergy = FormattedOutput.round(FormattedOutput.parseDouble((selectedPath.cumulativePathEnergy.sum/ selectedPath.cumulativePathEnergy.length).toString()), 2)
          selectedPathOverhead = FormattedOutput.round(FormattedOutput.parseDouble((selectedPath.cumulativePathBDP.sum / selectedPath.cumulativePathBDP.length).toString()), 2)

          //Manage the adaptive path weights that changed over time
          selectedPathEnergyVariance = selectedPath.hopWeights_Energy
          selectedPathOverheadVariance = selectedPath.hopWeights_BDP

          overallPlacementOverhead = selectedPath.cumulativePathCost

          //Take this path and place the queries on it:
          //1) For this we will need to process the tree:

          LogMessage(nodeName, s"The selected path is: $selectedPathDecentral")

          LogMessage(nodeName, s"Operator Placement Started")
          @tailrec
          def processPlacementTree(currentNode: Node, optimalPath: mutable.Buffer[String]): Node = {
            if (currentNode._Cprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Placement complete.")
              currentNode
            }
            else {
              if (currentNode.right != null && !currentNode.right._Cprocessed) {
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Right Exists. Process Right")
                processPlacementTree(currentNode.right, optimalPath)
              }
              else if (currentNode.left != null && !currentNode.left._Cprocessed) {
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Left Exists. Process Left")
                processPlacementTree(currentNode.left, optimalPath)
              }
              else {
                if (optimalPath.nonEmpty) {
                  LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Processing Placement")

                  currentNode._executionNodePort = optimalPath.last
                  currentNode._executionNode = mapping.getName(currentNode._executionNodePort)

                  var name = currentNode._query.replace("nodeQuery", currentNode._executionNode)
                  val query = currentNode._type match {
                    case Operator.WINDOW => name
                    case Operator.FILTER => name
                    case Operator.JOIN => name
                    case Operator.AGGREGATION => name
                    case Operator.SEQUENCE => name
                    case Operator.PREDICT1 => name
                    case Operator.PREDICT2 => name
                    case Operator.HEATMAP => name
                    case _ => name
                  }
                  currentNode._query = query
                  LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Query: $query")
                  LogMessage(nodeName, s"Current Optimal Path ${optimalPath.mkString(" ")}")
                  optimalPath -= optimalPath.last

                  //This is the deployment part - we will do it in the next tree iteration:
                  //currentNode._value = new String(NFNDataValue(fetchContent(NFNInterest(s"${name}"), ccnApi, 30 seconds).get.data).toDataRepresentation);
                  //LogMessage(s"computed ${currentNode._value}\n")
                  //currentNode._value = "Temp"
                  currentNode._Cprocessed = true
                  LogMessage(nodeName, s"CurrentNode: Doing recursion, back to Parent!")

                  if (currentNode.parent == null)
                    currentNode
                  else
                    processPlacementTree(currentNode.parent, optimalPath);
                }
                else {
                  currentNode
                }
              }
            }
          }

          //Here we will get the tree with placement done
          var placementRoot = processPlacementTree(root._root, selectedPath.pathNodes.reverse.toBuffer[String])
          LogMessage(nodeName, s"Operator Placement Completed")

          LogMessage(nodeName, s"Query Deployement Started")

          @tailrec
          def processDeploymentTree(currentNode: Node): Node = {
            if (currentNode._Vprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - is Deployed.")
              currentNode
            }
            else {
              if (currentNode.right != null && !currentNode.right._Vprocessed) {
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Right Exists. Process Right")
                processDeploymentTree(currentNode.right)
              }
              else if (currentNode.left != null && !currentNode.left._Vprocessed) {
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Left Exists. Process Left")
                processDeploymentTree(currentNode.left)
              }
              else {
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Deploying Operator")

                var name = currentNode._query
                val query = currentNode._type match {
                  case Operator.WINDOW => name
                  case Operator.FILTER => name//.replace("[Q1]",currentNode.left._value)
                  case Operator.JOIN => name.replace("[Q1]", currentNode.left._value).replace("[Q2]", currentNode.right._value)
                  case Operator.AGGREGATION => name
                  case Operator.SEQUENCE => name
                  case Operator.PREDICT1 => name.replace("[Q1]",currentNode.left._value)
                  case Operator.PREDICT2 => name//.replace("[Q1]",currentNode.left._value)
                  case Operator.HEATMAP => name.replace("[Q1]",currentNode.left._value)
                  case _ => name
                }
                currentNode._query = query
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Query: $query")
                //currentNode._value = new String(fetchContentRepeatedly(NFNInterest(s"${currentNode._query}"), ccnApi, 30 seconds).get.data);
                //currentNode._value = executeNFNQuery(currentNode._query)

                //In order to simulate network results (which can fail due to node availability or etc - we will comment out actual deployment and introduce a delay of 1.5 seconds which is the average query response time for a distributed network node.
                //This delay is based on the average delay noted during the last 50 runs. Log information is present in NodeA_Log.
                //Determine the location (name) where this query will be executed:
                var remoteNodeName = currentNode._query.substring(currentNode._query.indexOf("/node/node") + 6 , currentNode._query.indexOf("nfn_service") - 1);
                var intermediateResult = Helpers.executeNFNQuery(currentNode._query, remoteNodeName,ccnApi,60)

                currentNode._value = intermediateResult

                LogMessage(nodeName, s"Deployment result: ${currentNode._value}")
                currentNode._Vprocessed = true
                LogMessage(nodeName, s"CurrentNode: Execution completed. Doing recursion, back to Parent!")

                if (currentNode.parent == null)
                  currentNode
                else
                  processDeploymentTree(currentNode.parent)
              }
            }
          }

          //Here we will get the tree with complete deployment
          var deployedRoot = processDeploymentTree(placementRoot)

          LogMessage(nodeName, s"Query Deployement Completed")

          //Output is what we send back as the final result. The final result will be the value of the root node.
          output = deployedRoot._value
        }
        else {
          //Issue a new query on the MOST OPTIMAL path and wait for the result:

          var path = paths.filter(x => x.pathNodes.length > 1).minBy(_.cumulativePathCost)
          LogMessage(nodeName, s"Path selected for Decentralized Query: ${path.pathNodes.mkString(" - ")}")
          var optimalPath = path.pathNodes.reverse.toBuffer[String]; //Reverse is needed in order to change 9003 -> 9002 -> 9001 to 9001 -> 9002 -> 9003 etc.
          var _executionNodePort = optimalPath(1) //Once again, we send the query to the second hop from us. I.e. the next hop;
          var _executionNode = mapping.getName(_executionNodePort)
          LogMessage(nodeName, s"No feasible path found. Sending query to: ${_executionNode}/${_executionNodePort}")
          var output = Helpers.executeNFNQuery(s"call 8 /node/${_executionNode}/nfn_service_QueryDecentral '$runID' 'DQ' '${_executionNodePort}' '$clientID' '$query' '$region' '$runTime'",_executionNode,ccnApi,120)

          /*var output = createAndExecCCNQuery(
            _executionNode
            , s"call 8 /node/${_executionNode}/nfn_service_QueryDecentral '$runID' 'DQ' '${_executionNodePort}' '$clientID' '$query' '$region' '$runTime'"
            , _executionNodePort
            , mapping.getIPbyName(_executionNode))*/

        }

        var timeOffset_Placement_Deployment = Calendar.getInstance().getTimeInMillis - timeNow_Placement_Deployment
        output = Helpers.executeInterestQuery(output,nodeName,ccnApi)
        if (output != null && !output.isEmpty)
          output = output.stripSuffix("\n").stripMargin('#')
        else
          output += "No Results!"

        LogMessage(nodeName, s"Query Execution Completed")
        LogMessage(nodeName, s"The Output is ${output}")
        //Generate Output:
        var timeOffset = Calendar.getInstance().getTime.getTime - timeNow
        //Format: runID, Time, ResponseTime, Path, CumulativePathEnergy, CumulativePathOverhead (BDP):


        var output_for_Run = s"${runID.toString},${runTime.toString},${timeOffset.toString},${timeOffset_OpTreeCreation.toString},${timeOffset_NodeDiscovery.toString},${timeOffset_Placement_Deployment.toString},${selectedPathDecentral.toString},${overallPlacementOverhead.toString},${selectedPathEnergy.toString},${selectedPathOverhead.toString}"

        var energyWeightString = ""
        var overheadWeightString = ""
        selectedPathEnergyVariance.foreach {
          case (key, value) => energyWeightString += s"$key-$value "
        }
        energyWeightString.trim()
        selectedPathOverheadVariance.foreach {
          case (key, value) => overheadWeightString += s"$key-$value "
        }
        overheadWeightString.trim()
        var output_for_AdaptiveWeights = s"${runID.toString},${runTime.toString},${timeOffset.toString},${selectedPathDecentral.toString},${energyWeightString.toString},${overheadWeightString.toString}"

        Helpers.writeOutputFiles(output_for_Run, output_for_AdaptiveWeights,output)

        return output
      }

      output
    }

    def getNodeStatus(algorithmEnum: Int, thisNode: String, nodeName: String): ListBuffer[NodeInfo] = {
      LogMessage(nodeName, s"Get Node Status Started - Algorithm Enum => $algorithmEnum")
      //Get all node information:
      val now = Calendar.getInstance()

      var allNodes = new ListBuffer[NodeInfo]()

      if(algorithmEnum == 1){

        //Below file must be present in order to carry out proper placement:
        val bufferedSource = Source.fromFile(Helpers.getNodeInformationPath)
        bufferedSource
          .getLines
          .foreach {line: String =>
            var nodeSplit = line.split("-"); //Data is always in the form of: nodeX-Port-IP
          var name = s"/${nodeSplit(1)}/" + now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE)
            //Get content from network
            var mapping = new NodeMapping()
            //val nameOfContentWithoutPrefixToAdd = CCNName(new String(name).split("/").tail: _*)
            val intermediateResult = Helpers.executeNFNQuery(s"(call 2 /node/${nodeSplit(0)}/nfn_service_GetContent '${name}')",nodeSplit(0),ccnApi,15)

            if (intermediateResult != "") {
              var ni = new NodeInfo(intermediateResult)
              LogMessage(nodeName, s"Node Added: ${ni.NI_NodeName}")
              allNodes += ni
            }
          }
        bufferedSource.close

        LogMessage(nodeName, s"Get Node Status Completed")
        allNodes
      }
      else if(algorithmEnum == 2){
        val kHops = Source.fromFile(Helpers.getDecentralizedKHops)
        var K = 0
        kHops.getLines().foreach {
          line: String =>
            K = FormattedOutput.toInt(line)
        }

        LogMessage(nodeName, s"Performing Decentralized lookup with $K hops")

        //Below file must be present in order to carry out proper placement:
        val bufferedSource = Source.fromFile(Helpers.getDecentralizedNodeInformation(nodeName))
        bufferedSource
          .getLines
          .foreach { line: String =>
            var nodeSplit = line.split("-"); //Data is always in the form of: nodeX-Port-Hops-IP
          var name = s"/${nodeSplit(1)}/" + now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE)

            //Only get information for K hops
            if (FormattedOutput.toInt(nodeSplit(2)) <= K ) {
              //Get content from network

              var mapping = new NodeMapping()

              val intermediateResult = Helpers.executeNFNQuery(s"(call 2 /node/${nodeSplit(0)}/nfn_service_GetContent '${name}')",nodeSplit(0),ccnApi,15)

              if (intermediateResult != "") {
                var ni = new NodeInfo(intermediateResult)
                LogMessage(nodeName, s"Node Added: ${ni.NI_NodeName}")
                allNodes += ni
              }
            }
          }
        bufferedSource.close

        LogMessage(nodeName, s"Get Node Status Completed")
        return allNodes
      }

      allNodes
    }

    def buildPaths(nodeName: String, rootNode: String, nodes: ListBuffer[NodeInfo]): ListBuffer[Paths] = {
      LogMessage(nodeName, s"Building Paths Started")
      var paths = new ListBuffer[Paths]
      var root = nodes.filter(x => x.NI_NodeName == rootNode).head
      nodes -= root
      nodes.insert(0, root)

      var endOfPath = false

      var currentRoot = root.NI_NodeName

      var traversedNodes = new ListBuffer[NodeInfo]
      traversedNodes += root

      var traversalNodes:ListBuffer[NodeInfo] = getTraversalNodes(nodeName, root, nodes, new ListBuffer[NodeInfo], traversedNodes)
      LogMessage(nodeName, s"getTraversalNodes -> on Root")

      var firstHopList = new ListBuffer[HopObject]
      var firstpath = new HopObject()
      firstpath.hopName = root.NI_NodeName
      firstpath.hopLatency += 0.0
      firstpath.previousHop = null
      LogMessage(nodeName, s"oneStepTraverse ROOT added -> NULL -> ${firstpath.hopName}")
      firstHopList += firstpath

      //All new paths will now be in hopInfo:
      LogMessage(nodeName, s"oneStepTraverse -> on Root")
      var hopInfo = oneStepTraverse(nodeName, root, root, firstHopList)

      while(traversalNodes.nonEmpty){
        var next = traversalNodes.head
        LogMessage(nodeName, s"While -> Next -> ${next.NI_NodeName}")

        LogMessage(nodeName, s"While -> Retrieve HopInfo\n")
        hopInfo.appendAll(oneStepTraverse(nodeName, root, next, hopInfo))

        LogMessage(nodeName, s"While -> Retrieve other traversal nodes")
        traversalNodes.appendAll(getTraversalNodes(nodeName, next, nodes, traversalNodes, traversedNodes))

        LogMessage(nodeName, s"While -> remove current node from traversal")
        traversalNodes = traversalNodes.tail

        traversedNodes += next
      }

      //Remove duplicate paths - since we traverse ALL possible HOPS and add the path and it's head:
      hopInfo = hopInfo.distinct

      //Get the utility function data:
      var multiObjFunction = Helpers.getMultiObjectiveFunctionMetrics

      var energyWeight = multiObjFunction(0)
      var bdpWeight = multiObjFunction(1)

      //Initialize adaptive weight assignment for each hop in the path:
      var previousEnergyWeight = 0.0
      var previousBDPWeight = 0.0

      //Recursively print the paths AND store in a path list:
      @tailrec
      def checkPath(hops:ListBuffer[HopObject]): String = {
        if(hops.isEmpty) {
          LogMessage(nodeName, s"Path Finished!")
          return "ok"
        }
        else {
          var current = hops.head
          LogMessage(nodeName, s"checkPath - Current => ${current.hopName}")
          var pathCost:Double = 0.0
          var pathString = ""
          var hopCount:Int = 0
          var pathNodes = new ListBuffer[String]

          var cumulativePathEnergy = new ListBuffer[Double]
          var cumulativePathBDP = new ListBuffer[Double]

          var hopWeights_Energy = scala.collection.mutable.HashMap[String, String]()
          var hopWeights_BDP = scala.collection.mutable.HashMap[String, String]()

          var lastHopEnergy = 0.0
          var lastHopBDP = 0.0

          while(current.previousHop != null) {
            LogMessage(nodeName, s"Current.Prev is not null - This node has a parent node => Path is: Parent -> Node = ${current.previousHop.hopName} -> ${current.hopName}");
            if (current.previousHop.previousHop == null
              || (current.previousHop.previousHop != null && (current.hopName != current.previousHop.previousHop.hopName))
            ) {
              var hopBDP: Double = 0.0
              var nodePower = nodes.filter(x => x.NI_NodeName == current.hopName)
              if (nodePower != null && nodePower.nonEmpty) {
                LogMessage(nodeName, s"Since we have a match - we will apply adaptive weightage")
                //Calculating the utility function for each hop:
                //Link cost = (Energy * Energy Weight) + (BDP * BDP Weight)
                //Adaptive Hop Weight assignment. Vary the adaptive Weights for all hops based on each hop change in Energy and BDP values.
                //Here, we initially start with 0.5,0.5 for both energy and bdp. We use Additive Increase, Additive Decrease to change the weights based on network conditions.
                LogMessage(nodeName, s"Previous Weight values were: Energy=${previousBDPWeight.toString} and BDP=${previousEnergyWeight.toString} ")
                if(previousBDPWeight == 0.0 && previousEnergyWeight == 0.0) {
                  LogMessage(nodeName, s"This is the first hop for adaptive weight application")
                  //Use standard 0.5,0.5
                  hopBDP = FormattedOutput.round(((nodePower.head.NI_Battery * energyWeight) + (current.hopLatency * bdpWeight)), 2)
                  previousBDPWeight = bdpWeight
                  previousEnergyWeight = energyWeight

                  lastHopBDP = current.hopLatency
                  lastHopEnergy = nodePower.head.NI_Battery
                  //By this time, we have the values of the weights and the hop metrics
                }
                else{
                  LogMessage(nodeName, s"This is a subsequent hop/s for adaptive weight application")
                  //This signifies that this is not the first hop in the path and now we should look at the previous hop values to determine whether
                  //we will increase or decrease a weight metric:
                  if(nodePower.head.NI_Battery >= lastHopEnergy && current.hopLatency <= lastHopBDP){
                    //Additive increase on energy and additive decrease on bdp:
                    if(previousEnergyWeight < 1.00 && previousBDPWeight > 0.00) {
                      previousEnergyWeight = FormattedOutput.round(previousEnergyWeight + 0.1, 2)
                      previousBDPWeight = FormattedOutput.round(previousBDPWeight - 0.1, 2) //Multiplicative Decrease: /2 | Additive Decrease: - 0.1
                    }
                  }
                  //else check the other way around - if bdp is more than the last hop and energy is less.
                  else  if(nodePower.head.NI_Battery <= lastHopEnergy && current.hopLatency >= lastHopBDP){
                    //Additive increase on energy and additive decrease on bdp:
                    if(previousEnergyWeight > 0.00 && previousBDPWeight < 1.00) {
                      previousEnergyWeight = FormattedOutput.round(previousEnergyWeight - 0.1, 2) //Multiplicative Decrease: /2 | Additive Decrease: - 0.1
                      previousBDPWeight = FormattedOutput.round(previousBDPWeight + 0.1, 2)
                    }
                  }

                  //In all other cases, we will not vary these weights since they have to move up or down together.
                  //Now we can assign the new hop BDP based on the new weights:
                  hopBDP = FormattedOutput.round(((nodePower.head.NI_Battery * previousEnergyWeight) + (current.hopLatency * previousBDPWeight)), 2)

                  //Set the metrics for this hop so that it can be used in the next hop:
                  lastHopBDP = current.hopLatency
                  lastHopEnergy = nodePower.head.NI_Battery
                }
                //Adding hop link cost in the overall path cost:
                hopWeights_Energy += s"${current.hopName}" -> s"${previousEnergyWeight.toString}"
                hopWeights_BDP+= s"${current.hopName}" -> s"${previousBDPWeight.toString}"

                cumulativePathEnergy += lastHopEnergy
                cumulativePathBDP += lastHopBDP

                pathCost += hopBDP

              }
              else {
                LogMessage(nodeName, s"Hop ${current.hopName} information not found in list of nodes")
              }
              pathString = s" --(BDP: $hopBDP)--> " + current.hopName + " " + pathString
              //Adding a hop
              hopCount = hopCount + 1

              pathNodes += current.hopName
            }

            current = current.previousHop
          }
          //Just for correct visual representation: Not utilized since the latency is always 0 in this case.
          if(current.previousHop == null){
            pathString = s"NULL --(${current.hopLatency})--> " + current.hopName + " " + pathString
            //This is the root node:
            hopCount = hopCount+1
            pathNodes += current.hopName
          }

          var path = new Paths()
          path.hopStringRepresentation = pathString
          path.hopCount = hopCount
          path.cumulativePathCost = pathCost
          path.pathNodes = pathNodes.toArray
          path.cumulativePathBDP = cumulativePathBDP.toArray
          path.cumulativePathEnergy = cumulativePathEnergy.toArray
          path.hopWeights_Energy = hopWeights_Energy
          path.hopWeights_BDP = hopWeights_BDP
          paths+=path

          LogMessage(nodeName, s"Path: $pathString\n")

          checkPath(hops.tail)
        }
      }
      checkPath(hopInfo)

      //Remove any duplicate paths that were created due to hop-linking:
      paths = paths.distinct

      LogMessage(nodeName, s"Building Paths Completed")
      paths
    }

    def getTraversalNodes(nodeName: String, cNode: NodeInfo, nodes:ListBuffer[NodeInfo], traversalNodes: ListBuffer[NodeInfo], traversedNodes: ListBuffer[NodeInfo]): ListBuffer[NodeInfo] = {
      var retNodes = new ListBuffer[NodeInfo]()
      for (latency <- cNode.NI_Latency) {
        var node = nodes.filter(x => x.NI_NodeName == latency.Lat_Node)

        if (node != null && node.nonEmpty) {
          var _node = nodes.filter(x => x.NI_NodeName == latency.Lat_Node).head
          if (!traversalNodes.contains(_node) && !traversedNodes.contains(_node)) {
            LogMessage(nodeName, s"add new Node in traversal List -> ${_node.NI_NodeName}")
            retNodes += _node
          }
        }

      }
      retNodes
    }

    def oneStepTraverse(nodeName: String, myRoot: NodeInfo, cNode: NodeInfo, cPath: ListBuffer[HopObject]): ListBuffer[HopObject] = {
      for(latency <- cNode.NI_Latency) {
        LogMessage(nodeName, s"oneStepTraverse -> ${latency.Lat_Node} - ${latency.Lat_Latency}")

        //find root node in CPath:
        var pathRoot = cPath.filter(x=> x.hopName == cNode.NI_NodeName)
        if(pathRoot!= null){
          var path = new HopObject()
          path.hopName = latency.Lat_Node
          path.hopLatency += latency.Lat_Latency
          path.previousHop = pathRoot.head; //get the first head
          LogMessage(nodeName, s"oneStepTraverse hop added -> ${path.previousHop.hopName} -> ${path.hopName}")
          cPath += path
        }
      }
      cPath
    }

    NFNStringValue(
      args match {
        case Seq(algorithm: NFNStringValue, runID: NFNStringValue, sourceOfQuery: NFNStringValue, clientID: NFNStringValue, query: NFNStringValue, region: NFNStringValue, timestamp: NFNStringValue) => processQuery(algorithm.str, runID.str, sourceOfQuery.str, clientID.str, query.str, region.str, timestamp.str)
        case _ =>
          throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
      }
    )
  }
}
