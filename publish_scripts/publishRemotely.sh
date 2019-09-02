#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/.."
#assuming you are at the project root
source "$work_dir/VMS.cfg"
count=0
declare -a VMSdir
# $2 queryType e.g., "QueryCentralRemNS" (argument 2 required)
# $3 queryServiceInterval e.g., 20 (argument 3 required)
# {"QueryCentralFixed" "QueryCentralLocalNS" "QueryCentralRemNS" "QueryDecentral" "QueryDecentralFixed" "QueryRandom" "QueryRandomLocalNS" "QueryRandomRemNS"}
queryType=$2
placementType=$3 # 1="Centralized", 2="Decentralized", 3="Random"
queryServiceInterval=$4
#simulation run time in seconds
simRunTime=$5
#TODO fix workaround: setting CCNL_HOME for executeQuery method (env variables of the remote machine cannot be accessed if quotes are removed from "ENDSSH" and if quotes are put then "queryType" cannot be accessed)
CCNL_HOME="~/MA-Ali/ccn-lite" #requires project to copied at the home location (~) # commented for local executions

#This script automates the execution of the system remotely on seven VMS (check VMS.cfg) 
#It first sets up the environment by copying the relevant binaries and scripts for execution remotely 
#Second it starts the topology on CCN-lite emulator
#It starts the NFN compute server 
#Last it starts the query service that is the part of ICNCEP, which reads the nodeData/queryStore at regular intervals to perform continuous detection of events
#Updates node state that is utilized by the placement component
#and intializes the query on one of the VMS (in this script 28) check "nodeData/queryStore"
#finally the results appear in nodeData/queryOutput and nodeData/nodei_Log
if [[ -z $simRunTime ]]
	then
		#default runtime 10 mins
		simRunTime=600
	fi

#Usage : bash publishRemotely.sh all "QueryCentralRemNS" 20
#Usage : bash publishRemotely.sh all "Predict1QueryCentralRemNS" 20
#new Usage: bash publishRemotely.sh all "Placement" 1 20 1200
all() {
	
	buildCCNLite
	buildNFN
	sleep 2s
	setup	
	sleep 2s
	copyNodeInfo
	sleep 2s
	copyCCNFiles
	sleep 2s
	copyNFNFiless
	sleep 2s
	createTopology
	sleep 5s
	execute
	sleep 5s
	executeQueryinVMA & sleep $simRunTime; shutdown
}

installDependencies() {
read -s -p "Enter Password for sudo: " sudoPW
	for i in "${VMS[@]}"
	do
		ssh -t $user@$i<<-ENDSSH
		echo "Installing JAVA"
		echo "$sudoPW" | sudo -S apt-get update
		echo "$sudoPW" | sudo -S apt-get install -y libssl-dev default-jdk default-jre bc iperf
		ENDSSH
	done
}


#copy the binaries
#Usage : bash publishRemotely.sh setup
setup() {
echo "creating directories and deleting old ones"
	for i in "${VMS[@]}"
	do
		echo "logged in: " $i 	
		# make directories if they don't exist already
		ssh -t $user@$i <<-'ENDSSH'	
		rm -rf ~/MA-Ali
		mkdir -p ~/MA-Ali/ccn-lite/bin/
		mkdir -p ~/MA-Ali/nodeData/
		mkdir -p ~/MA-Ali/computeservers/nodes/
		mkdir -p ~/MA-Ali/sensors
		ENDSSH
    	done
}

copyNFNFiles(){
echo "copying NFN-jar"
	#copy only the required jar
	for i in "${VMS[@]}"
	do
		scp -rp "$work_dir"/nfn-scala/target/scala-2.10/*.jar $user@$i:~/MA-Ali/computeservers/nodes/*/
	done
}

copyCCNFiles(){
echo "copying CCN-lite binaries"
	#copy only the required binaries
	for i in "${VMS[@]}"
	do	
		ssh -t $user@$i <<-'ENDSSH'
		rm -rf ~/MA-Ali/computeservers/nodes/
		mkdir -p ~/MA-Ali/computeservers/nodes/
		ENDSSH
		scp -rp "$work_dir"/ccn-lite/bin/* $user@$i:~/MA-Ali/ccn-lite/bin/ #TODO Ali: are there specific binaries of ccn-lite that must be copied ONLY? 
	done

}

#copy the nodeInformation
#Usage : bash publishRemotely.sh copyNodeInfo()
copyNodeInfo() {
echo "copying NodeInformation"
	((count=0))
	for i in "${VMS[@]}"
	do
		echo "logged in: " $i 	
		# empty stuff in nodeData
		ssh -t $user@$i <<-'ENDSSH'	
		rm -rf ~/MA-Ali/nodeData/
		mkdir -p ~/MA-Ali/nodeData/
		rm -rf ~/MA-Ali/sensors
		mkdir -p ~/MA-Ali/sensors
		rm -rf ~/MA-Ali/evalData
		mkdir -p ~/MA-Ali/evalData
		ENDSSH
		nodesdir=($(ls -d $work_dir/computeservers/nodes/*))
		scp -rp ${nodesdir[$count]} $user@$i:~/MA-Ali/computeservers/nodes/
		scp -rp "$work_dir"/nodeData $user@$i:~/MA-Ali/

		scp -rp "$work_dir/VMS.cfg" $user@$i:~/MA-Ali/
		scp -rp "$work_dir"/sensors/* $user@$i:~/MA-Ali/sensors/
	
		#scp -rp "$work_dir"/evalData/* $USER@$i:~/MA-Ali/evalData/

		#copy the scripts only once for this VM
		ssh $user@$i 'rm -r ~/MA-Ali/VM-Startup-Scripts'
		VMSdir=($(ls -d $work_dir/VM-Startup-Scripts/*))
		
		#copy the start up scripts
		scp -r ${VMSdir[$count]} $user@$i:~/MA-Ali/VM-Startup-Scripts

		#copy the shutdown scripts
		ssh $user@$i 'rm -r ~/MA-Ali/VM-Shutdown-Scripts'
		VMSdir=($(ls -d $work_dir/VM-Shutdown-Scripts/*))
		scp -r ${VMSdir[$count]} $user@$i:~/MA-Ali/VM-Shutdown-Scripts
		
		((count++))
	
	done
}

#initializes the topology and starts the compute server
#Usage : bash publishRemotely.sh create
createTopology() {
for i in "${VMS[@]}"
	do
		echo "logged in: " $i
		ssh -t $user@$i <<-'ENDSSH'
		cd ~/MA-Ali/VM-Startup-Scripts
		#call the scripts asynchronously using screen (nohup and & didn't work) in order to repeat the same for all the VMs
		#first create topology and start the compute server in all nodes
		screen -d -m bash executeScripts.sh initialize
		ENDSSH
	done

}

#initiates query service and node state update for operator placement
#Usage : bash publishRemotely.sh execute "QueryCentralRemNS" 20
execute() {
for i in "${VMS[@]}"
	do
		echo "logged in: " $i
		ssh -t $user@$i <<-ENDSSH
		cd ~/MA-Ali/VM-Startup-Scripts
		#second start the query service and update node state 
		bash executeScripts.sh start $queryType $queryServiceInterval
		ENDSSH
	done

}

#initiates the query on the first VM
#Usage : bash publishRemotely.sh executeQuery "Placement" 1 (1: Centralized  and 2: Decentralized)
executeQueryinVMA() {
	ssh $user@${VMS[0]} <<-ENDSSH
	echo "logged in "${VMS[0]}
	#one of a kind query
	case $placementType in
	1)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'JOIN(name,name,FILTER(name,WINDOW(name,victims,5,M),3=M&4>30,name),FILTER(name,WINDOW(name,victims,5,M),3=M&4>30,name))' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	2)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Decentralized' '1' 'Source' 'Client1' 'JOIN(name,name,FILTER(name,WINDOW(name,victims,5,M),3=M&4>30,name),FILTER(name,WINDOW(name,victims,5,M),3=M&4>30,name))' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	3)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'PREDICT1(name,name,2m,JOIN(name,name,WINDOW(name,plug0,5,M),WINDOW(name,plug1,5,M)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	4)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'JOIN(name,name,PREDICT2(name,name,2m,WINDOW(name,plug0,5,M)),PREDICT2(name,name,2m,WINDOW(name,plug1,5,M)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	5)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'FILTER(name,PREDICT1(name,name,2m,JOIN(name,name,WINDOW(name,plug0,5,M),WINDOW(name,plug1,5,M))),6>20,name)' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	6)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'FILTER(name,JOIN(name,name,PREDICT2(name,name,2m,WINDOW(name,plug0,5,M)),PREDICT2(name,name,2m,WINDOW(name,plug1,5,M))),6>20,name)' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	7)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'HEATMAP(name,name,0.0015,8.8215389251709,8.7262659072876,51.7832946777344,51.8207664489746,JOIN(name,name,WINDOW(name,gps1,5,M),WINDOW(name,gps2,5,M)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	8)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'PREDICT1(name,name,30s,JOIN(name,name,WINDOW(name,plug0,16:22:00.000,16:23:00.000),WINDOW(name,plug1,16:22:00.000,16:23:00.000)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	9)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'JOIN(name,name,FILTER(name,WINDOW(name,victims,22:18:38.841,22:18:41.841),3=M&4>30,name),FILTER(name,WINDOW(name,victims,22:18:38.841,22:18:41.841),3=M&4>30,name))' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	10)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,16:22:00.000,16:23:00.000)),PREDICT2(name,name,30s,WINDOW(name,plug1,16:22:00.000,16:23:00.000)))' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	11)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'HEATMAP(name,name,0.0015,8.7262659072876,8.8215389251709,51.7832946777344,51.8207664489746,JOIN(name,name,WINDOW(name,gps1,15:23:00.000,15:23:30.000),WINDOW(name,gps2,15:23:00.000,15:23:30.000)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	12)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'FILTER(name,JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,16:22:00.000,16:23:00.000)),PREDICT2(name,name,30s,WINDOW(name,plug1,16:22:00.000,16:23:00.000))),5>58,name)' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	13)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'FILTER(name,WINDOW(name,victims,22:18:38.841,22:18:41.841),3=M&4>30,name)' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	14)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'WINDOW(name,victims,22:18:38.841,22:18:41.841)' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	15)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'FILTER(name,PREDICT1(name,name,30s,JOIN(name,name,WINDOW(name,plug0,16:22:00.000,16:23:00.000),WINDOW(name,plug1,16:22:00.000,16:23:00.000))),2>58,name)' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	16)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'WINDOW(name,victims,4,S)' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	17)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'FILTER(name,WINDOW(name,victims,4,S),3=M&4>30,name)' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	18)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'JOIN(name,name,FILTER(name,WINDOW(name,victims,4,S),3=M&4>30,name),FILTER(name,WINDOW(name,victims,4,S),3=M&4>30,name))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	19)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,1,S)),PREDICT2(name,name,30s,WINDOW(name,plug1,1,S)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	20)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'FILTER(name,JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,1,S)),PREDICT2(name,name,30s,WINDOW(name,plug1,1,S))),6>50,name)' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	21)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u ${VMS[0]}/9001 -w 20 "" "call 8 /node/nodeA/nfn_service_Placement 'Centralized' '1' 'Source' 'Client1' 'HEATMAP(name,name,0.0015,8.7262659072876,8.8215389251709,51.7832946777344,51.8207664489746,JOIN(name,name,WINDOW(name,gps1,2,S),WINDOW(name,gps2,2,S)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	*) echo "do_nothing"
	;;
	esac
	ENDSSH

}

# builds the nfn-scala alongwith SACEPICN code into a single jar located in nfn-scale/target.. This is then copied using setup
# Usage bash publishRemotely.sh build
buildNFN(){
echo "building NFN-Scala"
	cd $work_dir/nfn-scala	
	sbt clean
	sbt compile
	sbt assembly
	cd ..
}

# builds CCN-lite binaries required to start the emulation 
# Usage bash publishRemotely.sh build
buildCCNLite(){
echo "building CCNLite"
	cd $work_dir/ccn-lite/src
	export USE_NFN=1
	export USE_NACK=1
	make clean all
}


# get the output from the machine where query was initialized (in this case VM 28)
# Usage bash publishRemotely.sh getOutput
getOutput(){
	mkdir -p $work_dir/HeatmapOutput/	
	scp -r $user@${VMS[0]}:~/MA-Ali/nodeData/* $work_dir/HeatmapOutput/

}

# graceful shutdown of relay and face 
# Usage bash publishRemotely.sh shutdown
shutdown(){
for i in "${VMS[@]}"
	do
		echo "logged in: " $i
		# Shutdown script stops the relay and destroys the face
		ssh -t $user@$i 'screen -d -m bash ~/MA-Ali/VM-Shutdown-Scripts/shutdown.sh'
		# Still the java and bash processes needs to be stopped
		bash $work_dir/publish_scripts/pkill.sh		
	done
}


help="
Invalid usage

Publish SACEPICN script

Usage: ./publishRemotely.sh <COMMAND1> <COMMAND2> <COMMAND3>

Available <COMMAND1> options:
install: Install dependencies and setup the project
setup: Copy the binaries and scripts to execute the application
build: build the SACEPICN app and the ccn-lite binaries
create: Creates and starts the topology on ccn-lite emulator
execute: Starts the query service, update node state and executes the placement strategy
executeQuery: Initializes the query on VM A (1st VM in VMS.cfg)
shutdown: Shutdown the emulation
all: Run all steps to publish the cluster and start the application

Available <COMMAND2> options: Query Placement service
input: {"QueryCentralFixed", "QueryCentralLocalNS", "QueryCentralRemNS", "QueryDecentral", "QueryDecentralFixed", "QueryRandom", "QueryRandomLocalNS", "QueryRandomRemNS"}

Avalable <COMMAND3> options: Query service interval 
input: {N}: Any natural number
"


if [ $1 == "setup" ]; then setup
elif [ $1 == "copyNodeInfo" ]; then copyNodeInfo
elif [ $1 == "install" ]; then installDependencies
elif [ $1 == "build" ]; then buildNFN #&& buildCCNLite
elif [ $1 == "create" ]; then createTopology
elif [ $1 == "execute" ]; then execute
elif [ $1 == "all" ]; then all
elif [ $1 == "executeQuery" ]; then executeQueryinVMA
elif [ $1 == "getOutput" ]; then getOutput
elif [ $1 == "shutdown" ]; then shutdown
else echo "$help"
fi

