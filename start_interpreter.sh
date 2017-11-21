#!/bin/bash

java -Djava.security.policy=./agent.policy -cp "./target/CloudAtlas-1.0-SNAPSHOT.jar:./lib/*" pl.edu.mimuw.cloudatlas.InterpreterCMD
