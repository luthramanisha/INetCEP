#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9001.sh ${VMS[0]} 9001 nodeA 9001 9001 victims QueryCentralRemNS
