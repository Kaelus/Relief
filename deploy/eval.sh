#!/usr/bin/env bash

if [ $# -lt 2 ]; then
    echo "usage: eval.sh <num_ycsb> <working_dir>"
    exit 1
fi

num_ycsb=$1
working_dir=$2

all_runtime=0
all_numReads=0
all_numWrites=0

all_readLatency=0
all_writeLatency=0
for (( i=0; i<$num_ycsb; i++ ))
do
    cID="c$i"
    numBothOps=`grep "Operations" $working_dir/$cID/transaction.dat`
    numReads=`echo $numBothOps | cut -d ' ' -f 3`
    numWrites=`echo $numBothOps | cut -d ' ' -f 9`
    #echo "numBothOps, " $numBothOps
    #echo "numReads, " $numReads
    #echo "numWrites, " $numWrites
    all_numReads=$(($all_numReads + $numReads))
    all_numWrites=$(($all_numWrites + $numWrites))
    all_bothOps=$(($all_numReads + $all_numWrites))

    averageLatencyBothOps=`grep "AverageLatency" $working_dir/$cID/transaction.dat`
    echo "averageLatencyBothOps, " $averageLatencyBothOps
    averageReadLatency=`echo $averageLatencyBothOps | cut -d ' ' -f 3 | cut -d '.' -f 1`
    averageWriteLatency=`echo $averageLatencyBothOps | cut -d ' ' -f 9 | cut -d '.' -f 1`
    all_readLatency=$(($averageReadLatency * $numReads + $all_readLatency))
    all_writeLatency=$(($averageWriteLatency * $numWrites + $all_writeLatency))
    
    inc=`grep "RunTime" $working_dir/$cID/transaction.dat  | cut -d ' ' -f 3 | cut -d '.' -f 1`
    #echo "Increasing by $inc"
    all_runtime=$(($all_runtime + $inc))
    #echo "all_runtime is " $all_runtime    
done

echo "####################################"
num_server=`ls $working_dir/s* | grep "$working_dir" | wc -l`
echo "num_server " $num_server 
echo "num_ycsb " $num_ycsb
echo "------------------------------------"
opsPerYCSB=$(($all_bothOps / $num_ycsb)) 
echo "Overall Throughput(ops/sec), " $(($opsPerYCSB * 1000 * $num_ycsb / $all_runtime))
echo "Overall Read Latency(us), " $(($all_readLatency / $all_numReads))
echo "Overall Write Latency(us), " $(($all_writeLatency / $all_numWrites))
echo "####################################"
