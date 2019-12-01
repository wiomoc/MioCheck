package de.wiomoc.miocheck.services

import androidx.lifecycle.LifecycleOwner
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import java.util.concurrent.TimeUnit

class LockerService(
    private val dbReference: DatabaseReference,
    private val functions: FirebaseFunctions
) {

    private fun mioTransaction(change: Int) =
        functions.getHttpsCallable("mioTransaction")
            .withTimeout(6, TimeUnit.SECONDS)
            .call(mapOf("change" to change))


    fun addMio() = mioTransaction(1)

    fun takeMio() = mioTransaction(-1)

    fun subscribeInventoryChange(owner: LifecycleOwner, cb: ((Long) -> Unit)) {
        LifecycleAwareValueEventListener.start(
            owner,
            dbReference
                .child("locker")
                .child("inventory")
        ) {
            cb(it.value as Long)
        }
    }


    fun subscribeBalanceChange(owner: LifecycleOwner, cb: ((Long) -> Unit)) {
        FirebaseAuth.getInstance().currentUser?.let { user ->
            LifecycleAwareValueEventListener.start(
                owner,
                dbReference
                    .child("account")
                    .child(user.uid)
                    .child("balance")
            ) {
                cb((it.value ?: 0L) as Long)
            }
        }

    }

    data class HistoryEntry(val timestamp: Long, val inventory: Long)

    fun subscribeHistoryChange(owner: LifecycleOwner, cb: (List<HistoryEntry>) -> Unit) {
        LifecycleAwareValueEventListener.start(
            owner,
            dbReference
                .child("history")
                .limitToLast(20)
                .orderByChild("timestamp")
        ) {
            cb(it.children.map {
                val timestamp = it.child("timestamp").getValue(Long::class.java)!!
                val inventory = it.child("newInventory").getValue(Long::class.java)!!
                HistoryEntry(timestamp, inventory)
            })
        }
    }

    fun subscribeLockPinChange(owner: LifecycleOwner, cb: (String) -> Unit) {
        LifecycleAwareValueEventListener.start(
            owner,
            dbReference.child("lockPin")
        ) {
            cb(it.value.toString())
        }
    }
}
