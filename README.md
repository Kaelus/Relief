# Overview

Relief is a cloud storage service middleware provisioning a history server for clients to read a log containing history of operations committed by the underlying distributed key-value store (DKVS). Relief understands how the underlying DKVS orders operations and reflects it in the history. Also, it updates the history under weak consistency, which means higher throughput, shorter latency and greater scalability compared to the history update under strong consistency.

# How to build

`gradle clean fatJar`

# Prerequisites
1. OS: the followings are tested on Ubuntu 16.04

2. DKVS needs to be set up and running. DynamoDB local mode is the default (do the following in the dynamodb home)
   - `java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb`
   
3. Configure relief.conf accordingly.

# How to run

1. Run relief programs.
   a. ReliefServer
      - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefServer -c `pwd`/conf/relief.conf
   b. ReliefClient
      - java -jar `pwd`/build/libs/relief-code-all-1.0.jar relief.ReliefLauncher -r ReliefClient -c `pwd`/conf/reliefClient.conf
   c. ReliefYCSBDriver



1. Run Rocky Controller (NBD server)
   - `java -jar `pwd`/build/libs/rocky-code-all-1.0.jar rocky.ctrl.RockyController`

2. Prepare the Rocky Block Device (nbd module & nbd client)
   - `sudo modprobe nbd`
   - `sudo lsmod | grep nbd`
   - `sudo nbd-client -N <volume name> localhost /dev/nbd0`
     - (testing is one of volume names)

To disconnect the Rocky Block Device from the Rocky server, `sudo nbd-client -d /dev/nbd0`

To remove Rocky Block Device module from the kernel, `sudo modprobe -r nbd`

## To Test

- `sudo mkfs.ext4 /dev/nbd0`
- `sudo mount /dev/nbd0 /tmp`
- `ls`
- `sudo umount /tmp`

# To Run multiple Rocky instances on a single host

In the directory 'conf', there is an example rocky.conf configuration file.
Use it at your discretion after setting port and lcvdName accordingly.
Those configuration parameters should be assigned with a unique value for
each rocky instance.

It's good idea to copy and paste the conf/rocky.conf in another directory
for each Rocky instance to run. For instance, we may have two files under
the directory run: run/rocky.conf.1 and run/rocky.conf.2
We should modify those configuration files accorinngly.

run/rocky.conf.1 sets port=10811 and lcvdName=testing1 and the first Rocky
instance will use /dev/nbd1 as the Rocky device driver.
Then, execute following commands:
- Run a Rocky instance with the correct configuration file path name.
  - `java -jar `pwd`/build/libs/rocky-code-all-1.0.jar rocky.ctrl.RockyController run/rocky.conf.1`
- Run nbd-client for the Rocky instance with correct parameters.
  - `sudo nbd-client -N testing1 localhost 10811 /dev/nbd1`

Likewise, suppose run/rocky.conf.2 sets port=10812 and lcvdName=testing2
Also, say /dev/nbd2 is the Rocky device driver instance to use.
- `java -jar \`pwd\`/build/libs/rocky-code-all-1.0.jar rocky.ctrl.RockyController run/rocky.conf.2`
- `sudo nbd-client -N testing2 localhost 10812 /dev/nbd2`

