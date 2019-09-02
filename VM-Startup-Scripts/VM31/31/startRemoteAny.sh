#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
queryType=$1
interval=$2
#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9004.sh ${VMS[3]} 9004 nodeD 9004 9004 victims $queryType $interval
