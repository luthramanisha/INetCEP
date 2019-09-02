#!/bin/bash

#Every node updates itself with network state (node that receives a query will fetch network state)
xterm -iconic -e "bash -c \"./updateNodeState_9001.sh 127.0.0.1 9001 nodeA 9001 9001 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9002.sh 127.0.0.1 9002 nodeB 9002 9002 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9003.sh 127.0.0.1 9003 nodeC 9003 9003 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9004.sh 127.0.0.1 9004 nodeD 9004 9004 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9005.sh 127.0.0.1 9005 nodeE 9005 9005 victims; exec bash\"" &
xterm -iconic -e "bash -c \"./updateNodeState_9006.sh 127.0.0.1 9006 nodeF 9006 9006 victims; exec bash\"" 
