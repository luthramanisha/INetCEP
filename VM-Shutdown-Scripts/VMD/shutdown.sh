#!/bin/bash

#shutdown the relay 
$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock debug halt | $CCNL_HOME/bin/ccn-lite-ccnb2xml

#remove face 
$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock debug dump | $CCNL_HOME/bin/ccn-lite-ccnb2xml

#destroy face
$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock destroyface $FACEID | $CCNL_HOME/bin/ccn-lite-ccnb2xml
