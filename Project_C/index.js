#!/usr/bin/env node

const express = require('express');
const session = require('express-session');
const dao = require('./dao.js');
const net = require('net');
const http = require('http');


const app = express();
const port = process.argv[2] || 3000;


app.enable('trust proxy');
app.use(session({
    secret: 'secret',
    resave: true,
    saveUninitialized: true,
    proxy: true
}));

app.get('/GeoNode', function(req, res) {
    //init host and port.
    const GeoHost = '130.63.96.51';
    const GeoServicePort = 36041;
    const client = net.createConnection(GeoServicePort, GeoHost);
    res.setHeader('Content-Type', 'text/plain');
    let dist = "";
    if (req.query.lat != null && req.query.lng != null) {
        if (!req.session.lat && !req.session.lng) {
            req.session.lat = req.query.lat;
            req.session.lng = req.query.lng;
            res.send('RECEIVED');
        } else {
            let response = [];
            let preLat = req.session.lat;
            let preLng = req.session.lng;
            let curLat = req.query.lat;
            let curLng = req.query.lng;
            req.session.lat = req.query.lat;
            req.session.lng = req.query.lng;
            let payload = preLat + " " + preLng + " " + curLat + " " + curLng;
            client.on('error', (err) => console.log(`Error: ${err}`));
            client.on('connect', () => {
                client.write(payload); // Send a request
                client.on('data', (data) => {
                    console.log(data.toString())
                })
                client.end(); // close the socket
            });
            client.on('data', (chunk) => response.push(chunk));
            client.on('end', () => {
                console.log(response.join())
                let data = response.join();
                data = data.substring(0, data.length - 1);
                res.send(`The distance from (${preLat}, ${preLng}) to (${curLat}, ${curLng}) is: ${data} km`);
            });
        }
    } else {
        res.send(`Don't understand: ${req.query}`);
    }
});

app.get('/Catalog', function(req, res) {
    dao.getCatalog(req.query.id, function(rows) {
        res.setHeader('Content-Type', 'application/json');
        res.write(JSON.stringify(rows));
        res.end();
    });
});




app.get('/Trip', function(req, res) {
    const MapquestKey = "BtA2TK5OpWwql2A06dr3DBZSubsonoN8";
    let API = "http://www.mapquestapi.com/directions/v2/route?";
    let from = req.query.from;
    let to = req.query.to;
    let payload = API + "key=" + MapquestKey + "&from=" + from + "&to=" + to;
    http.get(payload, (resp) => {
        let data = '';
        resp.on('data', (chunk) => {
            data += chunk;
        });
        resp.on('end', () => {
            console.log(data.toString());
            let obj = JSON.parse(data.toString());
            res.setHeader('Content-Type', 'application/json');
            if(obj.info.statuscode == 603){
              let output = {"distance": 0, "time": 0};
              res.write(JSON.stringify(output));
              res.end();
            }
            else{
              let dist = obj.route.distance;
              let time = obj.route.formattedTime;
              let output = {"distance": dist, "time": time};
              res.write(JSON.stringify(output,null,0));
              res.end();
            }
        });
    }).on("error", (err) => {
        console.log("Error: " + err.message);
    });
});

const server = app.listen(port, function() {
    const host = server.address().address;
    const port = server.address().port;
    console.log(`server listening to ${host}:${port}`);
});
