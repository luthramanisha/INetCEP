#!/bin/bash
IP=$(hostname -I)
NODE_G="$HOME/MA-Ali/computeservers/nodes/nodeG"
sleep 0.1
echo "Compute Server Updated"

#Start Compute Server
java -jar "$NODE_G"/nfn-assembly-0.2.0.jar --mgmtsocket /tmp/mgmt-nfn-relay-g.sock --ccnl-addr "$IP" --ccnl-port 9007 --cs-port 9017 --debug --ccnl-already-running /node/nodeG
