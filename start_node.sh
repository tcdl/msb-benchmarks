#!/usr/bin/env bash

trap ctrl_c INT
function ctrl_c(){
    echo "Removing exchange msb:jMeter:perfomance";
    curl -i -u guest:guest -XDELETE http://localhost:15672/api/exchanges/%2F/msb:jMeter:perfomance

    kill $PID_NODE;
    kill $(jps -l | grep ApacheJMeter.jar | awk '{print $1}');
	echo "Script stopped";
	exit 0;
}

if ! type jmeter >/dev/null 2>&1
then
	echo "jmeter don't detected. Please install jmeter. Aborting";
fi

if ! type node >/dev/null 2>&1
then
	echo "node.js don't detected. Please install node.js. Aborting";
fi

cd consumer-nodejs
npm install >/dev/null 2>&1
cd ..

echo "Creating exchange msb:jMeter:perfomance";
curl -i -u guest:guest -H "content-type:application/json" -XPUT -d'{"type":"fanout","durable":false}' http://localhost:15672/api/exchanges/%2F/msb:jMeter:perfomance

node consumer-nodejs/index.js & PID_NODE=$!;

sleep 5;

echo "Creating binding";
curl -i -u guest:guest -H "content-type:application/json" -XPOST -d'{"routing_key":"","arguments":[]}' http://localhost:15672/api/bindings/%2F/e/msb:jMeter:perfomance/q/msb:perfomance.testing.t

jmeter -n -t producer-jmeter/msb.jmx & PID_JMETER=$!;
wait $PID_JMETER;
wait $PID_NODE;
