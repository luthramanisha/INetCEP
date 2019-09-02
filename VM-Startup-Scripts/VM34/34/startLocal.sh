#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Update one node (in this case 9001) with network state (heartbeats)
./updateNodeState_9007.sh ${VMS[6]} 9007 nodeG 9007 9007 victims QueryCentralLocalNS
