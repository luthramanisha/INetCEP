#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Update one node (in this case 9002) with network state (heartbeats)
./updateNodeState_9002.sh ${VMS[1]} 9002 nodeB 9002 9002 victims QueryCentralLocalNS
