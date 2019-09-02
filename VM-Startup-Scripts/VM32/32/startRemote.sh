#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9005.sh ${VMS[4]} 9005 nodeE 9005 9005 victims QueryCentralRemNS
 
