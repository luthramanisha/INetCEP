#!/usr/bin/python

# File name: manifest_to_config.py
# Author: Sebastian Hennig
# Modified by: Manisha Luthra
# Date created: 18.07.2018
# Python Version: 2.7
# Description: Parses the Manifest XML file downloaded from GENI and outputs
# the hosts IP addresses in the config file format
# Usage python manifest_to_config.py <manifest.xml>

import os
import sys
import re
import xml.etree.ElementTree
import socket
import string
from shutil import copy

project_root = os.path.join(os.path.dirname(os.path.realpath(__file__)), "..")
e = xml.etree.ElementTree.parse(sys.argv[1]).getroot()
ip_array = []

# Iterate through all nodes in the array
for child in e.findall('{http://www.geni.net/resources/rspec/3}node'):
    ip_array.append(child.find('{http://www.geni.net/resources/rspec/3}host').attrib['ipv4'])

#sort by IP address
ip_array = sorted(ip_array, key=lambda x:tuple(map(int, x.split('.'))))

VMS_str = "("
for ip in ip_array:
    VMS_str += "\"" + ip + "\" "
VMS_str += ")"

config_file = open(os.path.join(project_root, "scripts/templates/VMS.cfg"), "r")
config = config_file.read()
config_file.close()

config = config.replace("{{VMS}}", VMS_str)

print(os.path.join(project_root, "VMS.cfg"))
config_file = open(os.path.join(project_root, "VMS.cfg"), "w")
config_file.write(config)
config_file.close()

start_port = 9001
node_id_array = list(string.ascii_uppercase)
node_info_file = open(os.path.join(project_root, "scripts/templates/nodeInformation"), "r")
template_node = node_info_file.read()
node_info_file.close()

nodes = ""
# Add nodes with ascending IPs with range of ip_array
for i in range(0, len(ip_array)):
    node = template_node
    node = node.replace("{{id}}", (node_id_array[i]))\
               .replace("{{port}}", (str(start_port)))\
	  .replace("{{IP}}", (ip_array[i]))	
    start_port +=1
    nodes += node

template_node = template_node.replace("node{{id}}-{{port}}-{{IP}}", nodes)

if not os.path.exists(project_root+"/nodeData"):
    os.mkdir(project_root+"/nodeData")

copy(project_root+"/scripts/templates/Decentralized_KHop", project_root+"/nodeData/")
copy(project_root+"/scripts/templates/placementUtilityFunction", project_root+"/nodeData/")
copy(project_root+"/scripts/templates/queryStore", project_root+"/nodeData/")

# Write the nodeInformation file
out_file = open(project_root + "/nodeData/nodeInformation", "w")
out_file.write(template_node)
out_file.close()
print(os.path.join(project_root, "/nodeData/nodeInformation"))

for i in range(0, len(ip_array)):
    node_file = open(project_root + "/nodeData/node"+node_id_array[i], "w")
    node_file.write(template_node)
    node_file.close()
    print(os.path.join(project_root, "/nodeData/node"+node_id_array[i]))


