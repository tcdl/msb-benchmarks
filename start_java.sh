#!/usr/bin/env bash

trap ctrl_c INT
function ctrl_c(){
    echo "Removing exchange msb:jMeter:perfomance";
    curl -i -u guest:guest -XDELETE http://localhost:15672/api/exchanges/%2F/msb:jMeter:perfomance
    kill $PID_JAVA;
    kill $(jps -l | grep ApacheJMeter.jar | awk '{print $1}');
	echo "Script stopped";
	exit 0;
}

if ! type jmeter >/dev/null 2>&1
then
	echo "jmeter don't detected. Please install jmeter. Aborting";
fi

if ! type java >/dev/null 2>&1
then
	echo "java don't detected. Please install java. Aborting";
fi

if ! type mvn >/dev/null 2>&1
then
	echo "mvn don't detected. Please install mvn. Aborting";
fi

cd consumer-java
mvn clean package -DskipTests
cd ..

echo "Creating exchange msb:jMeter:perfomance";
curl -i -u guest:guest -H "content-type:application/json" -XPUT -d'{"type":"fanout","durable":false}' http://localhost:15672/api/exchanges/%2F/msb:jMeter:perfomance

java -jar consumer-java/target/fat-bus2http-microservice-1.0.0-SNAPSHOT.jar & PID_JAVA=$!;

sleep 8;

echo "Creating binding";
curl -i -u guest:guest -H "content-type:application/json" -XPOST -d'{"routing_key":"","arguments":[]}' http://localhost:15672/api/bindings/%2F/e/msb:jMeter:perfomance/q/msb:perfomance.testing.t

jmeter -n -t producer-jmeter/msb.jmx & PID_JMETER=$!;

wait $PID_JMETER;
wait $PID_JAVA;
