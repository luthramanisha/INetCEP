#!/usr/bin/env bash
#script to access environment variables over an ssh connection 
#solution found here: https://unix.stackexchange.com/questions/46143/why-bash-unable-to-find-command-even-if-path-is-specified-properly
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/../"
source "$work_dir/VMS.cfg"
#still the ssh server has to be registered for the respective user
for i in "${VMS[@]}"
    do
	#https://superuser.com/questions/136646/how-to-append-to-a-file-as-sudo
	ssh -t $user@$i 'echo "# Set this to allow user environment path at the remote server" | sudo tee -a /etc/ssh/sshd_config'
	ssh -t $user@$i 'echo "PermitUserEnvironment yes" | sudo tee -a /etc/ssh/sshd_config'
	ssh -t $user@$i 'sudo service ssh restart'	
	ssh $user@$i 'echo "CCNL_HOME=$HOME/MA-Ali/ccn-lite" >> ~/.ssh/environment'
	ssh $user@$i 'echo "JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64" >> ~/.ssh/environment'
	ssh $user@$i 'echo "PATH=$PATH:$CCNL_HOME/bin:$JAVA_HOME/bin" >> ~/.ssh/environment'
    done



