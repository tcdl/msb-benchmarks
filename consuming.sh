#!/usr/bin/env bash

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

if ! type node >/dev/null 2>&1
then
	echo "node.js don't detected. Please install node.js. Aborting";
fi

echo "Select msb version";
options=("node.js" "java" "Quit")
select opt in "${options[@]}"
    do
        case $opt in
        "node.js")
            echo "Installing node dependency";
            cd consumer-nodejs
            npm install >/dev/null 2>&1
            cd ..
            consume_command='node consumer-nodejs/index.js'
            break;
            ;;
        "java")
            echo "Building jar";
            cd consumer-java
            mvn clean package -DskipTests >/dev/null 2>&1
            cd ..
            consume_command='java -jar consumer-java/target/fat-bus2http-microservice-1.0.0-SNAPSHOT.jar'
            break;
            ;;
        "Quit")
            exit 0;
            break;
            ;;
         *)
            echo "invalid option";;
        esac;
    done;


echo "Select message size";
options=("150 bytes" "1500 bytes" "Quit")
select opt in "${options[@]}"
    do
        case $opt in
        "150 bytes")
            test_file="producer-jmeter/150b.jmx"
            break;
            ;;
        "1500 bytes")
            test_file="producer-jmeter/1500b.jmx"
            break;
            ;;
        "Quit")
            exit 0;
            break;
            ;;
         *)
            echo "invalid option";;
        esac;
    done;

trap ctrl_c INT
function ctrl_c(){
    echo "Removing exchange and queue";
    curl -i -u guest:guest -XDELETE http://localhost:15672/api/exchanges/%2F/msb:jMeter:perfomance
    curl -i -u guest:guest -XDELETE http://localhost:15672/api/queues/%2F/msb:perfomance.testing.d

    kill $PID;
    kill $(jps -l | grep ApacheJMeter.jar | awk '{print $1}');
	echo "Script stopped";
	exit 0;
}



echo "Creating exchange, queue and bindings";
curl -i -u guest:guest -H "content-type:application/json" -XPUT -d'{"type":"fanout","durable":false}' http://localhost:15672/api/exchanges/%2F/msb:jMeter:perfomance >/dev/null 2>&1
curl -i -u guest:guest -H "content-type:application/json" -XPUT -d'{"durable":true}' http://localhost:15672/api/queues/%2F/msb:perfomance.testing.d >/dev/null 2>&1
curl -i -u guest:guest -H "content-type:application/json" -XPOST -d'{"routing_key":"","arguments":[]}' http://localhost:15672/api/bindings/%2F/e/msb:jMeter:perfomance/q/msb:perfomance.testing.d >/dev/null 2>&1

echo "Creating messages by jmeter";
jmeter -n -t $test_file >/dev/null 2>&1

echo "Starting consuming"
eval $consume_command & PID=$!;
wait $PID;
