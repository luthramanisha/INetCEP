#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../"
source "$work_dir/VMS.cfg"

#Start nodes:
#CCN-Lite-Old Version
$CCNL_HOME/bin/ccn-nfn-relay -v trace -u 9004 -x /tmp/mgmt-nfn-relay-d.sock &
#$CCNL_HOME/bin/ccn-lite-relay -v trace -u 9004 -x /tmp/mgmt-nfn-relay-a.sock &
sleep 0.1
echo "Relay started in BG"

#----------------------------|
#Create Faces and Link nodes:
#----------------------------|
#9004 -> 9001
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /node/nodeA $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /9001 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9004 -> 9002
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /node/nodeB $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /9001 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9004 -> 9003
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /node/nodeC $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /9003 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9004 -> 9005
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /node/nodeE $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /9005 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9004 -> 9006
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock newUDPface any ${VMS[5]} 9006 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /node/nodeF $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /9006 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9004 -> 9007
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock newUDPface any ${VMS[1]} 9002 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /node/nodeG $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-d.sock prefixreg /9006 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1
#-------------------------|

