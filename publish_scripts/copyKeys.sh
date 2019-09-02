#!/usr/bin/env bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../"
#assuming you are at the project root
source "$work_dir/VMS.cfg"

#still the ssh server has to be registered for the respective user
for i in "${VMS[@]}"
    do
	ssh $user@$i 'mkdir -p ~/.ssh'
	cat ~/.ssh/id_rsa.pub | ssh $user@$i 'cat >> ~/.ssh/authorized_keys'
    done



