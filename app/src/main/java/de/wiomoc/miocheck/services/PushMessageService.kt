package de.wiomoc.miocheck.services

import com.google.firebase.messaging.FirebaseMessaging

class PushMessageService(
    private val preferenceService: PreferenceService
) {

    fun hasSubscribedToTopic(id: String) = preferenceService.hasSubscribedToTopic(id)

    fun subscribeToNotification(id: String) =
        FirebaseMessaging.getInstance().subscribeToTopic(id)
            .addOnSuccessListener {
                preferenceService.addSubscription(id)
            }

    fun unsubscribeToNotification(id: String) =
        FirebaseMessaging.getInstance().unsubscribeFromTopic(id)
            .addOnSuccessListener {
                preferenceService.removeSubscription(id)
            }
}
