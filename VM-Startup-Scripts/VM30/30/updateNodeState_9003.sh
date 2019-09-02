#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
ParentIP=$1
ParentPort=$2
ParentNodeName=$3
MyPort=$4
MyNodeName=$5
HostedSensor=$6
QueryType=$7
Interval=$8
if [[ -z $Interval ]]
then
	#default interval
	Interval=20
fi

./queryService.sh $ParentIP $ParentPort $ParentNodeName $QueryType $Interval &
echo 'Query Service Started'

function join_by { local IFS="$1"; shift; echo "$*"; }

declare -A arrNodes

#9003 node will start iperf on 8003:
nohup iperf -s -p 8003 -u  &

#replacing this read with a sleep of 30 (for automated scripts)
#read -p 'iperf Started - Other nodes up? [y/n] ' Identifier
echo 'Waiting for 30 seconds..'
sleep 30
echo 'Update node state is ready to begin..'
#After this sleep elapses, we can assume that the iperf services on the other nodes have already been started.

#Simulated: Starting Battery Life = 100
Battery=100

while true
do 

	#3 --> 2
	B9002=$(echo $(ping -c 1 ${VMS[1]} | tail -1| awk '{print $4 / 2}' | cut -d '/' -f 2) \* $(iperf -c ${VMS[1]} -p 8002 -u -t 1 | tail -1 | awk '{print $8}') |bc)
	B9007=$(echo $(ping -c 1 ${VMS[6]} | tail -1| awk '{print $4 / 2}' | cut -d '/' -f 2) \* $(iperf -c ${VMS[6]} -p 8007 -u -t 1 | tail -1 | awk '{print $8}') |bc)
	arrNodes=( ["9002"]="9002=$B9002" 
		   ["9007"]="9007=$B9007"
		 )

	Latency=$(join_by , ${arrNodes[@]})
	#echo $Latency

	#Important to note that if power_supply BAT0 does not exist then this device is NOT a mobile device. This is true for VM's, which is why we are going to simulate lowering battery life:
	#Battery=$(cat /sys/class/power_supply/BAT0/capacity)
	#$(cat /sys/class/power_supply/BAT0/capacity)
	#$(upower -i $(upower -e | grep BAT) | grep --color=never -E percentage|xargs|cut -d' ' -f2|sed s/%//)
	
	#Simulate Battery Life:
	if (( $Battery == 0 ))
	then
		Battery=100
	fi

	time=$(echo `date "+%M"` | bc)
	if (( $time % 5 == 0 ))
	then
	    Battery=`expr $Battery - 1`
	fi
	
	DATE_WITH_TIME=`date "+%Y%m%d-%H%M%S%3N"` #%S%3N

	Content="$MyNodeName|$HostedSensor|$Latency|$Battery"

	#echo $Content

	#Set Content at parent (so that parent can fetch later):
	#timeout 10 $CCNL_HOME/bin/ccn-lite-simplenfn -s ndn2013 -u $ParentIP/$ParentPort -w 10 "call 4 /node/$ParentNodeName/nfn_service_UpdateNodeState '$MyPort' '$Content' '$DATE_WTIH_TIME'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3
	timeout 10 $CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u $ParentIP/$ParentPort -w 10 "" "call 4 /node/$ParentNodeName/nfn_service_UpdateNodeState '$MyPort' '$Content' '$DATE_WTIH_TIME'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 3
	if [ $? -eq 124 ]; then
	    # Timeout occurred
		echo `date "+%Y-%m-%d %H:%M:%S.%3N"` ': (Update Node State) Request timed out after 10 seconds -' $?
	else
	    # No hang
		echo `date "+%Y-%m-%d %H:%M:%S.%3N"` ': (Update Node State) Request Successful'
	fi

	arrNodes=()

    sleep 50
done
