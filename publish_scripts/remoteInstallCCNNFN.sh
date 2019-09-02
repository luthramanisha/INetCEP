#!/bin/bash
work_dir="$(cd "$(dirname "$0")" ; pwd -P)/.."
#assuming you are at the project root
source "$work_dir/VMS.cfg"
count=0
declare -a VMSdir
CCNL_HOME="~/MA-Ali/ccn-lite" #requires project to copied at the home location (~)
all(){
copyCCN
#copyNFN
buildCCN
#buildNFN
#moveNFN
}
installDependencies(){
read -s -p "Enter Password for sudo: " sudoPW
	for i in "${VMS[@]}"
	do	
		echo "logged in: " $i
		ssh $user@$i <<-ENDSSH
		echo "$sudoPW" | sudo -S apt-get remove scala-library scala
		cd Download
		echo "$sudoPW" | sudo -S wget http://scala-lang.org/files/archive/scala-2.10.7.deb
		echo "$sudoPW" | sudo -S dpkg -i scala-2.10.7.deb
		echo "$sudoPW" | sudo -S wget https://dl.bintray.com/sbt/debian/sbt-0.13.16.deb
		echo "$sudoPW" | sudo -S dpkg -i sbt-0.13.16.deb
		echo "$sudoPW" | sudo -S at install -y cmake
		ENDSSH
	done
}
copyCCN(){
for i in "${VMS[@]}"
	do
		echo "logged in: " $i 	
		# make directories if they don't exist already
		ssh -t $user@$i <<-'ENDSSH'
		cd MA-Ali
		rm -rf ccn-lite
		mkdir ccn-lite
		ENDSSH
		scp -rp "$work_dir"/ccn-lite/src $user@$i:~/MA-Ali/ccn-lite/
		scp -rp "$work_dir"/ccn-lite/bin $user@$i:~/MA-Ali/ccn-lite/
	done
}
buildCCN(){
for i in "${VMS[@]}"
	do
		echo "logged in: " $i 	
		# make directories if they don't exist already
		ssh -t $user@$i <<-'ENDSSH'
		cd MA-Ali/ccn-lite/src
		export USE_NFN=1
		export USE_NACK=1
		make clean all
		ENDSSH
	done
}
copyNFN(){
for i in "${VMS[@]}"
	do
		echo "logged in: " $i 	
		# make directories if they don't exist already
		ssh -t $user@$i <<-'ENDSSH'
		cd MA-Ali
		rm -rf nfn-scala
		mkdir nfn-scala
		ENDSSH
		scp -rp "$work_dir"/nfn-scala/* $user@$i:~/MA-Ali/nfn-scala/
	done
}
buildNFN(){
for i in "${VMS[@]}"
	do
		echo "logged in: " $i 	
		# make directories if they don't exist already
		ssh -t $user@$i <<-'ENDSSH'
		cd MA-Ali/nfn-scala	
		sbt clean
		sbt compile
		sbt assembly
		ENDSSH
	done
}
moveNFN(){
for i in "${VMS[@]}"
	do
		echo "logged in: " $i 	
		# make directories if they don't exist already
		ssh -t $user@$i <<-'ENDSSH'
		rm ~/MA-Ali/computeservers/nodes/*/*.jar
		cp ~/MA-Ali/nfn-scala/target/scala-2.10/*.jar ~/MA-Ali/computeservers/nodes/*/
		ENDSSH
	done
}
if [ $1 == "all" ]; then all
elif [ $1 == "copyCCN" ]; then copyCCN
elif [ $1 == "copyNFN" ]; then copyNFN
elif [ $1 == "buildCCN" ]; then buildCCN
elif [ $1 == "buildNFN" ]; then buildNFN
elif [ $1 == "moveNFN" ]; then moveNFN
elif [ $1 == "dep" ]; then installDependencies
else echo "$help"
fi
