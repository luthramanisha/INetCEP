#!/bin/bash

ParentIP=$1
ParentPort=$2
ParentNodeName=$3
MyPort=$4
MyNodeName=$5
HostedSensor=$6

function join_by { local IFS="$1"; shift; echo "$*"; }

declare -A arrNodes

#9006 node will start iperf on 8006:
nohup iperf -s -p 8006 -u  &

read -p 'iperf Started - Other nodes up? [y/n] ' Identifier

while true
do 

	#6 --> 5
	B9005=$(echo $(ping -c 1 10.2.1.32 | tail -1| awk '{print $4 / 2}' | cut -d '/' -f 2) \* $(iperf -c 10.2.1.32 -p 8005 -u -t 1 | tail -1 | awk '{print $8}') |bc)

	arrNodes=( ["9005"]="9005=$B9005" 
		 )

	#instead of ping, use iperf

	Latency=$(join_by , ${arrNodes[@]})
	#echo $Latency

	Battery=100
	#$(upower -i $(upower -e | grep BAT) | grep --color=never -E percentage|xargs|cut -d' ' -f2|sed s/%//)

	#DATE_WITH_TIME=`date "+%Y%m%d-%H%M"` #%S%3N

	Content="$MyNodeName|$HostedSensor|$Latency|$Battery"

	#echo $Content

	#Set Content at parent (so that parent can fetch later):
	timeout 10 $CCNL_HOME/bin/ccn-lite-simplenfn -s ndn2013 -u $ParentIP/$ParentPort -w 10 "call 3 /node/$ParentNodeName/nfn_service_UpdateNodeState '$MyPort' '$Content'" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3

	arrNodes=()

    sleep 50
done
