#!/bin/bash
#get IP address referred https://stackoverflow.com/questions/8529181/which-terminal-command-to-get-just-ip-address-and-nothing-else
IP=$(hostname -I)
NODE_A="$HOME/MA-Ali/computeservers/nodes/nodeA"

sleep 0.1
echo "Compute Server Updated"

#Start Compute Server
java -jar "$NODE_A"/nfn-assembly-0.2.0.jar --mgmtsocket /tmp/mgmt-nfn-relay-a.sock --ccnl-addr "$IP" --ccnl-port 9001 --cs-port 9011 --debug --ccnl-already-running /node/nodeA
