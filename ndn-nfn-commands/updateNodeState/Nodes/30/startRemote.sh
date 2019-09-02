#!/bin/bash

#Every node updates itself with network state (node that receives a query will fetch network state)
./updateNodeState_9003.sh 10.2.1.30 9003 nodeC 9003 9003 victims
