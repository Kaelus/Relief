# Overview

Relief is a cloud storage service middleware provisioning a history server for clients to read a log containing history of operations committed by the underlying distributed key-value store (DKVS). Relief understands how the underlying DKVS orders operations and reflects it in the history. Also, it updates the history under weak consistency, which means higher throughput, shorter latency and greater scalability compared to the history update under strong consistency.

# How to build

`gradle clean fatJar`

# Prerequisites

1. OS: the followings are tested on Ubuntu 16.04

2. DKVS needs to be set up and running. DynamoDB local mode is the default (do the following in the dynamodb home)
   - `java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb`
   
3. Configure relief.conf accordingly.

# How to run relief programs.

1. ReliefServer
   - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefServer -c `pwd`/conf/relief.conf

2. ReliefClient
   - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefClient -c `pwd`/conf/reliefClient.conf

3. ReliefYCSBDriver:
   a. To load:
      - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefYCSBDriver -c `pwd`/conf/reliefClient.conf -load -P `pwd`/workloads/workloada -P `pwd`/workloads/relief-workload

   b. To do transactions:
      - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefYCSBDriver -c `pwd`/conf/reliefClient.conf -t -P workloads/workloada -P workloads/relief-workload

4. ReliefVerifier:
   - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefVerifier -c `pwd`/conf/reliefVerifier.conf

# How to Test Locally

1. Edit configuration files for relief servers in run/r{1,2,..} properly.

2. Edit configuration files for YCSB clients in run/c{1,2,..} properly.

3. Run relief servers. (Example for r1. Repeat for r{2,..} by replacing r1 below.)
   - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefServer -c `pwd`/run/r1/relief.conf

4. Run relief clients. (Example for c1. Repeat for c{2,..} by replacing c1 below.)
   - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefClient -c `pwd`/run/c1/reliefClient.conf

5. Run YCSB clients. (Example for c1 to do transaction, assuming loaded. Repeat for c{2,..} by replacing c1 below.)
      - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefYCSBDriver -c `pwd`/run/c1/reliefClient.conf -t -P `pwd`/workloads/workloada -P `pwd`/workloads/relief-workload -threads 16 -s > transaction.dat

# How to Get Empirical Results

1. Measure the scalability: measure the throughput and the latency while varying the number of operations by increasing the number of YCSB threads each of which issues the fixed amount of operations.
   - Run two relief controllers in SC-CRHU, EC-CRHU and No-CRHU modes.
     - SC-CRHU: Sequentially Consistent Conflict-free Replicated History Update
     - EC-CRHU: Eventually Consistent Conflict-free Replicated History Update
     - No-CRHU: No Conflict-free Replicated History Update
   - Run two YCSB instances.
     - One YCSB instance for one relief controller.
     - Another YCSB instance for the other relief controller.
     - Adjust the number of threads: 1,2,4,8,16,32,64,128
   - Run four different workloads:
     - workloada (read heavy): read 0.5 and update 0.5
     - workloadb (write heavy): read 0.95 and update 0.05
     - workloadc (read only): read 1.0
     - workloadf (read and read-modify-write): read 0.5 readmodifywrite 0.5
   - Compare results between SC-CRHU, EC-CRHU and No-CRHU for each workload.
     - Take the numbers of the YCSB instance for one relief controller.
     - For SC-CRHU, distinguish numbers between the primary and the secondary.
     
2. Run verification over the history
   - Run a relief controller in EC-CRHU mode.
   - Run a YCSB instance with multiple threads.
   - Run a relief client and read history to save the history in a trace file.
   - Run a relief verifier to verify the trace file's history.
   - Measure the time taken while varying the number of YCSB transactions.

