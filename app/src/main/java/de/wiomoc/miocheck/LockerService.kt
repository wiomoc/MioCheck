package de.wiomoc.miocheck

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions

class LockerService(
    private val dbReference: DatabaseReference,
    private val functions: FirebaseFunctions
) {
    fun addMio() {
        functions.getHttpsCallable("mioTransaction").call(mapOf("change" to 1))
    }

    fun takeMio() {
        functions.getHttpsCallable("mioTransaction").call(mapOf("change" to -1))
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
