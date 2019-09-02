#!/bin/bash
IP=$(hostname -I)
NODE_C="$HOME/MA-Ali/computeservers/nodes/nodeC"

sleep 0.1
echo "Compute Server Updated"

#Start Compute Server
java -jar "$NODE_C"/nfn-assembly-0.2.0.jar --mgmtsocket /tmp/mgmt-nfn-relay-c.sock --ccnl-addr "$IP" --ccnl-port 9003 --cs-port 9013 --debug --ccnl-already-running /node/nodeC
