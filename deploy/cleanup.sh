#!/usr/bin/env bash

kill $(jps | grep 'DynamoDBLocal.jar' | awk '{print $1}')
kill $(jps | grep 'relief-code-all-1.0.jar' | awk '{print $1}')
