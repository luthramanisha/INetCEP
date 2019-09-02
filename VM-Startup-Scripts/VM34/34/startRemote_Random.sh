#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9007.sh ${VMS[6]} 9007 nodeG 9007 9007 victims QueryRandom
