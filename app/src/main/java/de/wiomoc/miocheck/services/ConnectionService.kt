package de.wiomoc.miocheck.services

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class ConnectionService(val dbReference: DatabaseReference) {
    fun onConnected(cb: (boolean: Boolean) -> Unit) {
        dbReference.database.getReference(".info/connected")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    cb(snapshot.getValue(Boolean::class.java)!!)
                }

            })
    }
}
