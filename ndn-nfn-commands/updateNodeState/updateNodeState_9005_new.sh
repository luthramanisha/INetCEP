#!/bin/bash

ParentIP=$1
ParentPort=$2
ParentNodeName=$3
MyPort=$4
MyNodeName=$5
HostedSensor=$6

function join_by { local IFS="$1"; shift; echo "$*"; }

declare -A arrNodes

#9005 node will start iperf on 8005:
xterm -iconic -hold -e iperf -s -p 8005 -u  &
sleep 5

while true
do 
	
	#5 --> 2	#5 --> 6
	B9002=$(echo $(ping -c 1 127.0.0.1 | tail -1| awk '{print $4 / 2}' | cut -d '/' -f 2) \* $(iperf -c 127.0.0.1 -p 8002 -u -t 1 | tail -1 | awk '{print $8}') |bc)
	B9006=$(echo $(ping -c 1 127.0.0.1 | tail -1| awk '{print $4 / 2}' | cut -d '/' -f 2) \* $(iperf -c 127.0.0.1 -p 8006 -u -t 1 | tail -1 | awk '{print $8}') |bc)

	arrNodes=( ["9002"]="9002=$B9002" 
		   ["9006"]="9006=$B9006" 
		 )

	Latency=$(join_by , ${arrNodes[@]})
	#echo $Latency

	Battery=100
	#$(upower -i $(upower -e | grep BAT) | grep --color=never -E percentage|xargs|cut -d' ' -f2|sed s/%//)

	#DATE_WITH_TIME=`date "+%Y%m%d-%H%M"` #%S%3N

	Content="$MyNodeName|$HostedSensor|$Latency|$Battery"

	#echo $Content

	#Set Content at parent (so that parent can fetch later):
	xterm -e $CCNL_HOME/build/bin/ccn-lite-simplenfn -s ndn2013 -u $ParentIP/$ParentPort -w 10 "call 3 /node/$ParentNodeName/nfn_service_SACEPICN_UpdateNodeState '$MyPort' '$Content'" | $CCNL_HOME/build/bin/ccn-lite-pktdump -f 3 &

	sleep 10

	arrNodes=()

    sleep 110
done

