package de.wiomoc.miocheck

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.wiomoc.miocheck.services.AvailabilityService
import de.wiomoc.miocheck.services.ShopStatus
import de.wiomoc.miocheck.services.Status
import org.koin.android.ext.android.inject

class AddShopDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_add_shop, null)

        return AlertDialog.Builder(context!!)
            .setView(view)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val dbService by inject<AvailabilityService>()
                val name = view.findViewById<EditText>(R.id.dialog_add_shop_shop_name_edittext)
                    .text.toString()
                val status =
                    if (view.findViewById<Switch>(R.id.dialog_add_shop_available_switch).isChecked) Status.AVAILABLE else Status.EMPTY
                dbService.addShop(ShopStatus(null, name, status))
                dialogInterface.dismiss()
            }
            .create()
    }
}
