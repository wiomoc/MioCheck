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
        balance: 0,
        name: user.displayName
    })
});

export const mioTransaction = functions.region("europe-west1")
    .https.onCall(async (data, context) => {
        if (!context.auth) return;

        const {change, lockerId} = data;
        if (!change || !lockerId) return false;
        const uid = context.auth.uid;

        const {snapshot} = await admin.database()
            .ref("/locker")
            .child(lockerId)
            .child("inventory")
            .transaction(inventory => {
                if (inventory === null) return 0;
                if (inventory + change < 0) {
                    return;
                }

                return inventory + change
            });

        if (!snapshot) {
            return false;
        }

        const promiseBag = [];
        promiseBag.push(admin.database()
            .ref("/locker")
            .child(lockerId)
            .child("user")
            .child(uid)
            .child("/balance")
            .transaction(balance => {
                return balance + change;
            }));

        const newInventory = snapshot.val();

        if (newInventory < 2 && change < 0) {
            promiseBag.push(admin.messaging().sendToTopic("inventoryLow", {
                notification: {
                    title: "MIO is running empty",
                    body: `MIO is running empty, only ${newInventory} available in Locker`
                }
            }));
        }

        promiseBag.push(admin.database()
            .ref("/locker")
            .child(lockerId)
            .child("/history").push().set({
                newInventory: newInventory,
                change: change,
                timestamp: Date.now(),
                user: uid
            }));

        await Promise.all(promiseBag as Promise<any>[]);

        return true;
    });

export const acceptInvitation = functions.region("europe-west1")
    .https.onCall(async (data, context) => {
        if (!context.auth) return;

        const {invitationCode} = data;

        const uid = context.auth.uid;

        const lockerId = await admin.database()
            .ref("/invitation")
            .child(invitationCode)
            .once("value")
            .then(value => value.val());

        if (!lockerId) {
            return false;
        }
        const promiseBag = [];

        promiseBag.push(admin.database()
            .ref("/locker")
            .child(lockerId)
            .child("user")
            .child(uid)
            .set({
                role: "NORMAL",
                balance: 0
            }));

        promiseBag.push(admin.database()
            .ref("/locker")
            .child(lockerId)
            .child("name")
            .once("value")
            .then(value => value.val())
            .then(lockerName => {
                return admin.database()
                    .ref("/account")
                    .child(uid)
                    .child("locker")
                    .child(lockerId).set(lockerName);
            }));

        await Promise.all(promiseBag);

        return {
            lockerId
        };
    });
