package nfn.service
/**
  * Created by Ali on 06.02.18.
  * This is the random adaptive approach
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

import SACEPICN.NodeMapping
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

//Added for CCN Command Execution:
import sys.process._
import filterAccess.tools.ConfigReader._
import config.StaticConfig

class QueryRandom() extends NFNService {

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {

    val sacepicnEnv = StaticConfig.systemPath
    //ClientID: Client who requested the query
    //Query: The query to break and process
    //Region: User Region to hit for sensors
    //Timestamp: Used to distinguish the time of arrival for the queries
    def processQuery(runID: String, sourceOfQuery:String, thisNode: String, clientID: String, query: String, region: String, timestamp: String): String = {

      //Run output creation:
      var run = runID
      var runTime = s"${new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance.getTime)}"
      var timeNow = Calendar.getInstance().getTimeInMillis()
      var selectedPath = ""
      var selectedPathEnergy = 0.0
      var selectedPathOverhead = 0.0
      var overallPlacementOverhead = 0.0

      //      var selectedPathEnergyVariance = new mutable.HashMap[String, String]()
      //      var selectedPathOverheadVariance = new mutable.HashMap[String, String]()

      //Get current node from interest:
      var nodeInfo = interestName.cmps.mkString(" ");
      var nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6, nodeInfo.indexOf("nfn_service") - 1);
      LogMessage(nodeName, s"Query Execution Started");

      //Set Mapping:
      var mapping = new NodeMapping();
      //Log Query for interval based trigger:
      if (save_to_QueryStore(runID, sourceOfQuery, thisNode, clientID, query, region, timestamp)) {
        LogMessage(nodeName, s"Query appended to Query Store - Source ${sourceOfQuery}");
      } else
        LogMessage(nodeName, s"Query NOT appended to Query Store - Source ${sourceOfQuery}");

      //Initialize ExpressionTree class:

      var et = new OperatorTree();
      //Create node stack or Node tree (current: Tree)
      LogMessage(nodeName, s"Operator Tree creation Started");
      var timeNow_OpTreeCreation = Calendar.getInstance().getTimeInMillis()
      var root = et.createOperatorTree(query);
      var timeOffset_OpTreeCreation = Calendar.getInstance().getTimeInMillis() - timeNow_OpTreeCreation
      LogMessage(nodeName, s"Operator Tree creation Completed");

      //Get current Network Status and Path information:
      var timeNow_NodeDiscovery = Calendar.getInstance().getTimeInMillis()

      var allNodes = getNodeStatus(nodeName);
      var paths = buildPaths(nodeName, thisNode, allNodes);

      var timeOffset_NodeDiscovery = Calendar.getInstance().getTimeInMillis() - timeNow_NodeDiscovery

      LogMessage(nodeName, s"Checking paths:");
      for (path <- paths) {
        LogMessage(nodeName, s"${path.pathNodes.reverse.mkString(" ") + " - BDP: " + path.cumulativePathCost + " - Hops: " + path.hopCount}");
      }

      //Now that we have all the paths we need: Place the queries on these paths:
      //1) Find the number of operators in the query:
      var opCount = root._stackSize;
      //2) For this size: Get the path matching the number of operators and the lowest BDP path:
      var optimalPaths = paths.filter(x => x.hopCount == opCount);

      //Randomize the path selection. Take one of the paths that has a hop count equal to our operator count.
      var optimalPath = new Paths()
      if (optimalPaths.length > 0)
        optimalPath = (scala.util.Random.shuffle(optimalPaths) take 1).head;

      if (optimalPath.pathNodes != null && optimalPath.pathNodes.length > 0) {
        //A random path has been found that can be used to carry out the placement.

        selectedPath = optimalPath.pathNodes.mkString("-").toString()
        //Getting the cumulative path energy and bdp:
        selectedPathEnergy = FormattedOutput.round(FormattedOutput.parseDouble((optimalPath.cumulativePathEnergy.reduceLeft[Double](_ + _) / optimalPath.cumulativePathEnergy.length).toString()), 2)
        selectedPathOverhead = FormattedOutput.round(FormattedOutput.parseDouble((optimalPath.cumulativePathBDP.reduceLeft[Double](_ + _) / optimalPath.cumulativePathBDP.length).toString()), 2)

        overallPlacementOverhead = optimalPath.cumulativePathCost

        //Manage the adaptive path weights that changed over time
        LogMessage(nodeName, s"Checking optimal path energy weights:");
        for (weight <- optimalPath.hopWeights_Energy) {
          LogMessage(nodeName, s"${weight}");
        }
        LogMessage(nodeName, s"Checking optimal path BDP weights:");
        for (weight <- optimalPath.hopWeights_BDP) {
          LogMessage(nodeName, s"${weight}");
        }

        //Take this path and place the queries on it:
        //1) For this we will need to process the tree:

        var timeNow_Placement_Deployment = Calendar.getInstance().getTimeInMillis()

        LogMessage(nodeName, s"Operator Placement Started");
        @tailrec
        def processPlacementTree(currentNode: Node, optimalPath: mutable.Buffer[String]): Node = {
          if (currentNode._Cprocessed) {
            LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Placement complete.")
            return currentNode;
          }
          else {
            if (currentNode.right != null && !currentNode.right._Cprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Right Exists. Process Right")
              processPlacementTree(currentNode.right, optimalPath);
            }
            else if (currentNode.left != null && !currentNode.left._Cprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Left Exists. Process Left")
              processPlacementTree(currentNode.left, optimalPath);
            }
            else {
              if (!optimalPath.isEmpty) {
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Processing Placement")

                currentNode._executionNodePort = optimalPath.last;
                currentNode._executionNode = mapping.getName(currentNode._executionNodePort);

                var name = currentNode._query.replace("nodeQuery", currentNode._executionNode);
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
                currentNode._query = query;
                LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Query: ${query}")
                LogMessage(nodeName, s"Current Optimal Path ${optimalPath.mkString(" ")}")
                optimalPath -= optimalPath.last;

                //This is the deployment part - we will do it in the next tree iteration:
                //currentNode._value = new String(NFNDataValue(fetchContent(NFNInterest(s"${name}"), ccnApi, 30 seconds).get.data).toDataRepresentation);
                //LogMessage(s"computed ${currentNode._value}\n")
                //currentNode._value = "Temp"
                currentNode._Cprocessed = true;
                LogMessage(nodeName, s"CurrentNode: Doing recursion, back to Parent!")

                if (currentNode.parent == null)
                  return currentNode;
                else
                  processPlacementTree(currentNode.parent, optimalPath);
              }
              else {
                return currentNode;
              }
            }
          }
        }

        //Here we will get the tree with placement done
        var placementRoot = processPlacementTree(root._root, optimalPath.pathNodes.reverse.toBuffer[String]);
        LogMessage(nodeName, s"Operator Placement Completed");

        LogMessage(nodeName, s"Query Deployement Started");
        @tailrec
        def processDeploymentTree(currentNode: Node): Node = {
          if (currentNode._Vprocessed) {
            LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - is Deployed.")
            return currentNode;
          }
          else {
            if (currentNode.right != null && !currentNode.right._Vprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Right Exists. Process Right")
              processDeploymentTree(currentNode.right);
            }
            else if (currentNode.left != null && !currentNode.left._Vprocessed) {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Left Exists. Process Left")
              processDeploymentTree(currentNode.left);
            }
            else {
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Deploying Operator")

              var name = currentNode._query;
              val query = currentNode._type match {
                case Operator.WINDOW => name
                case Operator.FILTER => name
                case Operator.JOIN => name.replace("[Q1]", currentNode.left._value).replace("[Q2]", currentNode.right._value)
                case Operator.AGGREGATION => name
                case Operator.SEQUENCE => name
                case Operator.PREDICT1 => name.replace("[Q]",currentNode.left._value)
                case Operator.PREDICT2 => name
                case Operator.HEATMAP => name.replace("[Q]",currentNode.left._value)
                case _ => name
              }
              currentNode._query = query;
              LogMessage(nodeName, s"CurrentNode: ${currentNode._type} - Query: ${query}")
              //currentNode._value = new String(fetchContentRepeatedly(NFNInterest(s"${currentNode._query}"), ccnApi, 30 seconds).get.data);
              //currentNode._value = executeNFNQuery(currentNode._query)

              //Determine the location (name) where this query wwriteOutputFilesill be executed:
              var remoteNodeName = currentNode._query.substring(currentNode._query.indexOf("/node/node") + 6, currentNode._query.indexOf("nfn_service") - 1);

              //In order to simulate network results (which can fail due to node availability or etc - we will comment out actual deployment and introduce a delay of 1.5 seconds which is the average query response time for a distributed network node.
              //This delay is based on the average delay noted during the last 50 runs. Log information is present in NodeA_Log.
              // var intermediateResult = createAndExecCCNQuery(remoteNodeName, currentNode._query, mapping.getPort(remoteNodeName), mapping.getIPbyName(remoteNodeName))
              //currentNode._value = intermediateResult;
              currentNode._value = "TemporaryDeploymentValue";

              LogMessage(nodeName, s"Deployment result: ${currentNode._value}\n")
              currentNode._Vprocessed = true;
              LogMessage(nodeName, s"CurrentNode: Execution completed. Doing recursion, back to Parent!")

              if (currentNode.parent == null)
                return currentNode;
              else
                processDeploymentTree(currentNode.parent);
            }
          }
        }

        //Here we will get the tree with deployment done
        var deployedRoot = processDeploymentTree(placementRoot);

        var timeOffset_Placement_Deployment = (Calendar.getInstance().getTimeInMillis() - timeNow_Placement_Deployment)

        LogMessage(nodeName, s"Query Deployement Completed");

        //Output is what we send back as the final result:
        var output = "";
        output = deployedRoot._value;

        if (output != null && !output.isEmpty)
          output = output.stripSuffix("\n").stripMargin('#');
        else
          output += "No Results!"

        LogMessage(nodeName, s"Query Execution Completed");

        //Generate Output:
        var timeOffset = Calendar.getInstance().getTimeInMillis() - timeNow
        //Format: runID, Time, ResponseTime, OpTreeTime, NodeDiscoveryTime, Placement_DeploymentTime, Path, CumulativePathEnergy, CumulativePathOverhead (BDP):
        var output_for_Run = s"${runID.toString},${runTime.toString},${timeOffset.toString},${timeOffset_OpTreeCreation.toString},${timeOffset_NodeDiscovery.toString},${timeOffset_Placement_Deployment.toString},${selectedPath.toString},${overallPlacementOverhead.toString},${selectedPathEnergy.toString},${selectedPathOverhead.toString}";

        var energyWeightString = ""
        var overheadWeightString = ""
        optimalPath.hopWeights_Energy.foreach {
          case (key, value) => energyWeightString += s"$key-$value "
        }
        energyWeightString.trim();
        optimalPath.hopWeights_BDP.foreach {
          case (key, value) => overheadWeightString += s"$key-$value "
        }
        overheadWeightString.trim();
        //Format: runID, Time, ResponseTime, Path, EnergyWeight, OverheadWeight
        var output_for_AdaptiveWeights = s"${runID.toString},${runTime.toString},${timeOffset.toString},${selectedPath.toString},${energyWeightString.toString},${overheadWeightString.toString}";

        writeOutputFiles(output_for_Run, output_for_AdaptiveWeights)
        return output;
      }
      else{
        var output_for_Run = "No suitable paths found for placement"
        writeOutputFiles(output_for_Run, output_for_Run)
        return output_for_Run
      }

      return "No Result!"
    }

    def getNodeStatus(nodeName: String): ListBuffer[NodeInfo] = {
      LogMessage(nodeName, s"Get Node Status Started");
      //Get all node information:
      val now = Calendar.getInstance()

      var allNodes = new ListBuffer[NodeInfo]();
      //Below file must be present in order to carry out proper placement:
      val bufferedSource = Source.fromFile(s"$sacepicnEnv/nodeData/nodeInformation");
      bufferedSource
        .getLines
        .foreach {line: String =>
          var nodeSplit = line.split("-"); //Data is always in the form of: nodeX/90XX
        var name = s"/${nodeSplit(1)}/" + now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE);
          //Get content from network
          //Execute NFN Query to get the latest state from Compute Server cache on the remote node (this is slower)
          //var intermediateResult = new String(fetchContentRepeatedly(NFNInterest(s"(call 2 /node/${nodeSplit(0)}/nfn_service_GetContent '${name}')"), ccnApi, 10 seconds).get.data)

          //var intermediateResult = executeNFNQuery(s"call 2 /node/${nodeSplit(0)}/nfn_service_GetContent '${name}')")
          var mapping = new NodeMapping()
          var intermediateResult = createAndExecCCNQuery(nodeSplit(0), s"call 2 /node/${nodeSplit(0)}/nfn_service_GetContent '${name}')", mapping.getPort(nodeSplit(0)), mapping.getIPbyName(nodeSplit(0)))

          if (intermediateResult != "") {
            var ni = new NodeInfo(intermediateResult);
            LogMessage(nodeName, s"Node Added: ${ni.NI_NodeName}")
            allNodes += ni
          }
        }
      bufferedSource.close

      LogMessage(nodeName, s"Get Node Status Completed")
      return allNodes
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

      while(!traversalNodes.isEmpty){
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
      var multiObjFunction = getMultiObjectiveFunctionMetrics()

      var energyWeight = multiObjFunction(0)
      var bdpWeight = multiObjFunction(1)

      //Initialize adaptive weight assignment for each hop in the path:
      var previousEnergyWeight = 0.0
      var previousBDPWeight = 0.0

      //Recursively print the paths AND store in a path list:
      @tailrec
      def checkPath(hops:ListBuffer[HopObject]): String = {
        if(hops.isEmpty)
        {LogMessage(nodeName, s"Path Finished!")
          return "ok";}
        else {
          var current = hops.head
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
            if (current.previousHop.previousHop == null
              || (current.previousHop.previousHop != null && (current.hopName != current.previousHop.previousHop.hopName))
            ) {
              var hopBDP: Double = 0.0
              var nodePower = nodes.filter(x => x.NI_NodeName == current.hopName)
              if (nodePower != null) {
                //Calculating the utility function for each hop:
                //Link cost = (Energy * Energy Weight) + (BDP * BDP Weight)
                //Adaptive Hop Weight assignment. Vary the adaptive Weights for all hops based on each hop change in Energy and BDP values.
                //Here, we initially start with 0.5,0.5 for both energy and bdp. We use Additive Increase, Additive Decrease to change the weights based on network conditions.
                if(previousBDPWeight == 0.0 && previousEnergyWeight == 0.0) {
                  //Use standard 0.5,0.5
                  hopBDP = FormattedOutput.round(((nodePower.head.NI_Battery * energyWeight) + (current.hopLatency * bdpWeight)), 2)
                  previousBDPWeight = bdpWeight
                  previousEnergyWeight = energyWeight

                  lastHopBDP = current.hopLatency
                  lastHopEnergy = nodePower.head.NI_Battery
                  //By this time, we have the values of the weights and the hop metrics
                }
                else{
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
                hopWeights_Energy += s"${current.hopName}" -> s"${previousEnergyWeight.toString()}"
                hopWeights_BDP+= s"${current.hopName}" -> s"${previousBDPWeight.toString()}"

                cumulativePathEnergy += lastHopEnergy
                cumulativePathBDP += lastHopBDP

                pathCost += hopBDP;

              }
              pathString = s" --(BDP: ${hopBDP})--> " + current.hopName + " " + pathString;
              //Adding a hop
              hopCount = hopCount + 1;

              pathNodes += current.hopName;
            }

            current = current.previousHop;
          }
          //Just for correct visual representation: Not utilized since the latency is always 0 in this case.
          if(current.previousHop == null){
            pathString = s"NULL --(${current.hopLatency})--> " + current.hopName + " " + pathString;
            //This is the root node:
            hopCount = hopCount+1;
            pathNodes += current.hopName;
          }

          var path = new Paths();
          path.hopStringRepresentation = pathString;
          path.hopCount = hopCount;
          path.cumulativePathCost = pathCost;
          path.pathNodes = pathNodes.toArray;
          path.cumulativePathBDP = cumulativePathBDP.toArray;
          path.cumulativePathEnergy = cumulativePathEnergy.toArray
          path.hopWeights_Energy = hopWeights_Energy
          path.hopWeights_BDP = hopWeights_BDP
          paths+=path;

          LogMessage(nodeName, s"Path: ${pathString}\n");

          checkPath(hops.tail);
        }
      }
      checkPath(hopInfo);

      //Store new metrics for upcoming runs:
      //      if(previousEnergyWeight != 0.0 && previousBDPWeight != 0.0)
      //        writeMetricsToStore(previousEnergyWeight.toString, previousBDPWeight.toString)

      //Remove any duplicate paths that were created due to hop-linking:
      paths = paths.distinct;

      LogMessage(nodeName, s"Building Paths Completed");
      return paths;
    }

    def getTraversalNodes(nodeName: String, cNode: NodeInfo, nodes:ListBuffer[NodeInfo], traversalNodes: ListBuffer[NodeInfo], traversedNodes: ListBuffer[NodeInfo]): ListBuffer[NodeInfo] = {
      var retNodes = new ListBuffer[NodeInfo]();
      for (latency <- cNode.NI_Latency) {
        var node = nodes.filter(x => (x.NI_NodeName == latency.Lat_Node)).head;
        if (!traversalNodes.contains(node) && !traversedNodes.contains(node)) {
          LogMessage(nodeName, s"add new Node in traversal List -> ${node.NI_NodeName}");
          retNodes += node;
        }
      }
      return retNodes;
    }

    def oneStepTraverse(nodeName: String, myRoot: NodeInfo, cNode: NodeInfo, cPath: ListBuffer[HopObject]): ListBuffer[HopObject] = {
      for(latency <- cNode.NI_Latency) {
        LogMessage(nodeName, s"oneStepTraverse -> ${latency.Lat_Node} - ${latency.Lat_Latency}");

        //find root node in CPath:
        var pathRoot = cPath.filter(x=> x.hopName == cNode.NI_NodeName);
        if(pathRoot!= null){
          var path = new HopObject();
          path.hopName = latency.Lat_Node;
          path.hopLatency += (latency.Lat_Latency);
          path.previousHop = pathRoot.head; //get the first head
          LogMessage(nodeName, s"oneStepTraverse hop added -> ${path.previousHop.hopName} -> ${path.hopName}");
          cPath += path;
        }
      }
      return cPath;
    }

    def getMultiObjectiveFunctionMetrics(): Array[Double] = {
      val bufferedSource = Source.fromFile(s"$sacepicnEnv/nodeData/placementUtilityFunction")
      var returnData = new Array[Double](2); //0.8|0.2 (ENERGY|BANDWIDTH.DELAY.PRODUCT)
      //To ensure that we always have utility function. Else, we get it from the file
      returnData(0) = 0.5;
      returnData(1) = 0.5

      //To ensure that we get a E|BDP value from the file, we iterate over all lines and check the last line in the config file.
      bufferedSource
        .getLines
        .foreach { line: String =>
          var lineSplit = line.split("\\|");
          if(lineSplit.length > 1){
            //Load this information in the Array:
            returnData(0) = FormattedOutput.parseDouble(lineSplit(0));
            returnData(1) = FormattedOutput.parseDouble(lineSplit(1));
          }
        }

      bufferedSource.close

      return returnData;
    }


    //Use this if overhead is not an issue. ExecuteQuery normalizes the use of fetchContent to one function:
    def executeInterestQuery(query: CCNName): String = {

      return new String(fetchContentRepeatedly(
        Interest(query),
        ccnApi,
        60 seconds).get.data)
    }
    def executeNFNQuery(query: String): String = {

      return new String(fetchContentRepeatedly(
        NFNInterest(query),
        ccnApi,
        60 seconds).get.data)
    }

    def createAndExecCCNQuery(nodeName: String, query:String, port: String, IP: String): String = {
      //var cmd:String = getValueOrDefault("CCN.NFNPeek", "echo No Result!")

      var cmd = """$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u #IP#/#PORT# -w 10 "" "#QUERY#/NFN""""
      var cmdPacketFormatter = "$CCNL_HOME/bin/ccn-lite-pktdump -f 2"
      //Replace IP PORT and QUERY
      //With this we can run the remote queries on the remote nodes:
      cmd = cmd.replace("#IP#", s"${IP}").replace("#PORT#", s"${port}").replace("#QUERY#", query);
      LogMessage(nodeName, s"Query sent to network node: ${cmd} | ${cmdPacketFormatter}");
      var result = execcmd(cmd, cmdPacketFormatter)
      LogMessage(nodeName, s"Query Result from network node: ${result}");

      if(result.contains("timeout") || result.contains("interest") || result == "")
        result = "No Result!"
      return result.trim().stripLineEnd;
    }

    def execcmd(cmd1: String, cmd2:String): String = {
      val result = Seq("/bin/sh", "-c", s"${cmd1} | ${cmd2}").!!
      return result
    }

    def save_to_QueryStore(runID: String, sourceOfQuery:String, interestOrigin: String, clientID: String, query: String, region: String, timestamp: String): Boolean = {
      //Source is not QueryStore and DecentralizeQuery
      if (sourceOfQuery != "QS" && sourceOfQuery != "DQ") {
        var filename = s"$sacepicnEnv/nodeData/queryStore";
        val file = new File(filename)
        file.getParentFile().mkdirs();
        file.createNewFile()

        //clear the file for old queries
        var w = new PrintWriter(file);
        w.close();

        val pw = new PrintWriter(new FileOutputStream(file, true));

        var now = Calendar.getInstance()
        var q_TimeStamp = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE);

        var queryToStore = s"QID:${clientID}_${q_TimeStamp} ${runID} ${interestOrigin} ${clientID} ${query} ${region} ${timestamp}"
        pw.println(queryToStore)
        pw.close()

        return true
      }

      return false
    }

    def writeOutputFiles(runAnalysis: String, weightVariance: String) = {
      var queryOutput = s"$sacepicnEnv/nodeData/queryOutput";
      var queryWeightVariance = s"$sacepicnEnv/nodeData/queryWeightVariance";
      val file1 = new File(queryOutput)
      val file2 = new File(queryWeightVariance)
      file1.getParentFile().mkdirs();
      file1.createNewFile()
      file2.getParentFile().mkdirs();
      file2.createNewFile()

      val pw1 = new PrintWriter(new FileOutputStream(file1, true));
      val pw2 = new PrintWriter(new FileOutputStream(file2, true));

      pw1.println(runAnalysis)
      pw1.close()
      pw2.println(weightVariance)
      pw2.close()
    }

    def writeMetricsToStore(Energy: String, Overhead: String) = {
      var weights = s"$sacepicnEnv/nodeData/placementUtilityFunction";
      val file1 = new File(weights)
      file1.getParentFile().mkdirs();
      file1.createNewFile()

      val pw1 = new PrintWriter(new FileOutputStream(file1, true));

      var writeText = s"${Energy}\\|${Overhead}"
      pw1.println(writeText)
      pw1.close()
    }

    NFNStringValue(
      args match {
        case Seq(runID: NFNStringValue, sourceOfQuery: NFNStringValue, myNode: NFNStringValue, clientID: NFNStringValue, query: NFNStringValue, region: NFNStringValue, timestamp: NFNStringValue) => processQuery(runID.str, sourceOfQuery.str, myNode.str, clientID.str, query.str, region.str, timestamp.str)
        case _ =>
          throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
      }
    )
  }
}
