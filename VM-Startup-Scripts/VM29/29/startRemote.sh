#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9002.sh ${VMS[1]} 9002 nodeB 9002 9002 victims QueryCentralRemNS

