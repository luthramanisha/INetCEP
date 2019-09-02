#!/bin/bash

IP=$1
Port=$2
NodeName=$3
HostedSensor=$4

function join_by { local IFS="$1"; shift; echo "$*"; }

declare -A arrNodes

xterm -hold -e iperf -s -p 8002 -u  &
sleep 1

while true
do 

	L9001=$(ping -c 1 127.0.0.1 | tail -1| awk '{print $4}' | cut -d '/' -f 2)
	L9003=$(ping -c 1 127.0.0.1 | tail -1| awk '{print $4}' | cut -d '/' -f 2)
	L9004=$(ping -c 1 127.0.0.1 | tail -1| awk '{print $4}' | cut -d '/' -f 2)
	B9001=$(iperf -c 127.0.0.1 -p 8001 -u -t 1 | tail -1 | awk '{print $8}')
	B9003=$(iperf -c 127.0.0.1 -p 8001 -u -t 1 | tail -1 | awk '{print $8}')
	B9004=$(iperf -c 127.0.0.1 -p 8001 -u -t 1 | tail -1 | awk '{print $8}')

	arrNodes=( ["9001"]="9001=$($L9001*$B9001)"
		   ["9003"]="9003=$($L9003*$B9003)"
		   ["9004"]="9004=$($L9004*$B9004)" 
		 )

	#instead of ping, use iperf

	Latency=$(join_by , ${arrNodes[@]})
	#echo $Latency

	Battery=$(upower -i $(upower -e | grep BAT) | grep --color=never -E percentage|xargs|cut -d' ' -f2|sed s/%//)

	Content="$NodeName|$HostedSensor|$Latency|$Battery"
	DATE_WITH_TIME=`date "+%Y%m%d-%H%M"` #%S%3N

	echo $Content

	$CCNL_HOME/bin/ccn-lite-simplenfn -s ndn2013 -u $IP/$Port -w 10 "call 3 /node/$NodeName/nfn_service_UpdateNodeState '$Port' '$Content'" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3

	#$CCNL_HOME/bin/ccn-lite-simplenfn -s ndn2013 -u $IP/$Port -w 10 "call 4 /node/$NodeName/nfn_service_SetData '/status/$NodeName/nodeStatus' '$Content' '$DATE_WITH_TIME'" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3

	arrNodes=()

    sleep 60
done

