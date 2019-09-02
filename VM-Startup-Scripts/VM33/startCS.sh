#!/bin/bash
IP=$(hostname -I)
NODE_F="$HOME/MA-Ali/computeservers/nodes/nodeF"

sleep 0.1
echo "Compute Server Updated"

#Start Compute Server
java -jar "$NODE_F"/nfn-assembly-0.2.0.jar --mgmtsocket /tmp/mgmt-nfn-relay-f.sock --ccnl-addr "$IP" --ccnl-port 9006 --cs-port 9016 --debug --ccnl-already-running /node/nodeF
