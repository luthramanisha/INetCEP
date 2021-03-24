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

* UpdateNodeState: Update network state information on the compute server of a local or remote node.

    src/main/scala/nfn/service/UpdateNodeState

* Window: An implementation of the window operator. Additional documentation has been provided in the class.

    src/main/scala/nfn/service/Window

* ComputeServerStarter: Updates to the compute server started to publish INetCEP services on the ccn-lite node

    src/main/scala/runnable/production/ComputeServerStarter

The following was changed in order to support producer initiated communication:

# Additions to CCN-Lite
We patched ccn-lite from the last release that supported NFN (ccn-lite 2.0.1) and have the latest master commits of https://github.com/cn-uofbasel/ccn-lite with the exception on some commits that mainly changed the data types.
Our goal was to extend ccn-lite in order to additionally allow producer initiated communication therefore we added three new packet types:

* Datastream Packet: A Packet that can initiate communication coming from a producer. The data is then send continuously to all consumers that have a pending interest in the data.
* Add Persistent Interest Packet: A Packet that signals, that the Interest should be always satisfied if possible, much like a subscribe message in pub-sub systems.
* Remove Persistent Interest Packet: A Packet that removes a persistent interest packet much like an unsubscribe message in a pub-sub system.

With these additions we also enable producer initiated complex event processing as in the consumers add a query interest and the datastream packages initiate the computation of the complex event.

This was achieved by making several extensions to ccn-lite:

* Adding a sensor struct: A seperate struct with an own loop that constantly creates sensor readings as event tuples and sends them to a specified relay

* Adding a sensor setting struct: A struct that holds the sensor information

* Adding a sensor tuple struct: A buffer that contains the event tuples which are sent to the node they are connected to

* Creating a utility library for packet dumping to constantly format the output of the packet

* Altering the ccn-lite-fwd mechanism in order to handle datastream packets, add constant interest packets and remove constant interest packets

Furhtermore we made changes to the linuxkernel Version of CCN-Lite in order to run it on the latest linux Kernel supported by Ubuntu 18.04 LTS which is at the time Kernel 5.3.0-40-generic. We also made extensions to integrate the capabilities to run NFN in the Kernel.

In Detail:

* src/ccnl-core/include/ccnl-interest.h: New struct ccnl_pendQ_s for pending query interests. Each interest can have a pending query interest that is executed whenever a data stream packet arrives that satisfies the interest packet. ccnl_query_append_pending a function that adds a pending query interest to an interest. ccnl_interest_dup a function that duplicates a given interest. Extendsion to the struct ccnl_interest_s with boolean variables isPersistent in order to indicate that the interest is persistent, isRemove in order to indicate that this interest removes other interests (is a remove persistent interest packet). A pointer to the fist pending Query of the pending queries for the interest. If null, there are no pending queries.

* src/ccnl-core/include/ccnl-os-time.h: Change struct ccnl_timerlists_s to take a new struct legacy_timer_emu which is necessary for the ccn-lite kernel version since newer Kernel versions use this for timing. Function legacy_timer_emu_func that emulates a legacy timer for newer Kernel Versions > 4.15.0.

* src/ccnl-core/include/ccnl-pkt-util.h: add functions ccnl_pkt_interest_isPersistent and ccnl_pkt_interest_isRemove in order to distinguish between the new packet types.

* src/ccnl-core/include/ccnl-pkt.h: Change struct ccnl_pktdetail_ndntlv_s to add to boolean variables isPersistent and isRemovePersistent to differentiate between these packet types.

* src/ccnl-core/include/ccnl-prefix.h: Change struct ccnl_prefix_s by defining CCNL_PREFIX_API to be the numb er 0x02 and CCNL_PREFIX_RQI to be the number 0x08.

* src/ccnl-core/include/ccnl_relay.h: Add generic function DBL_LINKED_LIST_EMPLACE_BACK to place a list item at the back of a doubly linked list and not in the front. Add generic Function DBL_LINKED_LIST_REMOVE_FIRST to remove the first element of the given doubly linked list and not the last.

* src/ccnl-core/src/ccnl-interest.c: Change ccnl_interest_new to also add the persistent and remove flag to the new interest. Add function ccnl_interest_dup, add function ccnl_query_append_pending

* src/ccnl-core/src/time.c: Change ccnl_set_timer to conform with linux kernel versions > 4.15.0

* src/ccnl-core/src/ccnl-relay.c: Change ccnl_serve_pending, ccnl_do_ageing to not remove a persistent interest.

* src/ccnl-dump/include/ccn-lite-pktdump-util.h and src/ccnl-dump/src/ccn-lite-pktdump-util.c: Created a library for packet dumps.

* src/ccnl-fwd/src/ccnl-fwd.c: Create function ccnl_content_serve_pendingQueries to serve the pending queries of an interest packet. Change function ccnl_fwd_handleContent to handle data stream packets and react accordingly by calling a pending query if it exists. Change function ccnl_handleInterest to react to a remove persistent interest packet to remove a persistent interest. Change ccnl_ndntlv_forwarder to react to the new packets.

* src/ccnl-lnxkernel/ccn-lite-lnxkernel.c: Change includes to make them work again. Implement ccnl_open_ethdev, ccnl_open_udpdev, ccnl_realloc and ccnl_strdup. Add a Krivine Abstract machine struct to the kernel relay to enable NFN in the Kernel.

* src/ccnl-nfn/src/ccnl-nfn-common.c: Implment function ccnl_nfn_local_interest_search to search for a local interest.

* src/ccnl-nfn/src/ccnl-nfn-ops.c: Implement a function str_split that splits a string with a given delimiter. Implement a function createNewPacket that creates a new data packet with a given content. This is used to store the operator state. Implement function ccnl_makeQueryPersistent which appends a nfn query to a persistent interest packet for data. This way when a data stream packet arrives and the persistent interest matches, the nfn function is executed with the newest data. Implement function window_purge_old_data that takes the previous data state, the parameters for a window and the new tuple to add and deletes the data that should not be in the new window. Implement function op_builtin_window that is a builtin window operator.

* src/ccnl-pkt/include/ccnl-pkt-builder.h and src/ccnl-pkt/src/ccnl-pkt-builder.c: Change function ccnl_mkSimpleInterest to accept the type of interest (normal, removePersistent and Persistent) and create the specific one accordingly. Implement ccnl_mkPersistentInterestObject to create a persistent interest.

* src/ccnl-pkt/include/ccnl-pkt-ndntlv.h and src/ccnl-pkt/include/ccnl-pkt-ndntlv.c: add Packet type identifier for data stream packet, persistent interest and remove persistent interest. Implement function ccnl_ndntlv_prependPersistentInterest that prepends a persistent interest packet with the according identifier.

* src/ccnl-sensor/\*: Structs and utilites that implement a sensor function.

* src/ccnl-utils/\*: Utility functions to create a new sensor, shut it down, send a persistent interest and remove a persistent interest. Furthermore to make a data stream packet.



## Services 
The services that were created for the system are:

* Starting Nodes: StartNode.sh shell service (on each node)
* Starting Compute Server: StartCS.sh shell service (on each node)
* Query Service: queryService.sh shell service (on each node) to manage the query store

    /VM-Startup-Scripts/VM28/queryService.sh

* Update Node State: updateNodeState_NodeID.sh (on each node) shell service to gather network data from iPerf and ping tools as well as node battery status to update the nodes network state on its local Compute Server.

    /VM-Startup-Scripts/VM28/updateNodeState_9001.sh
