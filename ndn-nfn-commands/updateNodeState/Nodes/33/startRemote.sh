#!/bin/bash

#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9006.sh 10.2.1.33 9006 nodeF 9006 9006 victims
