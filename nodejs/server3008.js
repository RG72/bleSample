var io = require('socket.io')(3008);

io.on('connection', function (socket) {
  console.log('Connected');
  socket.on('leText', function (leText) {
    console.log('message ', leText);
  });

  socket.on('bleData', function (json) {
    try{
      var j=JSON.parse(json);
    }catch(e){
    //can not parse
      j={}
    }
    console.log('bleData',JSON.stringify(j,null,'  '));
  });

  socket.on('disconnect', function () {
    io.emit('user disconnected');
  });
});