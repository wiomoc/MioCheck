import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

admin.initializeApp();

export const sendAvailiablityNotification = functions.region("europe-west1")
    .database.ref("/shop-status/{shop}")
    .onUpdate(async (snapshot, context) => {
        if (snapshot.after.child("status").val() === "AVAILABLE" && snapshot.before.child("status").val() === "EMPTY") {
            await admin.messaging().sendToTopic(context.params.shop, {
                notification: {
                    title: "MIO is available again",
                    body: `MIO is available at ${snapshot.after.child("name").val()}`
                }
            })
        }
    });

export const newAccount = functions.region("europe-west1").auth.user().onCreate(async (user, context) => {
    await admin.database().ref("/account").child(user.uid).set({
        balance: 0
    })
});

export const addMio = functions.region("europe-west1")
    .https.onCall(async (data, context) => {
        if (!context.auth) return;

        await admin.database().ref("/locker/inventory").transaction(inventory => {
            return inventory + 1
        });

        await admin.database().ref("/account/" + context.auth.uid + "/balance").transaction(balance => {
            return balance + 1;
        });

        return true

    });

export const takeMio = functions.region("europe-west1")
    .https.onCall(async (data, context) => {
        if (!context.auth) return;

        await admin.database().ref("/locker/inventory").transaction(inventory => {
            if (inventory === 0) {
                return
            }

            return inventory - 1
        });

        await admin.database().ref("/account/" + context.auth.uid + "/balance").transaction(balance => {
            return balance - 1;
        });

        return true
    });
