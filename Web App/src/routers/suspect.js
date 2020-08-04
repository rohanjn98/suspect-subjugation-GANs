const express = require("express");
const router = express.Router();
const firebase = require('firebase');
const _ = require('lodash');

//Firebase configuration
const firebaseConfig = require("../db/firebase");

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

let id = 0;

//root route
router.get("/", function(req, res) {
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
        }
    );
});

router.post("/action", function(req, res) {
    console.log("hello from action");
    console.log(req.body);
    let str = req.body.buttonAction;
    console.log(str);
    let action = str.substring(0, 4);
    let name = str.substring(4);
    if (action === "view") {
        res.redirect("/suspect/" + name);
    }
    if (action === "trak") {
        res.redirect("/suspect/track/" + name);
    }
});

router.get("/initialize", function(req, res) {
    res.render('initialize', {
        id: id++
    });
});

router.post("/initialize", function(req, res) {
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

router.get("/suspect/:suspectName", function(req, res) {
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
        }
    );
});

router.get("/suspect/track/:suspectName", function(req, res) {
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
        }
    );
});

module.exports = router
