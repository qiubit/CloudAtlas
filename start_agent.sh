#!/bin/bash

trap 'kill $PID_1 $PID_2 $PID_3' SIGINT SIGTERM EXIT


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo Starting services...
python $DIR/utils/fetcher/fetchers.py $DIR/config.ini >/dev/null & 
PID_1=$!
sleep 5
java -cp "./target/CloudAtlas-1.0-SNAPSHOT.jar:./lib/*" pl.edu.mimuw.cloudatlas.agent.NewAgent agentConfig.properties &
PID_2=$!
sleep 5
java -jar "./target/CloudAtlas-1.0-SNAPSHOT-spring-boot.jar" agentConfig.properties >/dev/null &
PID_3=$!
sleep 5
wait $PID_1 $PID_2 $PID_3



