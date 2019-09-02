#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../"
source "$work_dir/VMS.cfg"

#Start nodes:
#CCN-Lite-Old Version
$CCNL_HOME/bin/ccn-nfn-relay -v trace -u 9006 -x /tmp/mgmt-nfn-relay-f.sock &
#$CCNL_HOME/bin/ccn-lite-relay -v trace -u 9006 -x /tmp/mgmt-nfn-relay-a.sock &
sleep 0.1
echo "Relay started in BG"

#----------------------------|
#Create Faces and Link nodes:
#----------------------------|
#9006 -> 9001
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock newUDPface any ${VMS[3]} 9004 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /node/nodeA $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /9001 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9006 -> 9002
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock newUDPface any ${VMS[3]} 9004 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /node/nodeB $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /9002 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9006 -> 9003
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock newUDPface any ${VMS[3]} 9004 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /node/nodeC $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /9003 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9006 -> 9004
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock newUDPface any ${VMS[3]} 9004 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /node/nodeD $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /9004 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9006 -> 9005
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock newUDPface any ${VMS[3]} 9004 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /node/nodeE $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /9004 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1

#9006 -> 9007
	FACEID=`$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock newUDPface any ${VMS[3]} 9004 \
	  | $CCNL_HOME/bin/ccn-lite-ccnb2xml | grep FACEID | sed -e 's/^[^0-9]*\([0-9]\+\).*/\1/'`
	sleep 0.1
	$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /node/nodeG $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-f.sock prefixreg /9007 $FACEID ndn2013   | $CCNL_HOME/bin/ccn-lite-ccnb2xml
	sleep 0.1
#-------------------------|

