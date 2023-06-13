
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

var functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendPush = functions.database.ref('/lobby/{lobbyID}/sendPush').onWrite( (change,context) => {
    let sendFlagValue = change.after.val();
    if(sendFlagValue){
        let msg = 'La partita è terminata!';
        return loadPlayers(context.params.lobbyID).then(users => {
            let tokens = [];
            for (let user of users) {
                if(user.FCMToken !== "NULL"){
                    console.log("send to: " + user.FCMToken);
                    tokens.push(user.FCMToken);
                }
            }
            let payload = {
                notification: {
                    title: 'LogicGame',
                    body: msg,
                    sound: 'default',
                    badge: '1'
                }
            };
    
            return admin.messaging().sendToDevice(tokens, payload);
        });
    }
    else{
        return 0;
    }
});

function loadPlayers(lobbyID) {
    let dbRef = admin.database().ref('/lobby/'+lobbyID+"/players");
    let defer = new Promise((resolve, reject) => {
        dbRef.once('value', (snap) => {
            let data = snap.val();
            let users = [];
            for (var property in data) {
                users.push(data[property]);
            }
            resolve(users);
        }, (err) => {
            reject(err);
        });
    });
    return defer;
}


