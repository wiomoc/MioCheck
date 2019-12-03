package de.wiomoc.miocheck.services

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class LifecycleAwareValueEventListener private constructor(
    private val query: Query,
    private val valueEventListener: ValueEventListener
) :
    LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun onStart() {
        query.addValueEventListener(valueEventListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun onStop() {
        query.removeEventListener(valueEventListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun cleanup(source: LifecycleOwner) {
        source.lifecycle.removeObserver(this)
    }

    fun stop() {
        query.removeEventListener(valueEventListener)
    }

    companion object {
        fun start(lifecycleOwner: LifecycleOwner, query: Query, cb: (snapshot: DataSnapshot) -> Unit): LifecycleAwareValueEventListener {
            val listener = LifecycleAwareValueEventListener(
                query,
                object : ValueEventListener {
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
            return listener
        }
    }
}
