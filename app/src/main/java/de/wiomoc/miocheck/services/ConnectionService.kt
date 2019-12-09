package de.wiomoc.miocheck.services

import com.google.firebase.database.DatabaseReference

class ConnectionService(private val dbReference: DatabaseReference) {
    val connected by lazy {
        dbReference.database.getReference(".info/connected").toLiveData { it.getValue(Boolean::class.java)!! }
    }
}
