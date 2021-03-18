<img style="float: left" src="https://github.com/luthramanisha/INetCEP/blob/master/INetCEP.png" width="300" height="200" />

# In-Network Complex Event Processing in Information-centric Networking

## Prerequisites
* Copy your public key to the remote machine using `copyKeys.sh` script
* Install dependencies and copy the binaries by 
```
bash publishRemotely.sh install
```
* Set environment variables using `setEnvironment.sh` script

There are two options to start INetCEP system. Basically Option 1 automates the entire process of option 2. 
Note: Follow only one of the below options.

## Option 1

To start INetCEP on the cluster of resources (see VMS.cfg), please follow the steps below: 

### Setup on VM compute resources

1. create your personalized VM configiration file `VMS.cfg` (refer VMS_MAKI1.cfg and VMS_MAKI2.cfg) for your user and IP addresses. 
2. auto generate node data for the respective machines with the following topology: 

       (3) -- (7)
       
        |

(1) -- (2) -- (5)
 
        |      |

       (4)    (6)
using `python generate_node_info.py`. Note: this script uses VMS.cfg as input for IP address information. Ports used are 9001, 9001,..,9001+n. (n: number of nodes).

### Setup on GENI resources

1. generate the Rspec file to request resources on GENI using `python generate_rspec.py <number of nodes> <out dir>`
2. upload the rspec, select site and reserve resources. 
3. download the manifest rspec file with IP address and port information. 
4. auto generate VMS.cfg file (refer VMS_GENI.cfg) and node data using `python manifest_to_config.py <manifest.xml>`

After the above setup execute the publishRemotely.sh script that publishes the application on either of the above resources based on the VMS.cfg file. Refer `publish_scripts/publishRemotely.sh` script. Usage:

```
bash publish_scripts/publishRemotely.sh all "QueryPlacement" 20
```

Here QueryPlacement is an element of set {"QueryCentralFixed", "QueryCentralLocalNS", "QueryCentralRemNS", "QueryDecentral", "QueryDecentralFixed", "QueryRandom", "QueryRandomLocalNS", "QueryRandomRemNS"}
that are currently available placement services

## Option 2

### Node Startup
Starting up nodes requires the following:
* Startup of the ccn-lite relay
* Linking a nfn-scala compute server with that relay
* Starting the node state update service that manages the node state and the query service

1. Starting ccn-lite relay on a network node (X) requires us to execute the startup script for that specific node. Let's take nodeID 28 as our sample node for the rest of this tutorial.

	```bash
	cd INetCEP/VM-Startup-Scripts/VM28/
	./startNodes.sh
	```

Inside the VM28 folder, the startNodes script resides. This script initializes a node, sets up the required faces and adds those faces to the node. For each node in the network, the face configuration has been done to reflect the network topology presented in the report.

2. Starting the Compute Server is similar to a node. Make sure that you are in the same /VM28 folder and then execute the following script.

	```bash
	cd INetCEP/VM-Startup-Scripts/VM28/
	./startCS.sh
	```

3. Starting the network discovery and query server is handled by one script that manages all sub-system tasks. These tasks include:

* Running the query server, that manages incoming consumer queries (stored in the query store) and re-evaluates them after a pre-defined time interval.
* Updating node state on local nodes. The node state is sent to the ccn-lite node that then sends it to the compute server. The compute server than adds this node state in its local content store.

To start decentralized query processing in the network, we have to pass the Query Type (Centralized, Random, Decentralized) and the interval of re-evaluation for the query store.


    cd INetCEP/VM-Startup-Scripts/VM28/28
    ./startRemoteAny.sh QueryDecentral 20

The startRemoteAny.sh script takes the query type (QueryDecentral -> basically the placement algorithm) that the system has to manage and the interval (20) that the query service has to consider for re-evaluations.

## Query Execution

In order to carry out query execution, we can access any node in the network and issue the following query. Here, any node in the network can act as a placement coordinator. Therefore, the query can be issued from any node to any node in the network.


    $CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 10.2.1.28/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_QueryDecentral '1' 'Source' '9001' 'Client1' 'JOIN([name],[FILTER(name,WINDOW(victims,22:18:36.800,22:18:44.001),3=M&4>30,name)],[FILTER(name,WINDOW(survivors,22:18:35.800,11:11:45.000),3=F&4>20,name)],[NULL])' 'Region1' '22:10:11.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2


The above query issues a ccn-lite-peek interest to nodeID 28 for a decentralized query processing. The complex query is a join that works on two filters. The filters additionally evaluate the window operators on the streams after which the window data is filtered based on column schema represented by (1,2,3,4....n. Here 3=F means check whether the 3rd column contains F, if yes, select it).
The result of each operator is fed to its parent operator, which once complete, is returned back to the consumer node.

This project is based on the CCN-lite project forked from [here](https://github.com/cn-uofbasel/ccn-lite) and [NFN-scala](https://github.com/cn-uofbasel/nfn-scala_deprecated).
Major enhancements are enlisted [here](changes.md)

## [Publications](#publications)
+ Manisha Luthra, Boris Koldehofe, Jonas Höchst, Patrick Lampe, A.H. Rizvi, Ralf Kundel, and Bernd Freisleben. “INetCEP: In-Network Complex Event Processing for Information-Centric Networking.” In: Proceedings of the 15th ACM/IEEE Symposium on Architectures for Networking and Communications Systems (ANCS). 2019, pp. 1–13. 
+ Manisha Luthra, Johannes Pfannmüller, Boris Koldehofe, Jonas Höchst, Artur Sterz, Rhaban Hark, and Bernd Freisleben. “Efficient Complex Event Processing in Information-centric Networking at the Edge (under submission).” In: (2021), pp. 1–17. URL: https://arxiv.org/pdf/2012.05070.pdf

## [Acknowledgement](#acknowledgement)

This work has been co-funded by the German Research Foundation (DFG) within the <a href="https://www.maki.tu-darmstadt.de/sfb_maki/ueber_maki/index.en.jsp" target="_blank">Collaborative Research Center (CRC) 1053 -- MAKI</a>

## [Contact](#contact)

Feel free to contact <a href="https://www.kom.tu-darmstadt.de/kom-multimedia-communications-lab/people/staff/manisha-luthra/" target="_blank">Manisha Luthra</a> or <a href="https://www.rug.nl/staff/b.koldehofe/" target="_blank">Boris Koldehofe</a> for any questions. 
