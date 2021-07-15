#!/usr/bin/env bash

if [ $# -lt 4 ]; then
    echo "usage: run.sh <num_server> <num_ycsb> <working_dir> <relief_dir> <dynamo_dir>"
    exit 1
fi

num_server=$1
num_ycsb=$2
working_dir=$3
relief_dir=$4
dynamo_dir=$5

BASE_DYNAMO_PORT=8000
BASE_RELIEF_PORT=10080

i=0
while [ $i -lt $num_server ]; do
    THIS_DYNAMO_PORT=`expr $BASE_DYNAMO_PORT + $i`
    java -Djava.library.path=$dynamo_dir -jar DynamoDBLocal.jar -sharedDb -port $THIS_DYNAMO_PORT -inMemory
    i=`expr $i + 1`
done

i=0
while [ $i -lt $num_server ]; do
    THIS_RELIEF_PORT=`expr $BASE_RELIEF_PORT + $i`
    NODE_ID="s$i"
    java -jar $relief_dir/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefServer -c $working_dir/$NODE_ID/relief.conf
    i=`expr $i + 1`
done

i=0
while [ $i -lt $num_ycsb ]; do
    THIS_RELIEF_PORT=`expr $BASE_RELIEF_PORT + $i`
    NODE_ID="c$i"
    java -jar $relief_dir/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefYCSBDriver -c $working_dir/$NODE_ID/reliefClient.conf -t -P $relief_dir/workloads/workloada -P $relief_dir/workloads/relief-workload -threads 16 -s > $working_dir/$NODE_ID/transaction.dat
    i=`expr $i + 1`
done


    
