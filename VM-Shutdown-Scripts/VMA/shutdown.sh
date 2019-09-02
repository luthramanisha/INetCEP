#!/bin/bash

#shutdown the relay 
echo "shutdown the relay"
$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-a.sock debug halt | $CCNL_HOME/bin/ccn-lite-ccnb2xml

#remove face 
echo "remove face"
$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-a.sock debug dump | $CCNL_HOME/bin/ccn-lite-ccnb2xml

#destroy face
echo "destroy face"
$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-a.sock destroyface $FACEID | $CCNL_HOME/bin/ccn-lite-ccnb2xml
