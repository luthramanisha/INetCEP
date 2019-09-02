#!/bin/bash
#workaround shutting down until gracefully taking down doesn't works (shutdown.sh)

work_dir="$(cd "$(dirname "$0")" ; pwd -P)"
source "$work_dir/VMS.cfg"

for i in "${VMS[@]}"
do
	ssh -t $USER@$i "screen -ls | grep Detached | cut -d. -f1 | awk '{print \$1}' | xargs kill"
done

