#!/bin/bash

#Update one node (in this case 9001) with network state (heartbeats)
xterm -iconic -e "bash -c \"./updateNodeState_9001_new.sh 127.0.0.1 9001 nodeA 9001 nodeA victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9002_new.sh 127.0.0.1 9001 nodeA 9002 nodeB victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9003_new.sh 127.0.0.1 9001 nodeA 9003 nodeC victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9004_new.sh 127.0.0.1 9001 nodeA 9004 nodeD victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9005_new.sh 127.0.0.1 9001 nodeA 9005 nodeE victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9006_new.sh 127.0.0.1 9001 nodeA 9006 nodeF victims; exec bash\"" 
