#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/.."
#assuming you are at the project root
source "$work_dir/VMS.cfg"
count=0
declare -a VMSdir
CCNL_HOME="~/MA-Ali/ccn-lite" #requires project to copied at the home location (~)
all(){
installDependencies
installOpenssl
copy
}

installDependencies(){
read -s -p "Enter Password for sudo: " sudoPW
	for i in "${VMS[@]}"
	do	
		echo "logged in: " $i
		ssh $user@$i <<-ENDSSH
		echo "$sudoPW" | sudo -S apt-get update
		echo "$sudoPW" | sudo -S apt autoremove -y
		echo "$sudoPW" | sudo -S apt install -y build-essential
		ENDSSH
	done
}


installOpenssl(){
read -s -p "Enter Password for sudo: " sudoPW
	for i in "${VMS[@]}"
	do
		echo "logged in: " $i
		ssh -t $user@$i<<-'ENDSSH'
		mkdir Download
		cd Download
		echo "Getting openssl1.1.0f"
		wget https://www.openssl.org/source/openssl-1.1.0f.tar.gz
		tar xzvf openssl-1.1.0f.tar.gz
		cd openssl-1.1.0f
		./config -Wl,--enable-new-dtags,-rpath,'$(LIBRPATH)'
		make
		echo "$sudoPW" | sudo -S make install
		ENDSSH
	done
}

copy(){
read -s -p "Enter Password for sudo: " sudoPW
	for i in "${VMS[@]}"
	do
		ssh -t $user@$i<<-'ENDSSH'
		cd /usr/local/lib/
		echo "$sudoPW" | sudo -S cp libcrypto.so.1.1 /usr/lib/
		echo "$sudoPW" | sudo -S cp libcrypto.a /usr/lib/
		echo "$sudoPW" | sudo -S cp libssl.so.1.1 /usr/lib/
		echo "$sudoPW" | sudo -S cp libssl.a /usr/lib/
		cd /usr/lib/
		echo "$sudoPW" | sudo -S ln -s libcrypto.so.1.1 libcrypto.so
		echo "$sudoPW" | sudo -S ln -s libssl.so.1.1 libssl.so
		echo "$sudoPW" | sudo -S ldconfig
		ENDSSH
	done
}

if [ $1 == "install" ]; then install
elif [ $1 == "copy" ]; then copy
elif [ $1 == "all" ]; then all
elif [ $1 == "dep" ]; then installDependencies
else echo "$help"
fi
