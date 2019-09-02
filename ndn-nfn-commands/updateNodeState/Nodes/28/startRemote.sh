#!/bin/bash

#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9001.sh 10.2.1.28 9001 nodeA 9001 9001 victims
