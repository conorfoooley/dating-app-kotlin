const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp();

exports.notifyNewMatch = functions.firestore
    .document('users/{city}/{gender}/{userId}/matched/{cardItem}')
    .onCreate((docSnapshot, context) => {
        const cardUserToNotifiyItem = docSnapshot.data();
        const cardUserToNotifiyId = cardUserToNotifiyItem['baseUserInfo'].userId;

        return admin.firestore().doc('usersBase/' + cardUserToNotifiyId).get().then(userDoc => {
            const registrationTokens = userDoc.get('registrationTokens')

            const notificationBody = "You've got a new match!"
            const payload = {
                data: {
                    CONTENT: notificationBody,
                  	TYPE: "NEW_MATCH"
                }
            }

            return admin.messaging().sendToDevice(registrationTokens, payload).then( response => {
                const stillRegisteredTokens = registrationTokens

                response.results.forEach((result, index) => {
                    const error = result.error
                    if (error) {
                        const failedRegistrationToken = registrationTokens[index]
                        console.error('mylogs_fcm', failedRegistrationToken, error)
                        if (error.code === 'messaging/invalid-registration-token'
                            || error.code === 'messaging/registration-token-not-registered') {
                                const failedIndex = stillRegisteredTokens.indexOf(failedRegistrationToken)
                                if (failedIndex > -1) {
                                    stillRegisteredTokens.splice(failedIndex, 1)
                                }
                            }
                    }
                })

                return admin.firestore().doc("usersBase/" + cardUserToNotifiyId).update({
                    registrationTokens: stillRegisteredTokens
                })
            })
        })
    })
