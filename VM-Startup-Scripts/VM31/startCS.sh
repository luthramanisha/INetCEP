#!/bin/bash
IP=$(hostname -I)
NODE_D="$HOME/MA-Ali/computeservers/nodes/nodeD"

sleep 0.1
echo "Compute Server Updated"

#Start Compute Server
java -jar "$NODE_D"/nfn-assembly-0.2.0.jar --mgmtsocket /tmp/mgmt-nfn-relay-d.sock --ccnl-addr "$IP" --ccnl-port 9004 --cs-port 9014 --debug --ccnl-already-running /node/nodeD
