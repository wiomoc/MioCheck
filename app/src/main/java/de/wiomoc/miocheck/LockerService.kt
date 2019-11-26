package de.wiomoc.miocheck

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions

class LockerService(
    private val dbReference: DatabaseReference,
    private val functions: FirebaseFunctions
) {

    class LifecycleAwareValueEventListener private constructor(
        private val query: Query,
        private val valueEventListener: ValueEventListener
    ) :
        LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            query.addValueEventListener(valueEventListener)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            query.removeEventListener(valueEventListener)
        }

        companion object {
            fun start(lifecycleOwner: LifecycleOwner, query: Query, cb: (snapshot: DataSnapshot) -> Unit) {
                val listener = LifecycleAwareValueEventListener(query, object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(snapshot: DataSnapshot) {
                        cb(snapshot)
                    }
                })

                lifecycleOwner.lifecycle.let {
                    if (it.currentState == Lifecycle.State.STARTED) {
                        listener.onStart()
                    }
                    it.addObserver(listener)
                }
            }
        }
    }

    fun addMio() {
        functions.getHttpsCallable("mioTransaction").call(mapOf("change" to 1))
    }

    fun takeMio() {
        functions.getHttpsCallable("mioTransaction").call(mapOf("change" to -1))
    }

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
                .limitToLast(50)
                .orderByChild("timestamp")
        ) {
            cb(it.children.map {
                val timestamp = it.child("timestamp").getValue(Long::class.java)!!
                val inventory = it.child("newInventory").getValue(Long::class.java)!!
                HistoryEntry(timestamp, inventory)
            })
        }
    }
}
