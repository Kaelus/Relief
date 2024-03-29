#!/usr/bin/env bash

if [ $# -lt 4 ]; then
    echo "usage: setup.sh <num_server> <num_ycsb> <working_dir> <relief_dir> <history_mode>"
    exit 1
fi

num_server=$1
num_ycsb=$2
working_dir=$3
relief_dir=$4
history_mode=$5
#conf_dir=$working_dir/conf

scriptdir=`dirname $0`

BASE_DYNAMO_PORT=8000
BASE_RELIEF_PORT=10080

# Setup servers
i=0
while [ $i -lt $num_server ]; do
    THIS_DYNAMO_PORT=`expr $BASE_DYNAMO_PORT + $i`
    THIS_RELIEF_PORT=`expr $BASE_RELIEF_PORT + $i`
    NODE_ID="s$i"
    
    # Setup Relief Servers
    test -d $working_dir/$NODE_ID && rm -rf $working_dir/$NODE_ID
    mkdir -p $working_dir/$NODE_ID
    cp -rf $scriptdir/relief.conf-template $working_dir/$NODE_ID/relief.conf
    sed -i -e "9s|.*|backEndHistoryServerURL=http://localhost:$THIS_DYNAMO_PORT|" $working_dir/$NODE_ID/relief.conf
    sed -i -e "10s|.*|backEndDataServerURL=http://localhost:$THIS_DYNAMO_PORT|" $working_dir/$NODE_ID/relief.conf
    sed -i -e "13s|.*|reliefAddress=127.0.0.1:$THIS_RELIEF_PORT|" $working_dir/$NODE_ID/relief.conf
    sed -i -e "17s|.*|historyServerMode=$history_mode|" $working_dir/$NODE_ID/relief.conf
    
    i=`expr $i + 1`
done

# Setup YCSB
i=0
while [ $i -lt $num_server ]; do
    THIS_RELIEF_PORT=`expr $BASE_RELIEF_PORT + $i`

    YCSB_GROUP_MAX=`expr $num_ycsb / $num_server`
    j=0
    while [ $j -lt $YCSB_GROUP_MAX ]; do
	NODE_COUNT=$(( $YCSB_GROUP_MAX * $i + $j ))
	NODE_ID="c$NODE_COUNT"
    
	test -d $working_dir/$NODE_ID && rm -rf $working_dir/$NODE_ID
	mkdir -p $working_dir/$NODE_ID
	cp -rf $scriptdir/reliefClient.conf-template $working_dir/$NODE_ID/reliefClient.conf
	sed -i -e "1s|.*|srvip=127.0.0.1:$THIS_RELIEF_PORT|" $working_dir/$NODE_ID/reliefClient.conf
	sed -i -e "3s|.*|keyStoreFile=$relief_dir/data/mykeystore.jks|" $working_dir/$NODE_ID/reliefClient.conf
	sed -i -e "5s|.*|traceFileName=$working_dir/$NODE_ID/traceFile|" $working_dir/$NODE_ID/reliefClient.conf

	j=`expr $j + 1`
    done
    
    i=`expr $i + 1`
done

