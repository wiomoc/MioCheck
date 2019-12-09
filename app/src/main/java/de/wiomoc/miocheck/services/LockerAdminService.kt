package de.wiomoc.miocheck.services

import androidx.lifecycle.Transformations
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions

class LockerAdminService(
    private val dbReference: DatabaseReference,
    private val functions: FirebaseFunctions,
    private val lockersService: LockersService,
    private val auth: FirebaseAuth
) {
    enum class UserRole { NORMAL, ADMIN }
    data class UserInfo(val uid: String, val name: String?, val balance: Long, val role: UserRole)

    val isAdmin by lazy {
        Transformations.switchMap(lockersService.selectedLockerId) { lockerId ->
            if (lockerId == null) return@switchMap null
            dbReference
                .child("locker")
                .child(lockerId)
                .child("user")
                .child(auth.currentUser!!.uid)
                .child("role")
                .toLiveData {
                    it.getValue(UserRole::class.java) == UserRole.ADMIN
                }
        }
    }

    fun getInvitationCodeUrl(cb: (String?) -> Unit) = lockersService.selectedLockerId.value?.let { lockerId ->
        dbReference
            .child("locker")
            .child(lockerId)
            .child("invitation")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) = cb(null)

                override fun onDataChange(snapshot: DataSnapshot) = cb(snapshot.value?.let {
                    "https://mio.wiomoc.de/i/$it"
                })
            })
    }

    fun userInfo() = lockersService.selectedLockerId.value?.let {
        dbReference
            .child("locker")
            .child(it)
            .child("user")
            .orderByChild("name")
    }

    object UserInfoParser : SnapshotParser<UserInfo> {
        override fun parseSnapshot(snapshot: DataSnapshot) =
            UserInfo(
                snapshot.key!!,
                snapshot.child("name").value?.toString(),
                snapshot.child("balance").getValue(Long::class.java) ?: 0L,
                snapshot.child("role").getValue(UserRole::class.java) ?: UserRole.NORMAL
            )
    }
}
