package de.wiomoc.miocheck.services

import androidx.lifecycle.LifecycleOwner
import com.google.firebase.database.DatabaseReference

class ConnectionService(private val dbReference: DatabaseReference) {
    fun onConnected(lifecycleOwner: LifecycleOwner, cb: (boolean: Boolean) -> Unit) {
        LifecycleAwareValueEventListener.start(
            lifecycleOwner,
            dbReference.database.getReference(".info/connected")
        ) {
            cb(it.getValue(Boolean::class.java)!!)
        }
    }
}
