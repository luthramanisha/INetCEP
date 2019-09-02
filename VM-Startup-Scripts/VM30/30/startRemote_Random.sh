#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9003.sh  ${VMS[2]} 9003 nodeC 9003 9003 victims QueryRandom
