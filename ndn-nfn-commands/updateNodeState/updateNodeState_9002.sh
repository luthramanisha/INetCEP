#!/bin/bash

ParentIP=$1
ParentPort=$2
ParentNodeName=$3
MyPort=$4
MyNodeName=$5
HostedSensor=$6

function join_by { local IFS="$1"; shift; echo "$*"; }

declare -A arrNodes

#9002 node will start iperf on 8002:
xterm -iconic -hold -e iperf -s -p 8002 -u  &
sleep 5

while true
do 

	#2 --> 1	#2 --> 3	#2 --> 4	#2 --> 5
	B9001=$(echo $(ping -c 1 127.0.0.1 | tail -1| awk '{print $4 / 2}' | cut -d '/' -f 2) \* $(iperf -c 127.0.0.1 -p 8001 -u -t 1 | tail -1 | awk '{print $8}') |bc)
	B9003=$(echo $(ping -c 1 127.0.0.1 | tail -1| awk '{print $4 / 2}' | cut -d '/' -f 2) \* $(iperf -c 127.0.0.1 -p 8003 -u -t 1 | tail -1 | awk '{print $8}') |bc)
	B9004=$(echo $(ping -c 1 127.0.0.1 | tail -1| awk '{print $4 / 2}' | cut -d '/' -f 2) \* $(iperf -c 127.0.0.1 -p 8004 -u -t 1 | tail -1 | awk '{print $8}') |bc)
	B9005=$(echo $(ping -c 1 127.0.0.1 | tail -1| awk '{print $4 / 2}' | cut -d '/' -f 2) \* $(iperf -c 127.0.0.1 -p 8005 -u -t 1 | tail -1 | awk '{print $8}') |bc)

	arrNodes=( ["9001"]="9001=$B9001" 
		   ["9003"]="9003=$B9003" 
		   ["9004"]="9004=$B9004" 
		   ["9005"]="9005=$B9005" 
		 )

	Latency=$(join_by , ${arrNodes[@]})
	#echo $Latency

	Battery=100
	#$(upower -i $(upower -e | grep BAT) | grep --color=never -E percentage|xargs|cut -d' ' -f2|sed s/%//)

	#DATE_WITH_TIME=`date "+%Y%m%d-%H%M"` #%S%3N

	Content="$MyNodeName|$HostedSensor|$Latency|$Battery"

	#echo $Content

	#Set Content at parent (so that parent can fetch later):
	xterm -e $CCNL_HOME/bin/ccn-lite-simplenfn -s ndn2013 -u $ParentIP/$ParentPort -w 30 "call 3 /node/$ParentNodeName/nfn_service_UpdateNodeState '$MyPort' '$Content'" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3 &

	arrNodes=()

    sleep 50
done

