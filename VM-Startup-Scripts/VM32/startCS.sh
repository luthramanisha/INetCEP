#!/bin/bash
IP=$(hostname -I)
NODE_E="$HOME/MA-Ali/computeservers/nodes/nodeE"

sleep 0.1
echo "Compute Server Updated"

#Start Compute Server
java -jar "$NODE_E"/nfn-assembly-0.2.0.jar --mgmtsocket /tmp/mgmt-nfn-relay-e.sock --ccnl-addr "$IP" --ccnl-port 9005 --cs-port 9015 --debug --ccnl-already-running /node/nodeE
