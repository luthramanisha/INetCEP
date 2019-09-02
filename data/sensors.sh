#!/bin/bash
highest=99
lowest=60
mkdir -p /home/veno/Thesis/sampledata/ && touch /home/veno/Thesis/sampledata/sensor1
touch /home/veno/Thesis/sampledata/sensor2 && touch /home/veno/Thesis/sampledata/sensor3
touch /home/veno/Thesis/sampledata/sensor4 && touch /home/veno/Thesis/sampledata/sensor5

> /home/veno/Thesis/sampledata/sensor1
> /home/veno/Thesis/sampledata/sensor2
> /home/veno/Thesis/sampledata/sensor3
> /home/veno/Thesis/sampledata/sensor4
> /home/veno/Thesis/sampledata/sensor5

# Define timestamp function
timestamp() {
  date +"%H:%M:%S.%3N"
}

while true; do 
	newrand1=$[ ( $RANDOM % ( $[ $highest - $lowest ] + 1 ) ) + $lowest ]
	newrand2=$[ ( $RANDOM % ( $[ $highest - $lowest ] + 1 ) ) + $lowest ]
	newrand3=$[ ( $RANDOM % ( $[ $highest - $lowest ] + 1 ) ) + $lowest ]
	newrand4=$[ ( $RANDOM % ( $[ $highest - $lowest ] + 1 ) ) + $lowest ]
	newrand5=$[ ( $RANDOM % ( $[ $highest - $lowest ] + 1 ) ) + $lowest ]
	echo $(timestamp)"/"$newrand1 >> /home/veno/Thesis/sampledata/sensor1
	echo $(timestamp)"/"$newrand2 >> /home/veno/Thesis/sampledata/sensor2
	echo $(timestamp)"/"$newrand3 >> /home/veno/Thesis/sampledata/sensor3
	echo $(timestamp)"/"$newrand4 >> /home/veno/Thesis/sampledata/sensor4
	echo $(timestamp)"/"$newrand5 >> /home/veno/Thesis/sampledata/sensor5
	#tsEpoch=$(($(date +'%s * 1000 + %-N / 1000000')))
	#echo $tsEpoch"/"$newrand1 >> /home/veno/Thesis/sampledata/sensor1
	#echo $tsEpoch"/"$newrand2 >> /home/veno/Thesis/sampledata/sensor2
	#echo $tsEpoch"/"$newrand3 >> /home/veno/Thesis/sampledata/sensor3
	#echo $tsEpoch"/"$newrand4 >> /home/veno/Thesis/sampledata/sensor4
	#echo $tsEpoch"/"$newrand5 >> /home/veno/Thesis/sampledata/sensor5

	$CCNL_HOME/bin/ccn-lite-mkC -i /home/veno/Thesis/sampledata/sensor1 -o $CCNL_HOME/test/ali_tlv/sensordata/sensor1.ccntlv -s ccnx2015 "/ndn/sensors/s1"
	$CCNL_HOME/bin/ccn-lite-mkC -i /home/veno/Thesis/sampledata/sensor2 -o $CCNL_HOME/test/ali_tlv/sensordata/sensor2.ccntlv -s ccnx2015 "/ndn/sensors/s2"
	$CCNL_HOME/bin/ccn-lite-mkC -i /home/veno/Thesis/sampledata/sensor3 -o $CCNL_HOME/test/ali_tlv/sensordata/sensor3.ccntlv -s ccnx2015 "/ndn/sensors/s3"
	$CCNL_HOME/bin/ccn-lite-mkC -i /home/veno/Thesis/sampledata/sensor4 -o $CCNL_HOME/test/ali_tlv/sensordata/sensor4.ccntlv -s ccnx2015 "/ndn/sensors/s4"
	$CCNL_HOME/bin/ccn-lite-mkC -i /home/veno/Thesis/sampledata/sensor5 -o $CCNL_HOME/test/ali_tlv/sensordata/sensor5.ccntlv -s ccnx2015 "/ndn/sensors/s5"

	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-a.sock addContentToCache $CCNL_HOME/test/ali_tlv/sensordata/sensor1.ccntlv
	#sleep 1
	#$CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-a.sock addContentToCache $CCNL_HOME/test/ali_tlv/sensordata/sensor2.ccntlv
	#sleep 1
	# $CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-a.sock addContentToCache $CCNL_HOME/test/ali_tlv/sensordata/sensor3.ccntlv
	# sleep 1
	# $CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-a.sock addContentToCache $CCNL_HOME/test/ali_tlv/sensordata/sensor4.ccntlv
	# sleep 1
	# $CCNL_HOME/bin/ccn-lite-ctrl -x /tmp/mgmt-nfn-relay-a.sock addContentToCache $CCNL_HOME/test/ali_tlv/sensordata/sensor5.ccntlv
	# sleep 1

	echo "Sensor 1: "$newrand1" | Sensor 2: "$newrand2" | Sensor 3: "$newrand3" | Sensor 4: "$newrand4" | Sensor 5: "$newrand5
    sleep 2
done
