#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Update one node (in this case 9001) with network state (heartbeats)
./updateNodeState_9005.sh ${VMS[4]} 9005 nodeE 9005 9005 victims QueryCentralLocalNS
