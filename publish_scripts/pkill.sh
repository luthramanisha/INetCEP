work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../"
source "$work_dir/VMS.cfg"

for i in "${VMS[@]}"
do
	ssh $user@$i <<-'ENDSSH'
	pkill -f "java"
	pkill -f "nfn-relay"
	pkill -f "iperf"
	pkill -f "ccn"
	pkill -f "nfn"
	pkill -f "bash"
	ENDSSH
done
