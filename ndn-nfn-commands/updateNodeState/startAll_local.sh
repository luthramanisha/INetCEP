#!/bin/bash

#Update one node (in this case 9001) with network state (heartbeats)
xterm -iconic -e "bash -c \"./updateNodeState_9001.sh 127.0.0.1 9001 nodeA 9001 9001 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9002.sh 127.0.0.1 9001 nodeA 9002 9002 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9003.sh 127.0.0.1 9001 nodeA 9003 9003 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9004.sh 127.0.0.1 9001 nodeA 9004 9004 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9005.sh 127.0.0.1 9001 nodeA 9005 9005 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9006.sh 127.0.0.1 9001 nodeA 9006 9006 victims; exec bash\"" 
