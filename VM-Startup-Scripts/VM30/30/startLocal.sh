#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../../"
source "$work_dir/VMS.cfg"
#Update one node (in this case 9001) with network state (heartbeats)
./updateNodeState_9003.sh  ${VMS[2]} 9003 nodeC 9003 9003 victims QueryCentralLocalNS

