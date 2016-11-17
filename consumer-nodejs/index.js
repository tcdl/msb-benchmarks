var msb = require('msb');

msb.configure({schema: null});

msb
.channelManager
.findOrCreateConsumer('msb:perfomance', {groupId: 'testing', durable: true})
.on('message', function(message) {
})
.on('error', console.error);

console.log('Node.js consumer started');

function stopHandler(){
  console.log('Node.js Consumer Stopping');
  msb.channelManager.close();
};

process.on("SIGTERM", stopHandler);
process.on("SIGINT", stopHandler);
process.on("SIGHUP", stopHandler);