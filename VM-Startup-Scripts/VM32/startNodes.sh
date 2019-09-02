#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../"
source "$work_dirVMS.cfg"

#Start nodes:
#CCN-Lite-Old Version
$CCNL_HOME/bin/ccn-nfn-relay -v trace -u 9005 -x /tmp/mgmt-nfn-relay-e.sock &
#$CCNL_HOME/bin/ccn-lite-relay -v trace -u 9005 -x /tmp/mgmt-nfn-relay-a.sock &
sleep 0.1
echo "Relay started in BG"

#----------------------------|
#Create Faces and Link nodes:
#----------------------------|
#9005 -> 9001
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /node/nodeA $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /9001 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9005 -> 9002
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /node/nodeB $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /9002 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9005 -> 9003
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /node/nodeC $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /9003 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9005 -> 9004
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /node/nodeD $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /9004 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9005 -> 9006
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /node/nodeF $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /9006 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9005 -> 9007
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /node/nodeG $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-e.sock prefixreg /9007 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1
#-------------------------|

