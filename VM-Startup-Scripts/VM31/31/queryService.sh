#!/bin/bash

#This script has to be started on each node capable of handling queries to utilize the queryStore and execute the commands after an interval.
input="$HOME/MA-Ali/nodeData/queryStore"

rm $HOME/MA-Ali/nodeData/queryStore

ParentIP=$1
ParentPort=$2
ParentNodeName=$3
QueryType=$4
Interval=$5

newRunID=0
while true
do 

#check if file exists:
if [ -f "$input" ]; then

#read the file line-by-line (each line contains a user query)
while IFS= read -r var
do
	echo `date "+%Y-%m-%d %H:%M:%S.%3N"` ': Reading Query Store' 
	ID=$(echo $var | awk '{print $1;}' )
	algorithm=$(echo $var | awk '{print $2;}' )
	runID=$(echo $var | awk '{print $3;}' )
	sourceOfQuery=$(echo $var | awk '{print $4;}' )
	clientID=$(echo $var | awk '{print $5;}' )
	query=$(echo $var | awk '{print $6;}' )
	region=$(echo $var | awk '{print $7;}' )
	#timestamp=$(echo $var | awk '{print $8;}' )
	timestamp=`date "+%H:%M:%S.%3N"` #use current time

	if (( $newRunID == 0 ))
	then
	    newRunID=`expr $runID + 1`
	else
	    newRunID=`expr $newRunID + 1`
	fi

	#Here sourceOfQuery = 'QS' signifies that this query is part of the re-computation process
	#timeout 20 $CCNL_HOME/bin/ccn-lite-simplenfn -s ndn2013 -u $ParentIP/$ParentPort -w 20 "" "call 8 /node/$ParentNodeName/nfn_service_$QueryType '$algorithm' '$newRunID' 'QS' '$clientID' '$query' '$region' '$timestamp'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2	
	timeout 20 $CCNL_HOME/bin/ccn-lite-peek -s ndn2013 -u $ParentIP/$ParentPort -w 20 "" "call 8 /node/$ParentNodeName/nfn_service_$QueryType '$algorithm' '$newRunID' 'QS' '$clientID' '$query' '$region' '$timestamp'/NFN" | $CCNL_HOME/bin/ccn-lite-pktdump -f 2
	if [ $? -eq 124 ]; then
	    # Timeout occurred
		echo `date "+%Y-%m-%d %H:%M:%S.%3N"` ': Request timed out after 20 seconds -' $?
	else
	    # No hang
		echo `date "+%Y-%m-%d %H:%M:%S.%3N"` ': Request Successful'
		#$? //This is the exit status = 0
		#get the result (future work, manage this result and send it back to the clientID)
	fi

done < "$input"
#input is the file that we have to read. In this case, the queryStore (that is created during query execution)

fi
sleep $Interval

echo `date "+%Y-%m-%d %H:%M:%S.%3N"` ': Query Store processed - Next update scheduled after (3) minutes' 

done
