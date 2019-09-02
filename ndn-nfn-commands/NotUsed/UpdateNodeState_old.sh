#!/bin/bash

echo $1
IP=$1
Port=$2
NodeName=$3
HostedSensor=$4

function join_by { local IFS="$1"; shift; echo "$*"; }


declare -A arrNodes
while true
do 
	arrNodes["9001"]=$(ping -c 1 127.0.0.1 | tail -1| awk '{print $4}' | cut -d '/' -f 2)
	arrNodes+=( arr["9002"]=$(ping -c 1 127.0.0.1 | tail -1| awk '{print $4}' | cut -d '/' -f 2))
	arrNodes+=( arr["9003"]=$(ping -c 1 127.0.0.1 | tail -1| awk '{print $4}' | cut -d '/' -f 2))

	Latency=(join_by , "${arrNodes[@]}")

	Battery=$(upower -i $(upower -e | grep BAT) | grep --color=never -E percentage|xargs|cut -d' ' -f2|sed s/%//)

	Content=$($NodeName'|'$HostedSensor'|'$Latency'|'$Battery)
	DATE_WITH_TIME=`date "+%Y%m%d-%H%M"` #%S%3N

	echo $Latency ' - ' $Battery ' - ' $Content ' - ' $DATE_WITH_TIME

	echo $CCNL_HOME/bin/ccn-lite-simplenfn -s ndn2013 -u $IP/$Port -w 10 "call 3 /node/$NodeName/nfn_service_UpdateNodeState '$NodeName' '$Content'" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3

	#$CCNL_HOME/bin/ccn-lite-simplenfn -s ndn2013 -u $IP/$Port -w 10 "call 4 /node/$NodeName/nfn_service_SetData '/status/$NodeName/nodeStatus' '$Content' '$DATE_WITH_TIME'" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3

	arrNodes=()

    sleep 60
done

