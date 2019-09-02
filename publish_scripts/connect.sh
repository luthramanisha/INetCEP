#!/usr/bin/env bash

work_dir="$(cd "$(dirname "$0")" ; pwd -P)/.."
source $work_dir/VMS.cfg

if [ -z $1 ]; then
	echo "Needs index as an argument e.g., connect.sh 0 will connect to the first VM in VMS.cfg"
else
	ssh $user@${VMS[$1]}
fi

