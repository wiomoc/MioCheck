package de.wiomoc.miocheck

import android.content.Context
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging

class NotificationService(
    val preferences: Preferences,
    val context: Context
) {

    fun hasSubscripedToTopic(id: String) = preferences.hasSubscripedToTopic(id)

    fun subscribeToNotification(id: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(id)
            .addOnCompleteListener { task ->
                preferences.addSubscription(id)

                Toast.makeText(context, "Subscribed", Toast.LENGTH_SHORT).show()
            }
    }

    fun unsubscribeToNotification(id: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(id)
            .addOnCompleteListener { task ->
                preferences.removeSubscription(id)
                Toast.makeText(context, "Unsubscribed", Toast.LENGTH_SHORT).show()
            }
    }

}
