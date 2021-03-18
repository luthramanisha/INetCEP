# Changes to NFN-Scala and CCN-lite

## Placement Logic
In order to understand the placement logic, we will describe the overall query execution process along with the invoked methods and their uses.

The overall query resolution process has the following steps:
* Issue a complex query (as shown above)
* The complex query is received by the ccn-lite node
* The ccn-lite node looks up the interest, performs lambda-calculus based reduction/closure operations on the interest. Once the call keyword is encountered, the ccn-lite node understands that this is a complex query that has to be send to a compute server. The ccn-lite node then looks at the node prefix passed for computation. Here /node/nodeA is the prefix. Previously, when the compute server started and linked itself with the ccn-lite node, it added this prefix to the face list of the relay. Therefore, the ccn-lite node forwards the interest to the compute server by looking up its face list.
* On receiving the interest from the ccn-lite relay, the compute server performs function resolution through the help of the abstract machine. The abstract machine tells the interest decomposer, the service to invoke (query) and the parameters.
* Once the service has been determined, it is invoked by the interest handler.
* On service invocation, the default class handler is invoked, that matches the arguments and invokes the appropriate handler method. This in usual cases is the 'processQuery' function.
* The processQuery function then sets up the required variables, stores the query in the query store and then creates an operator tree. Each method in the code-base is accompanied with relevant documentation that can be looked up for more information. Once the operator tree has been created, network state discovery is done.
* After network state has been gathered, the paths are build up for placement. Here each path, regardless of hops is looked up, and unique 1-hop...n-hop paths are made. While the paths are being created, on each hop, the adaptive weight application is carried out. Once this is done, each path object then contains the path node information, hops and path cost.
* After the paths have been created, the path that matches the operator count and has the minimum path cost is selected. In decentralized placement, if no path is found, then the query is sent to the best 1-hop neighboring node that carries out this process.
* The selected path is then sent to the ProcessPlacement function, that traverses the path and sets in the appropriate complex queries in it.
* Once the complex query has been set, the ProcessDeployment function is invoked, that executes these queries in the network. Upon gathering the evaluated results for child operators, the top most operator is then evaluated and then the result is returned back to the consumer.

## Additions to NFN-Scala for INetCEP
The following services, classes were added to the system in order to build up INetCEP over NFN-Scala.

* HopObject: Representation for each hop

    /src/main/java/INetCEP/HopObject

* Map: Representation for the operator graph in both tree and stack forms

    /src/main/java/INetCEP/Map

* Node: Representation for each network node in the system. This contains the complex query, node name, port, parent and neighbor node links, processing tags etc.

    /src/main/java/INetCEP/Node

* NodeInfo: This is used to represent all network nodes (determined during the node discovery process), get relevant node information and access the node list for node data (name, port, latency etc)

    /src/main/java/INetCEP/NodeInfo

* NodeMapping: This is a representation of all nodes in the network with regards to their name prefix, port, ip

    /src/main/java/INetCEP/NodeMapping

* Operator: An enumeration for available system operators

    /src/main/java/INetCEP/Operator

* OperatorTree: A java class that creates the operator tree by parsing the incoming complex query in the interest and creating NFN queries from those interests. Process: Parse interest, extract operator information, put operator queries in the tree/stack and return it.

    /src/main/java/INetCEP/OperatorTree

* Paths: This is the representation for paths that contain network nodes. Each path can contain one or more network nodes

    /src/main/java/INetCEP/Paths

* FormattedOutput: Added code to manage numeric data in the system

    /src/main/scala/myutil/FormattedOutput

* ExecuteQuery: A service to execute all types of queries remotely

    src/main/scala/nfn/service/ExecuteQuery

* Filter: A service that provides the filter operator functionality. Additional documentation is done in code.

    src/main/scala/nfn/service/Filter

* GetContent: Get network named-content for any interest

    src/main/scala/nfn/service/GetContent

* GetData: Get data hosted on a network node (data hosted on the nodes file-system). This can be used to manage remote config files etc.

    src/main/scala/nfn/service/GetData

* Join: A services that provides JOIN operator functionality.

    src/main/scala/nfn/service/Join

* NFNService: Changes made to the NFNService to manage additional logging and fixed 

    src/main/scala/nfn/service/NFNService

* NFNServer: Changes made to the HandlePacket function that was breaking on complex interests.

    src/main/scala/nfn/NFNServer

* QueryCentralFixed: Centralized placement based on fetch-based fixed weight variance in the buildpaths function

    src/main/scala/nfn/service/QueryCentralFixed

* QueryCentralLocalNS: Centralized placement based on heartbeats approach in the getNodeStatus function

    src/main/scala/nfn/service/QueryCentralLocalNS

* QueryCentralRemNS: Centralized placement based on fetch-based adaptive weight variance

    src/main/scala/nfn/service/QueryCentralRemNS

* QueryDecentral: Decentralized placement based on fetch-based adaptive weight variance

    src/main/scala/nfn/service/QueryDecentral

* QueryDecentralFixed: Decentralized placement based on fetch-based fixed weight variance

    src/main/scala/nfn/service/QueryDecentralFixed

* QueryRandom: Initial Random placement service (improved in RandomLocal and RandomRem)

    src/main/scala/nfn/service/QueryRandom

* QueryRandomLocalNS: Random placement based on hearbeat-based adaptive weight variance

    src/main/scala/nfn/service/QueryRandomLocalNS

* QueryRandomRemNS: Random placement based on fetch-based adaptive weight variance

    src/main/scala/nfn/service/QueryRandomRemNS

* SetData: Set data on a node. This data is set on node filesystem to configure system files.

    src/main/scala/nfn/service/SetData

* UpdateNodeState: Update network state information on the compute server of a local or remote node.

    src/main/scala/nfn/service/UpdateNodeState

* Window: An implementation of the window operator. Additional documentation has been provided in the class.

    src/main/scala/nfn/service/Window

* ComputeServerStarter: Updates to the compute server started to publish INetCEP services on the ccn-lite node

    src/main/scala/runnable/production/ComputeServerStarter



## Additions to CCN-Lite for INetCEP
In CCN-Lite, the major change was to manage the control interests. This was done in the ccnl_fwd_handleInterest and ccnl_fwd_handleContent methods of ccnl-core-fwd.c code file.
Here, instead of saving the interest data in the content store, we skipped this process to reduce content store overload from control messages.

    /ccn-lite/src/ccnl-core-fwd.c

## Services 
The services that were created for the system are:

* Starting Nodes: StartNode.sh shell service (on each node)
* Starting Compute Server: StartCS.sh shell service (on each node)
* Query Service: queryService.sh shell service (on each node) to manage the query store

    /VM-Startup-Scripts/VM28/queryService.sh

* Update Node State: updateNodeState_NodeID.sh (on each node) shell service to gather network data from iPerf and ping tools as well as node battery status to update the nodes network state on its local Compute Server.

    /VM-Startup-Scripts/VM28/updateNodeState_9001.sh
