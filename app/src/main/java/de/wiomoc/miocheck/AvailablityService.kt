package de.wiomoc.miocheck

import com.firebase.ui.database.SnapshotParser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import java.util.*

enum class Status {
    EMPTY, AVAILABLE
}

data class ShopStatus(
    var id: String? = null,
    val name: String,
    val status: Status,
    val lastChanged: Date = Date()
)

class AvailabilityService(private val dbReference: DatabaseReference) {

    private fun shopStatus() = dbReference.child("shop-status")

    fun getShopStatuses() = shopStatus()
        .limitToFirst(50)
        .orderByChild("name")

    fun addShop(shopStatus: ShopStatus) = shopStatus().push().apply {
        child("name").setValue(shopStatus.name)
        child("status").setValue(shopStatus.status)
        child("lastChanged").setValue(shopStatus.lastChanged.time)
    }

    fun changeStatus(shop: String, status: Status) = shopStatus().child(shop).apply {
        child("status").setValue(status)
        child("lastChanged").setValue(Date().time)
    }
}

object ShopStatusParser : SnapshotParser<ShopStatus> {
    override fun parseSnapshot(snapshot: DataSnapshot) =
        ShopStatus(
            snapshot.key!!,
            snapshot.child("name").value!!.toString(),
            snapshot.child("status").getValue(Status::class.java)!!,
            Date(snapshot.child("lastChanged").getValue(Long::class.java)!!)
        )
}
