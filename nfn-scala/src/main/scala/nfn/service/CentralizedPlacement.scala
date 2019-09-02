package nfn.service
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

import SACEPICN._
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

//Place the query on a single node!
class CentralizedPlacement() extends NFNService {

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {
    def processQuery(algorithm: String, processing: String, runID: String, sourceOfQuery: String, clientID: String, query: String, region: String, timestamp: String): String = {
      //Run output creation:
      var runTime = s"${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance.getTime)}"
      var output = ""
      var algorithmEnum = 0
      var centralizedProc = false

      //Get current node from interest:
      var nodeInfo = interestName.cmps.mkString(" ")
      var nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1)
      LogMessage(nodeName, s"Query Execution Started")

      if (algorithm.toLowerCase == "centralized") algorithmEnum = 1 //1 = Centralized
      else if (algorithm.toLowerCase == "decentralized") algorithmEnum = 2 //2 = Decentralized
      else "Invalid Algorithm name. Accepted values are 'Centralized' or 'Decentralized'"

      if (processing.toLowerCase == "centralized") centralizedProc = true
      else "Pass right arguments for distributed processing, processing parameter must not be set!"

      if (algorithmEnum == 1 && processing.toLowerCase == "centralized") {
        LogMessage(nodeName, s"Right algorithm found!")
        /* TODO: replace all the disk IO operations to a static configuration file
        //Set Mapping:
        var mapping = new NodeMapping()
        //Get thisNodePort:
        val thisNode = mapping.getPort(nodeName)
        */
        //We use static config instead of reading it from the file which might reduce latency because of disk IO
        val thisNode = "9001"

        //Log Query for interval based trigger:
        if (Helpers.save_to_QueryStore(algorithm, processing, runID, sourceOfQuery, thisNode, clientID, query, region, timestamp)) {
          LogMessage(nodeName, s"Query appended to Query Store - Source $sourceOfQuery")
        } else
          LogMessage(nodeName, s"Query NOT appended to Query Store - Source $sourceOfQuery")
        //from here the query processing starts
        var timeNow = Calendar.getInstance().getTimeInMillis
        var et = new OperatorTree()
        LogMessage(nodeName, s"Operator Tree creation Started")

        var timeNow_OpTreeCreation = Calendar.getInstance().getTimeInMillis
        //create operator graph
        var root = et.createOperatorTree(query)

        var timeOffset_OpTreeCreation = Calendar.getInstance().getTimeInMillis - timeNow_OpTreeCreation

        LogMessage(nodeName, s"Operator Tree creation Completed")

        var timeNow_Placement_Deployment = Calendar.getInstance().getTimeInMillis
        LogMessage(nodeName, s"Operator Placement Started")
        var timeNow_executeNFNquery = 0L

        //Process query centrally
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
              currentNode._executionNodePort = thisNode
              currentNode._executionNode = "nodeA"
              //currentNode._executionNodePort = mapping.getPort(nodeName)
              //currentNode._executionNode = mapping.getName(currentNode._executionNodePort)

              var name = currentNode._query.replace("nodeQuery", currentNode._executionNode)
              //var name = currentNode._query
              val query = currentNode._type match {
                case Operator.WINDOW => name
                case Operator.FILTER => name.replace("[Q1]",currentNode.left._value)
                case Operator.JOIN => name.replace("[Q1]", currentNode.left._value).replace("[Q2]", currentNode.right._value)
                case Operator.AGGREGATION => name
                case Operator.SEQUENCE => name
                case Operator.PREDICT1 => name.replace("[Q1]",currentNode.left._value)
                case Operator.PREDICT2 => name.replace("[Q1]",currentNode.left._value)
                case Operator.HEATMAP => name.replace("[Q1]",currentNode.left._value)
                case _ => name
              }
              currentNode._query = query
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Query: $query")

              //Determine the location (name) where this query wwriteOutputFilesill be executed:
              var remoteNodeName = currentNode._query.substring(currentNode._query.indexOf("/node/node") + 6, currentNode._query.indexOf("nfn_service") - 1)
              timeNow_executeNFNquery = Calendar.getInstance().getTimeInMillis
              val intermediateResult = Helpers.executeNFNQuery(currentNode._query,remoteNodeName,ccnApi,60)
              currentNode._value = intermediateResult

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
        var deployedRoot = processDeploymentTree(root._root)

        var timeOffset_Placement_Deployment = Calendar.getInstance().getTimeInMillis - timeNow_Placement_Deployment
        LogMessage(nodeName, s"Operator Placement Completed")

        //Output is what we send back as the final result:
        output = deployedRoot._value
        output = Helpers.executeInterestQuery(output,nodeName,ccnApi)
        if (output != null && !output.isEmpty)
          output = output.stripSuffix("\n").stripMargin('#')
        else
          output += "No Results!"
        val timeOffset_executeNFNquery = Calendar.getInstance().getTimeInMillis - timeNow_executeNFNquery
        LogMessage(nodeName, s"Query Execution Completed")

        //Generate Output:
        var timeOffset = Calendar.getInstance().getTimeInMillis - timeNow
        //Format: runID, Time, ResponseTime, OpTreeTime, NodeDiscoveryTime, Placement_DeploymentTime, Output
        var output_for_Run = s"${runID.toString},${runTime.toString},${timeOffset.toString},${timeOffset_OpTreeCreation.toString},${timeOffset_Placement_Deployment.toString}, " +
          s"${timeOffset_executeNFNquery}"
        Helpers.writeOutputFiles(output_for_Run,output)


      }
      else LogMessage(nodeName, s"Call the right Query service for distributed processing!")
      output
    }


    NFNStringValue(
      args match {
        case Seq(algorithm: NFNStringValue, processing: NFNStringValue, runID: NFNStringValue, sourceOfQuery: NFNStringValue, clientID: NFNStringValue, query: NFNStringValue, region: NFNStringValue, timestamp: NFNStringValue) => processQuery(algorithm.str, processing.str, runID.str, sourceOfQuery.str, clientID.str, query.str, region.str, timestamp.str)
        case _ =>
          throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
      }
    )

  }
}
