#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9006.sh ${VMS[5]} 9006 nodeF 9006 9006 victims QueryDecentral
