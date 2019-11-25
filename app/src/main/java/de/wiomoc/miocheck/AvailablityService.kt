package de.wiomoc.miocheck

import com.firebase.ui.database.SnapshotParser
import com.google.firebase.database.*
import java.util.*

enum class Status {
    EMPTY, AVAILABLE
}

data class ShopStatus(
    var id: String? = null,
    val name: String = "",
    val status: Status = Status.EMPTY,
    val lastChanged: Date = Date()
)

class AvailabilityService {
    var database = FirebaseDatabase.getInstance()
    var dbReference = database.reference

    fun shopStatus() = dbReference.child("shop-status")

    fun addShop(shopStatus: ShopStatus) = shopStatus().push().setValue(shopStatus)

    fun changeStatus(shop: String, status: Status) = shopStatus().child(shop).apply {
        child("status").setValue(status.toString())
        child("lastChanged").setValue(Date())
    }
}

class ShopStatusParser : SnapshotParser<ShopStatus> {
    override fun parseSnapshot(snapshot: DataSnapshot) = snapshot.getValue(ShopStatus::class.java)!!
        .apply {
            id = snapshot.key!!
        }
}
