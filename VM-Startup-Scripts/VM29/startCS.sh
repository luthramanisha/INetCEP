#!/bin/bash
IP=$(hostname -I)
NODE_B="$HOME/MA-Ali/computeservers/nodes/nodeB"

sleep 0.1
echo "Compute Server Updated"

#Start Compute Server
java -jar "$NODE_B"/nfn-assembly-0.2.0.jar --mgmtsocket /tmp/mgmt-nfn-relay-b.sock --ccnl-addr "$IP" --ccnl-port 9002 --cs-port 9012 --debug --ccnl-already-running /node/nodeB
