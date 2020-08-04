require('dotenv').config()
const express = require('express');
const bodyParser = require('body-parser');
const path = require('path')
const request = require('request');
const https = require('https');
const ejs = require("ejs");
const _ = require('lodash');
const firebase = require('firebase');
const ejsLint = require('ejs-lint');
const app = express();

ejsLint('track');

// Router paths
const suspectRoutes = require('./routers/suspect');

// Define paths for Express Config
const publicDirectoryPath = path.join(__dirname, '../public')
const viewsPath = path.join(__dirname, '../templates/views')

// Setup static directory to serve
app.use(express.static(publicDirectoryPath))


// ejs Config, Middleware
//app.use(expressLayouts);
app.set('view engine', 'ejs');
app.set('views', viewsPath)
app.use(express.urlencoded({
  extended: false
}));


app.use(bodyParser.urlencoded({
    extended: true
}));


app.use(suspectRoutes)
//PORT = 5000
app.listen(process.env.PORT || 5000, function() {
    console.log("Server is running");
});
