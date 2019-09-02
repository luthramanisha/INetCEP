#!/bin/bash

mkdir /var/tmp/nodeData
mkdir /var/tmp/sensors

touch /var/tmp/nodeData/nodeInformation
touch /var/tmp/nodeData/placementUtilityFunction

echo "nodeA-9001
nodeB-9002
nodeC-9003
nodeD-9004
nodeE-9005
nodeF-9006" > /var/tmp/nodeData/nodeInformation

echo "0.8|0.2" > /var/tmp/nodeData/placementUtilityFunction

touch /var/tmp/sensors/buildings
touch /var/tmp/sensors/infrastructure
touch /var/tmp/sensors/victims
touch /var/tmp/sensors/survivors
touch /var/tmp/sensors/vulnerability

echo "22:18:38.841/Building A/O/L
22:18:39.841/Building B/R/H
22:18:40.841/Building C/C/M
22:18:41.841/Building D/O/H
22:18:42.841/Building E/R/L
22:18:43.841/Building F/C/M" > /var/tmp/sensors/buildings 

echo "22:18:38.841/Tower A/Y
22:18:39.841/Road B/N
22:18:40.841/Tower C/Y
22:18:41.841/Road D/N
22:18:42.841/Tower E/Y
22:18:43.841/Road F/N" > /var/tmp/sensors/infrastructure

echo "22:18:38.841/2001/M/51
22:18:39.841/2002/F/41
22:18:40.841/2003/F/21
22:18:41.841/2004/M/18
22:18:42.841/2005/F/16
22:18:43.841/2006/M/35" > /var/tmp/sensors/survivors

echo "22:18:38.841/1001/M/50
22:18:39.841/1002/F/40
22:18:40.841/1003/F/22
22:18:41.841/1004/M/8
22:18:42.841/1005/F/6
22:18:43.841/1006/M/25" > /var/tmp/sensors/victims

echo "22:18:38.841/10/7/3/7
22:18:39.841/11/8/3/9
22:18:40.841/11/8/8/9
22:18:41.841/15/9/8/11
22:18:42.841/18/9/10/14
22:18:43.841/19/9/10/18" > /var/tmp/sensors/vulnerability



