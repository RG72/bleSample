var app = require('http').createServer(handler)
var io = require('socket.io')(app);
var fs = require('fs');

app.listen(3008);

function handler (req, res) {
  fs.readFile(__dirname + '/index.html',
  function (err, data) {
    if (err) {
      res.writeHead(500);
      return res.end('Error loading index.html');
    }

    res.writeHead(200);
    res.end(data);
  });
}
var calcDistance=function (j) {

    var ratio_db = j.txCalibratedPower - j.rssi;
    var ratio_linear = Math.pow(10, ratio_db / 10);

    var r = Math.sqrt(ratio_linear);
    j.distance=r;
    return r;
};//calcDistance

io.on('connection', function (socket) {
  console.log('Connected');
  socket.emit('info',{
    mac:'test',
    uuid:'testUUID',
    rssi:-61
  });

  socket.on('bleData', function (json) {
    var j=null;
    try{
      var j=JSON.parse(json);
    }catch(e){
    //can not parse
    }
    if (j){
      j.uuid="";
      j.scanRecord.slice(9,25).forEach(function(b){
        j.uuid+=Number(b).toString(16);
      });
      j.txCalibratedPower=j.scanRecord[29];

      calcDistance(j);
      socket.broadcast.emit('info',j);
    }

    console.log('bleData',j);
  });

  socket.on('disconnect', function () {
    io.emit('user disconnected');
  });
});
