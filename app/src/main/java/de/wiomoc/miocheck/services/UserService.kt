package de.wiomoc.miocheck.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference

class UserService(private val dbReference: DatabaseReference) {
    fun getInvolvedLockers() =
        FirebaseAuth.getInstance().currentUser?.let { user ->
            dbReference
                .child("account")
                .child(user.uid)
                .child("locker")
                .orderByValue()
        }
}
