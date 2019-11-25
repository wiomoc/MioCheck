package de.wiomoc.miocheck

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions

class LockerService() {
    var database = FirebaseDatabase.getInstance()
    var dbReference = database.reference
    val functions = FirebaseFunctions.getInstance("europe-west1")

    fun addMio() {
        functions.getHttpsCallable("addMio").call().addOnFailureListener {
            it.printStackTrace()
        }
    }

    fun takeMio() {
        functions.getHttpsCallable("takeMio").call()
    }

    fun subscribeInventoryChange(cb: ((Long) -> Unit)) = dbReference
        .child("locker")
        .child("inventory")
        .addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(p0: DataSnapshot) {
                cb(p0.value as Long)
            }
        })

    fun subscribeBalanceChange(cb: ((Long) -> Unit)) =
        FirebaseAuth.getInstance().currentUser?.let { user ->
            dbReference
                .child("account")
                .child(user.uid)
                .child("balance")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        cb((p0.value ?: 0L) as Long)
                    }
                })
        }
}
