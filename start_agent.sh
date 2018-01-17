#!/bin/bash

trap 'kill $PID_1 $PID_2 $PID_3 $PID_4 $PID_5' SIGINT SIGTERM EXIT


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo Starting services...
cd $DIR/target/classes
rmiregistry &
PID_1=$!
sleep 5
cd $DIR
python $DIR/utils/fetcher/fetchers.py $DIR/config.ini >/dev/null & 
PID_2=$!
sleep 5
java -Djava.security.policy=./agent.policy -cp "./target/CloudAtlas-1.0-SNAPSHOT.jar:./lib/*" pl.edu.mimuw.cloudatlas.agent.NewAgent agentConfig.properties &
PID_3=$!
# java -Djava.security.policy=./agent.policy -cp "./target/CloudAtlas-1.0-SNAPSHOT.jar:./lib/*" pl.edu.mimuw.cloudatlas.fetcher.Fetcher &
# PID_4=$!
sleep 5
java -Djava.security.policy=./agent.policy -jar "./target/CloudAtlas-1.0-SNAPSHOT-spring-boot.jar" >/dev/null &
PID_4=$!
sleep 5
wait $PID_1 $PID_2 $PID_3 $PID_4



