#!/bin/bash

trap 'kill $PID_1 $PID_2 $PID_3 $PID_4' SIGINT SIGTERM EXIT


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo Starting services...
python $DIR/utils/fetcher/fetchers.py $DIR/config.ini &
PID_1=$!
sleep 5
java -cp "./target/CloudAtlas-1.0-SNAPSHOT.jar:./lib/*" pl.edu.mimuw.cloudatlas.signer.Signer private_key.der &
PID_2=$!
sleep 5
java -cp "./target/CloudAtlas-1.0-SNAPSHOT.jar:./lib/*" pl.edu.mimuw.cloudatlas.agent.NewAgent tst_public.der &
PID_3=$!
sleep 5
java -jar "./target/CloudAtlas-1.0-SNAPSHOT-spring-boot.jar" &
PID_4=$!
sleep 5
wait $PID_1 $PID_2 $PID_3 $PID_4



