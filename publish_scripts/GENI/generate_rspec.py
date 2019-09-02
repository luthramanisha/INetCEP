#!/usr/bin/python

# File name: generate_geni_spec.py
# Author: Sebastian Hennig
# Date created: 18.07.2018
# Python Version: 2.7
# Description: Generates the GENI RSpec for the specified number of nodes

import os
import sys

project_root = os.path.join(os.path.dirname(os.path.realpath(__file__)), "..")

# Read GENI wrapper template
template_file = open(os.path.join(project_root, "scripts/templates/geni_rspec.xml"), "r")
template = template_file.read()
template_file.close()

# Read GENI node template
nodes = ""
template_node_file = open(os.path.join(project_root, "scripts/templates/rspec_node.xml"), "r")
template_node = template_node_file.read()
template_node_file.close()

# Add nodes with ascending IDs
for i in range(0, int(sys.argv[1])):
    node = template_node
    node = node.replace("{{node-id}}", ("node-" + str(i)))
    nodes += node

template = template.replace("{{nodes}}", nodes)
if not os.path.exists(sys.argv[2]):
    os.mkdir(sys.argv[2])

# Write the GENI RSpec file
out_file = open(sys.argv[2] + "/rspec-" + sys.argv[1] +  ".xml", "w")
out_file.write(template)
out_file.close()


print("Generated successfully at " + sys.argv[2])
