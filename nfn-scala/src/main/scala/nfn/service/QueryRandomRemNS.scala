package nfn.service
/**
  * Created by Ali on 06.02.18.
  * This is the random adaptive approach based on fetch based nds
  */
import java.io._
import java.util._

import SACEPICN.{NodeMapping, _}
import akka.actor.ActorRef
import myutil.FormattedOutput
import nfn.tools.Networking._

import scala.annotation.tailrec
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.duration._
import scala.io.Source
import scala.language.postfixOps

//Added for contentfetch
import java.util.Calendar

import ccn.packet.{CCNName, Interest, NFNInterest}
import config.StaticConfig

class QueryRandomRemNS() extends NFNService {

  override def function(interestName: CCNName, args: Seq[NFNValue], ccnApi: ActorRef): NFNValue = {

    val sacepicnEnv = StaticConfig.systemPath
    //ClientID: Client who requested the query
    //Query: The query to break and process
    //Region: User Region to hit for sensors
    //Timestamp: Used to distinguish the time of arrival for the queries
    def processQuery(thisNode: String, clientID: String, query: String, region: String, timestamp: String): String = {

      //Get current node from interest:
      var nodeInfo = interestName.cmps.mkString(" ");
      var nodeName = nodeInfo.substring(nodeInfo.indexOf("/node") + 6 , nodeInfo.indexOf("nfn_service") - 1);
      LogMessage(nodeName, s"Query Execution Started");

      //Log Query for interval based trigger:
      if(save_to_QueryStore(thisNode, clientID, query, region, timestamp)){
        LogMessage(nodeName, s"Query appended to Query Store");
      }else
        LogMessage(nodeName, s"Query NOT appended to Query Store");

      //Initialize ExpressionTree class:
      var et = new OperatorTree();
      //Create node stack or Node tree (current: Tree)
      var root = et.createOperatorTree(query);

      //Get current Network Status and Path information:
      var allNodes = getNodeStatus(nodeName);

      //Now that we have all the nodes we need to get the random nodes based on the number of operators:
      //1) Find the number of operators in the query:
      var opCount = root._stackSize;
      //2) For this size: Get the random number of nodes from all the nodes: Random nodes selection based on all Nodes:
      LogMessage(nodeName, s"Selecting Random Nodes:");

      //Random nodes selected
      var selectedNodes = (scala.util.Random.shuffle(allNodes) take opCount).toArray;

      var formatNodeType = new ArrayBuffer[String]
      for(randomNode <- selectedNodes){
        formatNodeType += randomNode.NI_NodeName
        LogMessage(nodeName, s"Randomly selected node is: ${randomNode.NI_NodeName} with current status (BDP: ${randomNode.NI_Latency} - Energy: ${randomNode.NI_Battery})");
      }

      //Take this path and place the queries on it:
      //1) For this we will need to process the tree:
      LogMessage(nodeName, s"Operator Placement Started");
      @tailrec
      def processPlacementTree(currentNode: Node, optimalPath: ArrayBuffer[String]): Node = {
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
              currentNode._executionNode = new NodeMapping().getName(currentNode._executionNodePort);

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
      var placementRoot = processPlacementTree(root._root, formatNodeType);
      LogMessage(nodeName, s"Operator Placement Completed");

      LogMessage(nodeName, s"Query Deployment Started");
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
            currentNode._value = executeNFNQuery(currentNode._query)
            LogMessage(nodeName, s"Deployment result: ${currentNode._value}")
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
      LogMessage(nodeName, s"Query Deployment Completed");

      //Output is what we send back as the final result:
      var output = "";
      output = deployedRoot._value;

      if (output != null && !output.isEmpty)
        output = output.stripSuffix("\n").stripMargin('#');
      else
        output += "No Results!"

      LogMessage(nodeName, s"Query Execution Completed");
      return output;
    }

    def getRandomElement(list: Seq[String], random: Random): String = list(random.nextInt(list.length))

    def getNodeStatus(nodeName: String): ListBuffer[NodeInfo] = {
      LogMessage(nodeName, s"Get Node Status Started");
      //Get all node information:
      val now = Calendar.getInstance()

      var allNodes = new ListBuffer[NodeInfo]();
      //Below file must be present in order to carry out proper placement:
      val bufferedSource = Source.fromFile(s"$sacepicnEnv/nodeData/nodeInformation");
      bufferedSource
        .getLines
        .foreach { line: String =>
          var nodeSplit = line.split("-"); //Data is always in the form of: nodeX/90XX
          var name = s"/${nodeSplit(1)}/" + now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE);
          //Get content from network
          //Execute NFN Query to get the latest state from Compute Server cache on the remote node (this is slower)
          //var intermediateResult = new String(fetchContentRepeatedly(NFNInterest(s"(call 2 /node/${nodeSplit(0)}/nfn_service_GetContent '${name}')"), ccnApi, 10 seconds).get.data)
          var intermediateResult = executeNFNQuery(s"call 2 /node/${nodeSplit(0)}/nfn_service_GetContent '${name}')")

          if (intermediateResult != "") {
            var ni = new NodeInfo(intermediateResult);
            LogMessage(nodeName, s"Node Added: ${ni.NI_NodeName}")
            allNodes += ni;
          }
        }
      bufferedSource.close

      LogMessage(nodeName, s"Get Node Status Completed");
      return allNodes;
    }

    def buildPaths(nodeName: String, rootNode: String, nodes: ListBuffer[NodeInfo]): ListBuffer[Paths] = {
      LogMessage(nodeName, s"Building Paths Started");
      var paths = new ListBuffer[Paths];
      var root = nodes.filter(x => x.NI_NodeName == rootNode).head;
      nodes -= root;
      nodes.insert(0, root);

      var endOfPath = false;

      var currentRoot = root.NI_NodeName;

      var traversedNodes = new ListBuffer[NodeInfo];
      traversedNodes += root;

      var traversalNodes:ListBuffer[NodeInfo] = getTraversalNodes(nodeName, root, nodes, new ListBuffer[NodeInfo], traversedNodes);
      LogMessage(nodeName, s"getTraversalNodes -> on Root");

      var firstHopList = new ListBuffer[HopObject];
      var firstpath = new HopObject();
      firstpath.hopName = root.NI_NodeName;
      firstpath.hopLatency += 0.0;
      firstpath.previousHop = null;
      LogMessage(nodeName, s"oneStepTraverse ROOT added -> NULL -> ${firstpath.hopName}");
      firstHopList += firstpath;

      //All new paths will now be in hopInfo:
      LogMessage(nodeName, s"oneStepTraverse -> on Root");
      var hopInfo = oneStepTraverse(nodeName, root, root, firstHopList);

      while(!traversalNodes.isEmpty){
        var next = traversalNodes.head;
        LogMessage(nodeName, s"While -> Next -> ${next.NI_NodeName}");

        LogMessage(nodeName, s"While -> Retrieve HopInfo\n");
        hopInfo.appendAll(oneStepTraverse(nodeName, root, next, hopInfo));

        LogMessage(nodeName, s"While -> Retrieve other traversal nodes");
        traversalNodes.appendAll(getTraversalNodes(nodeName, next, nodes, traversalNodes, traversedNodes));

        LogMessage(nodeName, s"While -> remove current node from traversal");
        traversalNodes = traversalNodes.tail;

        traversedNodes += next;

      }

      //Remove duplicate paths - since we traverse ALL possible HOPS and add the path and it's head:
      hopInfo = hopInfo.distinct;

      //Get the utility function data:
      var multiObjFunction = getMultiObjectiveFunctionMetrics();

      var energyWeight = multiObjFunction(0)
      var bdpWeight = multiObjFunction(1)

      //Recursively print the paths AND store in a path list:
      @tailrec
      def checkPath(hops:ListBuffer[HopObject]): String = {
        if(hops.isEmpty)
          {LogMessage(nodeName, s"(Finish!)");
          return "ok";}
        else {
          var current = hops.head;
          var pathBDP:Double = 0.0;
          var pathString = "";
          var hopCount:Int = 0;
          var pathNodes = new ListBuffer[String];

          //Initialize adaptive weight assignment for each hop in the path:
          var previousEnergyWeight = 0.0
          var previousBDPWeight = 0.0

          var lastHopEnergy = 0.0
          var lastHopBDP = 0.0

          while(current.previousHop != null) {
            if (current.previousHop.previousHop == null
              || (current.previousHop.previousHop != null && (current.hopName != current.previousHop.previousHop.hopName))
            ) {
              var hopBDP: Double = 0.0;
              var nodePower = nodes.filter(x => x.NI_NodeName == current.hopName);
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
                      previousEnergyWeight = previousEnergyWeight + 0.01;
                      previousBDPWeight = previousBDPWeight - 0.01;
                    }
                  }
                  //else check the other way around - if bdp is more than the last hop and energy is less.
                  else  if(nodePower.head.NI_Battery <= lastHopEnergy && current.hopLatency >= lastHopBDP){
                    //Additive increase on energy and additive decrease on bdp:
                    if(previousEnergyWeight > 0.00 && previousBDPWeight < 1.00) {
                      previousEnergyWeight = previousEnergyWeight - 0.01;
                      previousBDPWeight = previousBDPWeight + 0.01;
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
                pathBDP += hopBDP;
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
          path.cumulativePathCost = pathBDP;
          path.pathNodes = pathNodes.toArray;
          paths+=path;

          LogMessage(nodeName, s"Path: ${pathString}");

          checkPath(hops.tail);
        }
      }
      checkPath(hopInfo);

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
      returnData(0) = 0.8;
      returnData(1) = 0.2

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

      return new String(fetchContent(
        Interest(query),
        ccnApi,
        60 seconds).get.data)
    }
    def executeNFNQuery(query: String): String = {

      return new String(fetchContent(
        NFNInterest(query),
        ccnApi,
        60 seconds).get.data)
    }

    def save_to_QueryStore(interestOrigin: String, clientID: String, query: String, region: String, timestamp: String): Boolean = {
      var filename = s"$sacepicnEnv/nodeData/queryStore";
      val file = new File(filename)
      file.createNewFile()
      val bw = new BufferedWriter(new FileWriter(file))

      var now = Calendar.getInstance()
      var q_TimeStamp = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE);

      var queryToStore = s"QID:${clientID}_${q_TimeStamp} ${interestOrigin} ${clientID} ${query} ${region} ${timestamp}"

      bw.write(queryToStore)
      bw.close()

      return true;
    }

    NFNStringValue(
      args match {
        case Seq(myNode: NFNStringValue, clientID: NFNStringValue, query: NFNStringValue, region: NFNStringValue, timestamp: NFNStringValue) => processQuery(myNode.str, clientID.str, query.str, region.str, timestamp.str)
        case _ =>
          throw new NFNServiceArgumentException(s"$ccnName can only be applied to values of type NFNBinaryDataValue and not $args")
      }
    )
  }
}
