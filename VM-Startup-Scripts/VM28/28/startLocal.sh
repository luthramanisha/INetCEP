#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Update one node (in this case 9001) with network state (heartbeats)
./updateNodeState_9001.sh ${VMS[0]} 9001 nodeA 9001 9001 victims QueryCentralLocalNS
