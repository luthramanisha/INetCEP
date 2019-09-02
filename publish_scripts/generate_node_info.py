#!/usr/bin/python

# File name: generate_node_info.py
# Description: generates node information (routing tables) to be used by the compute server in nodeData/
# Author: Manisha Luthra

import os
import sys
import re
import xml.etree.ElementTree
import socket
import string
from shutil import copy

project_root = os.path.join(os.path.dirname(os.path.realpath(__file__)), "..")
ip_array = []
in_file = open(os.path.join(project_root, "VMS.cfg"), "r")
contents = in_file.read()
in_file.close()

lines = contents.split('=')

for i in lines:
	if "(" in i:
		ip_array = i.replace("(", "")\
			.replace(")\n", "")\
			.replace('"','')\
			.split(" ")
		print(ip_array)

start_port = 9001
node_id_array = list(string.ascii_uppercase)
node_info_file = open(os.path.join(project_root, "publish_scripts/GENI/templates/nodeInformation"), "r")
template_node = node_info_file.read()
node_info_file.close()

nodes = ""
# serial no of hops information (TODO Patrick: either move it to the system or generate for different topologies)
# currently generated for the topology used in current eval
node_hops = [["0", "1", "2", "2", "2", "3", "3"],\
            ["1", "0", "1", "1", "1", "2", "2"],\
            ["2", "1", "0", "2", "2", "3", "1"],\
            ["2", "1", "2", "0", "2", "3", "3"],\
            ["2", "1", "2", "2", "0", "1", "3"],\
            ["3", "2", "3", "3", "1", "0", "4"],\
            ["3", "2", "1", "3", "3", "4", "0"]]

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

copy(project_root+"/publish_scripts/GENI/templates/Decentralized_KHop", project_root+"/nodeData/")
copy(project_root+"/publish_scripts/GENI/templates/placementUtilityFunction", project_root+"/nodeData/")
copy(project_root+"/publish_scripts/GENI/templates/queryStore", project_root+"/nodeData/")

# Write the nodeInformation file
out_file = open(project_root + "/nodeData/nodeInformation", "w")
out_file.write(template_node)
out_file.close()
print(os.path.join(project_root, "/nodeData/nodeInformation"))


# Write in each node file
for j in range(0, len(ip_array)):
    node_file = open(os.path.join(project_root, "publish_scripts/templates/node"), "r")
    template_hops = node_file.read()
    node_file.close()
    hops = ""
    start_port = 9001
    for i in range(0, len(ip_array)):
        hop = template_hops
        hop = hop.replace("{{id}}", (node_id_array[i]))\
                   .replace("{{port}}", (str(start_port)))\
	      .replace("{{hops}}", (node_hops[j][i]))\
	      .replace("{{IP}}", (ip_array[i]))
        hops += hop
        start_port +=1
        	
    #print(hops)
    template_hops = template_hops.replace("node{{id}}-{{port}}-{{hops}}-{{IP}}", hops)
    node_file = open(project_root + "/nodeData/node"+node_id_array[j], "w")
    node_file.write(template_hops)
    node_file.close()
    print(os.path.join(project_root, "/nodeData/node"+node_id_array[j]))

#for i in range(0, len(ip_array)):
#    node_file = open(project_root + "/nodeData/node"+node_id_array[i], "w")
#    node_file.write(template_node)
#    node_file.close()
#    print(os.path.join(project_root, "/nodeData/node"+node_id_array[i]))

shutdown_script = open(os.path.join(project_root, "publish_scripts/templates/shutdown.sh"), "r")
shutdown_template = shutdown_script.read()
shutdown_script.close()

# Write a shutdown script for each node
for i in range(0, len(ip_array)):
    node_id = shutdown_template.replace("{{id}}", node_id_array[i].lower())
    #node_ids += node_id
    if not os.path.exists(project_root + "/VM-Shutdown-Scripts/VM" + node_id_array[i]):
        os.mkdir(project_root + "/VM-Shutdown-Scripts/VM" + node_id_array[i])

    node_shutdown_script = open(project_root + "/VM-Shutdown-Scripts/VM" + node_id_array[i] + "/shutdown.sh", "w")
    node_shutdown_script.write(node_id)
    node_shutdown_script.close()
    print(os.path.join(project_root, "/VM-Shutdown-Scripts/VM"+node_id_array[i]))
