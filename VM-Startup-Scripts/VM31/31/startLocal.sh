#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Update one node (in this case 9001) with network state (heartbeats)
./updateNodeState_9004.sh  ${VMS[3]} 9004 nodeD 9004 9004 victims QueryCentralLocalNS

