#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Update one node (in this case 9001) with network state (heartbeats)
./updateNodeState_9006.sh ${VMS[5]} 9006 nodeF 9006 9006 victims QueryCentralLocalNS
