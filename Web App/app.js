const express = require('express');
const bodyParser = require('body-parser');
const request = require('request');
const https = require('https');
const ejs = require("ejs");
const _ = require('lodash');
const firebase = require('firebase');
const ejsLint = require('ejs-lint');

ejsLint('track');

const app = express();
app.use(express.static("public"));
app.set('view engine', 'ejs');
app.use(bodyParser.urlencoded({
  extended: true
}));

let id = 0;

//Firebase configuration
const firebaseConfig = {
  apiKey: "AIzaSyBaTKt-B3oR_yC5--ePZ7ZCeMm97svpb8E",
  authDomain: "armfire-6367d.firebaseapp.com",
  databaseURL: "https://armfire-6367d.firebaseio.com",
  projectId: "armfire-6367d",
  storageBucket: "armfire-6367d.appspot.com",
  messagingSenderId: "180478544259",
  appId: "1:180478544259:web:78dd554a9d33e346a0ceb6",
  measurementId: "G-GGP9SWZBSD"
};
// Initialize Firebase
firebase.initializeApp(firebaseConfig);

//root route
app.get("/", function(req, res) {
  const referencePath = '/Criminals/';
  const allSuspectsReference = firebase.database().ref(referencePath);
  //Attach an asynchronous callback to read the data
  allSuspectsReference.on("value",
    function(snapshot) {
      console.log(snapshot.val());
      const suspectsFromDB = snapshot.val();
      res.render("home", {
        suspects: suspectsFromDB
      });
      allSuspectsReference.off("value");
    },
    function(errorObject) {
      console.log("The read failed: " + errorObject.code);
    });
});

app.post("/action", function(req, res) {
  let str = req.body.buttonAction;
  let action = str.substring(0, 4);
  let name = str.substring(4);
  if (action === "view") {
    res.redirect("/suspect/" + name);
  }
  if (action === "trak") {
    res.redirect("/suspect/track/" + name);
  }
});

app.get("/initialize", function(req, res) {
  res.render('initialize', {
    id: id++
  });
});

app.post("/initialize", function(req, res) {
  const id = req.body.suspectID;
  const suspectPhoto = "https://firebasestorage.googleapis.com/v0/b/armfire-6367d.appspot.com/o/person.png?alt=media&token=0efdd78f-5939-434a-81ae-2ff4a98bd368"
  const suspect = {
    id: req.body.suspectID,
    name: req.body.suspectName,
    height: parseFloat(req.body.suspectHeight),
    age: parseInt(req.body.suspectAge),
    description: req.body.suspectDescription,
    photo: suspectPhoto
  };
  const referencePath = '/Criminals/' + id + '/';
  const suspectReference = firebase.database().ref(referencePath);
  suspectReference.set(suspect, function(error) {
    if (error) {
      console.log("Data could not be saved. Error:" + error);
    } else {
      console.log("Data saved successfully.");
    }
  });
  res.redirect("/");
});

app.get("/suspect/:suspectName", function(req, res) {
  const referencePath = '/Criminals/';
  const allSuspectsReference = firebase.database().ref(referencePath);
  //Attach an asynchronous callback to read the data
  allSuspectsReference.on("value",
    function(snapshot) {
      console.log(snapshot.val());
      const suspectsFromDB = snapshot.val();
      suspectsFromDB.forEach(function(suspect) {
        if (_.kebabCase(suspect.name) === _.kebabCase(req.params.suspectName)) {
          res.render("view", {
            suspect: suspect
          });
        }
      });
      allSuspectsReference.off("value");
    },
    function(errorObject) {
      console.log("The read failed: " + errorObject.code);
    });
});

app.get("/suspect/track/:suspectName", function(req, res) {
  const referencePath = '/Criminals/';
  const allSuspectsReference = firebase.database().ref(referencePath);

  //Attach an asynchronous callback to read the data
  allSuspectsReference.on("value",
    function(snapshot) {
      //console.log(snapshot.val());
      const suspectsFromDB = snapshot.val();
      suspectsFromDB.forEach(function(suspect) {
        if (_.kebabCase(suspect.name) === _.kebabCase(req.params.suspectName)) {
          res.render("track", {
            locations: suspect.locations
          });
        }
      });
      allSuspectsReference.off("value");
    },
    function(errorObject) {
      console.log("The read failed: " + errorObject.code);
    });
});

//PORT = 5000
app.listen(process.env.PORT || 5000, function() {
  console.log("Server is running on port 5000");
});
