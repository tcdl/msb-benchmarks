#!/bin/bash
if ! mkdir temp_dir >/dev/null 2>&1
then
	echo "Cann't make 'temp_dir' folder. Aborting.";
	exit 1;
fi 

if ! type java >/dev/null 2>&1
then
	echo "Java don't detected. Please install java. Aborting";
fi

if ! type ant >/dev/null 2>&1
then
	echo "ant don't detected. Please install ant. Aborting";
fi


os=$(uname -s)

echo 'Start installing jmeter';
if [ $os = 'Darwin' ] 
then
	echo "OSX detected. brew will use for install jMeter";
	brew install jmeter --with-plugins >/dev/null 2>&1
	extFolder='/usr/local/Cellar/jmeter/3.0/libexec/lib/ext';
fi

if [ $os = 'Linux' ] 
then
	echo 'Linux OS detected. Use apt-get';
	sudo apt-get install jmeter
	extFolder='/usr/share/jmeter/lib/ext';
fi

echo 'jMeter installed. Start installing plugins.';

echo 'Installing RabbitMQ plugin';
cd temp_dir
git clone https://github.com/jlavallee/JMeter-Rabbit-AMQP.git  >/dev/null 2>&1;
cp "$extFolder/ApacheJMeter_core.jar" ./JMeter-Rabbit-AMQP
cd JMeter-Rabbit-AMQP
ant >/dev/null 2>&1
sudo cp ./target/dist/JMeterAMQP.jar $extFolder	
cd ../..
rm -rf temp_dir;

echo "Installing jmeter-plugins-manager";
sudo curl "https://repo1.maven.org/maven2/kg/apc/jmeter-plugins-manager/0.10/jmeter-plugins-manager-0.10.jar" -o "$extFolder/jmeter-plugins-manager-0.10.jar" >/dev/null 2>&1;
sudo java -cp "$extFolder/jmeter-plugins-manager-0.10.jar" org.jmeterplugins.repository.PluginManagerCMDInstaller

echo "Installing amqp-client";
sudo curl "http://repo1.maven.org/maven2/com/rabbitmq/amqp-client/3.6.5/amqp-client-3.6.5.jar" -o "$extFolder/amqp-client-3.6.5.jar" >/dev/null 2>&1;

echo "Done";