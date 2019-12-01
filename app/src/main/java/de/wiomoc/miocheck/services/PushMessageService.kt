package de.wiomoc.miocheck.services

import android.content.Context
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import de.wiomoc.miocheck.R

class PushMessageService(
    private val preferenceService: PreferenceService,
    private val context: Context
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
