#!/bin/bash

trap 'kill $PID_1 $PID_2 $PID_3 $PID_4 $PID_5' SIGINT SIGTERM EXIT


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo Starting services...
cd $DIR/target/classes
${JAVA_HOME}/bin/rmiregistry &
PID_1=$!
sleep 5
cd $DIR
python $DIR/utils/fetcher/fetchers.py $DIR/config.ini &
PID_2=$!
sleep 5
java -Djava.security.policy=./agent.policy -cp "./target/CloudAtlas-1.0-SNAPSHOT.jar:./lib/*" pl.edu.mimuw.cloudatlas.agent.Agent &
PID_3=$!
sleep 5
java -Djava.security.policy=./agent.policy -cp "./target/CloudAtlas-1.0-SNAPSHOT.jar:./lib/*" pl.edu.mimuw.cloudatlas.fetcher.Fetcher &
PID_4=$!
sleep 5
java -Djava.security.policy=./agent.policy -jar "./target/CloudAtlas-1.0-SNAPSHOT-spring-boot.jar" &
PID_5=$!
sleep 5
wait $PID_1 $PID_2 $PID_3 $PID_4 $PID_5



