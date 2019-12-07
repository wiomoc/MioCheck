package de.wiomoc.miocheck.services

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.functions.FirebaseFunctions
import java.util.concurrent.TimeUnit

class LockerService(
    private val dbReference: DatabaseReference,
    private val functions: FirebaseFunctions,
    private val userService: UserService,
    private val auth: FirebaseAuth
) {

    private fun mioTransaction(change: Int) =
        functions.getHttpsCallable("mioTransaction")
            .withTimeout(6, TimeUnit.SECONDS)
            .call(mapOf("lockerId" to userService.selectedLockerId.value!!, "change" to change))

    fun addMio() = mioTransaction(1)

    fun takeMio() = mioTransaction(-1)

    data class HistoryEntry(val timestamp: Long, val inventory: Long)

    val lockPin by lazy {
        Transformations.switchMap(userService.selectedLockerId) { id ->
            dbReference
                .child("locker")
                .child(id)
                .child("pin")
                .toLiveData {
                    it.value.toString()
                }
        }
    }

    val history by lazy {
        Transformations.switchMap(userService.selectedLockerId) { lockerId ->
            if(lockerId == null) return@switchMap null
            dbReference
                .child("locker")
                .child(lockerId)
                .child("history")
                .limitToLast(20)
                .orderByChild("timestamp")
                .toLiveData {
                    it.children.map {
                        val timestamp = it.child("timestamp").getValue(Long::class.java)!!
                        val inventory = it.child("newInventory").getValue(Long::class.java)!!
                        HistoryEntry(timestamp, inventory)
                    }
                }
        }
    }

    val inventory by lazy {
        Transformations.switchMap(userService.selectedLockerId) { lockerId ->
            if(lockerId == null) return@switchMap null
            dbReference
                .child("locker")
                .child(lockerId)
                .child("inventory")
                .toLiveData {
                    it.getValue(Long::class.java)!!
                }
        }
    }

    val balance by lazy {
        Transformations.switchMap(userService.selectedLockerId) { lockerId ->
            if(lockerId == null) return@switchMap null
            dbReference
                .child("locker")
                .child(lockerId)
                .child("user")
                .child(auth.currentUser!!.uid)
                .child("balance")
                .toLiveData {
                    it.getValue(Long::class.java)!!
                }
        }
    }
}
