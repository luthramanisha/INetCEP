#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/.."
#assuming you are at the project root
source "$work_dir/VMS.cfg"
#CCNL_HOME="~/MA-Ali/ccn-lite"

buildAndUpload(){
build
uploadOnVMA
}

build()  {
	cd $work_dir/nfn-scala	
	sbt clean
	sbt compile
	sbt assembly
	cd ..
}

uploadOnVMA() {
	scp -rp "$work_dir"/nfn-scala/target/scala-2.10/*.jar $user@${VMS[0]}:~/MA-Ali/computeservers/nodes/*/
	scp "$work_dir"/VM-Startup-Scripts/VM28/28/queryService.sh $user@${VMS[0]}:~/MA-Ali/VM-Startup-Scripts/28/
	scp -r "$work_dir"/sensors/* $user@${VMS[0]}:~/MA-Ali/sensors/
}


# Usage: bash publishRemotely.sh centralme $queryNumber $runtime
centralProcessingOnHost() {
	if [[ -z $2 ]]
		then
			#default runtime 10 mins
			$2=600
	fi

	echo 'received call with args' $1 $2
	screen -d -m $CCNL_HOME/bin/ccn-nfn-relay -v trace -u 9001 -x /tmp/mgmt-nfn-relay-a.sock -d $CCNL_HOME/test/ndntlv > $work_dir/VM-Startup-Scripts/relay.log
	nohup java -jar $work_dir/nfn-scala/target/scala-2.10/nfn-assembly-0.2.0.jar --mgmtsocket /tmp/mgmt-nfn-relay-a.sock --ccnl-port 9001 --cs-port 9002 --debug --ccnl-already-running /node/nodeA > $work_dir/VM-Startup-Scripts/CS.log &
	nohup bash $work_dir/VM-Startup-Scripts/VM28/28/queryService_local.sh 127.0.0.1 9001 nodeA CentralizedPlacement 20 > $work_dir/VM-Startup-Scripts/startUp.log &
	centralQuery $1 & sleep $2; shutdownHost
}

centralQuery() {
case $1 in
	1)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 20 "" "call 9 /node/nodeA/nfn_service_CentralizedPlacement 'Centralized' 'Centralized' '1' 'Source' 'Client1' 'WINDOW(name,victims,4,S)' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	2)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 20 "" "call 9 /node/nodeA/nfn_service_CentralizedPlacement 'Centralized' 'Centralized' '1' 'Source' 'Client1' 'FILTER(name,WINDOW(name,victims,4,S),3=M&4>30,name)' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	3)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 20 "" "call 9 /node/nodeA/nfn_service_CentralizedPlacement 'Centralized' 'Centralized' '1' 'Source' 'Client1' 'JOIN(name,name,FILTER(name,WINDOW(name,victims,4,S),3=M&4>30,name),FILTER(name,WINDOW(name,survivors,4,S),3=F&4>25,name))' 'Region1' '12:06:58.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	4)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 20 "" "call 9 /node/nodeA/nfn_service_CentralizedPlacement 'Centralized' 'Centralized' '1' 'Source' 'Client1' 'JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,1,M)),PREDICT2(name,name,30s,WINDOW(name,plug1,1,M)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	5)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 20 "" "call 9 /node/nodeA/nfn_service_CentralizedPlacement 'Centralized' 'Centralized' '1' 'Source' 'Client1' 'FILTER(name,JOIN(name,name,PREDICT2(name,name,30s,WINDOW(name,plug0,1,M)),PREDICT2(name,name,30s,WINDOW(name,plug1,1,M))),6>50,name)' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	6)
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 20 "" "call 9 /node/nodeA/nfn_service_CentralizedPlacement 'Centralized' 'Centralized' '1' 'Source' 'Client1' 'HEATMAP(name,name,0.0015,8.7262659072876,8.8215389251709,51.7832946777344,51.8207664489746,JOIN(name,name,WINDOW(name,gps1,2,S),WINDOW(name,gps2,2,S)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	;;
	*) echo "do_nothing"
	;;
	esac
}


centralProcessingOnVMA() {
	if [[ -z $1 ]]
	then
		#default runtime 10 mins
		$1=600
	fi

	ssh $user@${VMS[0]} <<-ENDSSH
	echo "logged in "${VMS[0]}		
	screen -d -m $CCNL_HOME/bin/ccn-nfn-relay -v trace -u 9001 -x /tmp/mgmt-nfn-relay-a.sock -d $CCNL_HOME/test/ndntlv > ~/MA-Ali/VM-Startup-Scripts/relay.log
	nohup java -jar ~/MA-Ali/computeservers/nodes/nodeA/nfn-assembly-0.2.0.jar --mgmtsocket /tmp/mgmt-nfn-relay-a.sock --ccnl-port 9001 --cs-port 9002 --debug --ccnl-already-running /node/nodeA > ~/MA-Ali/VM-Startup-Scripts/CS.log &
	nohup bash ~/MA-Ali/VM-Startup-Scripts/28/queryService.sh 127.0.0.1 9001 nodeA CentralizedPlacement 20 > ~/MA-Ali/VM-Startup-Scripts/startUp.log &
	$CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u 127.0.0.1/9001 -w 20 "" "call 9 /node/nodeA/nfn_service_CentralizedPlacement 'Centralized' 'Centralized' '1' 'Source' 'Client1' 'HEATMAP(name,name,0.0015,8.7262659072876,8.8215389251709,51.7832946777344,51.8207664489746,JOIN(name,name,WINDOW(name,gps1,2,S),WINDOW(name,gps2,2,S)))' 'Region1' '16:22:00.200'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	ENDSSH
	sleep $1; shutdown
}

shutdown() {
	ssh $user@${VMS[0]} <<-'ENDSSH'
	screen -d -m bash $work_dir/VM-Shutdown-Scripts/VMA/shutdown.sh 
	pkill -f "java"
	pkill -f "nfn-relay"
	pkill -f "iperf"
	pkill -f "ccn"
	pkill -f "nfn"
	pkill -f "query" 
	ENDSSH
}

shutdownHost() {
	screen -d -m bash $work_dir/VM-Shutdown-Scripts/VMA/shutdown.sh 
	pkill -f "java"
	pkill -f "nfn-relay"
	pkill -f "iperf"
	pkill -f "ccn"
	pkill -f "nfn"
	pkill -f "query" 
}



help="
Invalid usage

Central processing on local host or VMA

Usage: ./centralProccessing.sh <arg1> <arg2> <arg3>

Available <arg1> options:
central: runs the query on the first VM in your VMS.cfg
centralme: runs the query on your host
shutdownme: shuts down the emulation on your host
shutdown: Shutdown the emulation

Available <arg2> options: type of queries - supported ones Window, Filter(Window), Join(F(W), F(W)), Join (Predict(W), Predict(W)), F (J(P(W), P(W))), Heatmap(J(W), J(W))
input: [1, 6]

Avalable <arg3> options: simulation run time in seconds
input: {N}: Any natural number
"



if [ $1 == "central" ]; then centralProcessingOnVMA $2
elif [ $1 == "centralme" ]; then centralProcessingOnHost $2 $3
elif [ $1 == "upload" ]; then uploadOnVMA
elif [ $1 == "getOutput" ]; then getOutput
elif [ $1 == "shutdownme" ]; then shutdownHost
elif [ $1 == "shutdown" ]; then shutdown
elif [ $1 == "build" ]; then build
elif [ $1 == "ba" ]; then buildAndUpload
else echo "$help"
fi


