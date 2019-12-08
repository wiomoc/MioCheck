package de.wiomoc.miocheck.services

import android.util.Base64
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions

typealias LockerId = String

class UserService(
    private val dbReference: DatabaseReference,
    private val functions: FirebaseFunctions,
    private val preferenceService: PreferenceService,
    private val auth: FirebaseAuth
) {
    init {
        auth.addAuthStateListener {
            if (it.currentUser == null) {
                preferenceService.selectedLockerId = null
                selectedLockerId.postValue(null)
            } else {
                selectedLockerId.fetchSelectedLocker()
            }
        }
    }

    val selectedLockerId by lazy {
        val liveData = MutableLiveData<LockerId>()
        liveData.fetchSelectedLocker()
        liveData
    }

    private fun MutableLiveData<LockerId>.fetchSelectedLocker() {
        val selectedLockerId = preferenceService.selectedLockerId
        if (selectedLockerId != null) {
            postValue(selectedLockerId)
        } else {
            getInvolvedLockers()?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.firstOrNull()?.key?.let {
                        preferenceService.selectedLockerId = it
                        postValue(it)
                    }
                }
            })
        }
    }

    fun selectLocker(id: LockerId) {
        preferenceService.selectedLockerId = id
        selectedLockerId.postValue(id)
    }

    fun getInvolvedLockers() =
        auth.currentUser?.let { user ->
            dbReference
                .child("account")
                .child(user.uid)
                .child("locker")
                .orderByValue()
        }

    fun acceptInvitation(invitationCode: String) = functions
        .getHttpsCallable("acceptInvitation")
        .call(mapOf("invitationCode" to invitationCode))
        .addOnSuccessListener {
            val data = it.data
            if (data is Map<*, *>) {
                val lockerId = data["lockerId"]
                selectLocker(lockerId!!.toString())
            }
        }

    fun createLocker(name: String, pin: String, image: ByteArray?) = functions
        .getHttpsCallable("createLocker")
        .call(
            mapOf(
                "name" to name,
                "pin" to pin,
                "image" to Base64.encodeToString(image, Base64.NO_WRAP)
            )
        )
        .addOnSuccessListener {
            val data = it.data
            if (data is Map<*, *>) {
                val lockerId = data["lockerId"]
                selectLocker(lockerId!!.toString())
            }
        }
}
