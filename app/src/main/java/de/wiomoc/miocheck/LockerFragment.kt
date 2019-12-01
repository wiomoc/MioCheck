package de.wiomoc.miocheck

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import de.wiomoc.miocheck.services.LockerService
import de.wiomoc.miocheck.services.PushMessageService
import kotlinx.android.synthetic.main.fragment_locker.*
import org.koin.android.ext.android.inject

class LockerFragment : Fragment(), NetworkErrorSnackbarMixin {

    private val lockerService by inject<LockerService>()
    private val notificationService by inject<PushMessageService>()

    private val INVENTORY_LOW_TOPIC = "inventoryLow"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_locker, container, false)
    }

    override fun onStart() {
        super.onStart()

        val takeButton = locker_take_button
        lockerService.subscribeBalanceChange(this) {
            view!!.findViewById<TextView>(R.id.locker_account_credit_txt).text = it.toString()
        }

        lockerService.subscribeInventoryChange(this) {
            view!!.findViewById<TextView>(R.id.locker_available_txt).text = it.toString()
            takeButton.isEnabled = it > 0
        }

        takeButton.setOnClickListener {
            lockerService.takeMio().addOnFailureListener(this)
        }

        locker_add_button.setOnClickListener {
            lockerService.addMio().addOnFailureListener(this)
        }

        locker_history_chart.apply {
            animation.duration = 0
            gradientFillColors =
                intArrayOf(
                    resources.getColor(R.color.colorAccent),
                    Color.TRANSPARENT
                )

            lockerService.subscribeHistoryChange(this@LockerFragment) { history ->
                val lineSet = linkedMapOf<String, Float>()
                history.map { it.timestamp.toString() to it.inventory.toFloat() }
                    .toMap(lineSet)

                animate(lineSet)
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_locker, menu)
        menu.findItem(R.id.fragment_locker_alarm)
            .setIcon(
                if (notificationService.hasSubscribedToTopic(INVENTORY_LOW_TOPIC))
                    R.drawable.ic_alarm_off
                else
                    R.drawable.ic_alarm_add
            )
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.fragment_locker_alarm -> {
            if (notificationService.hasSubscribedToTopic(INVENTORY_LOW_TOPIC)) {
                notificationService.unsubscribeToNotification(INVENTORY_LOW_TOPIC)
                    .addOnSuccessListener {
                        item.setIcon(R.drawable.ic_alarm_add)
                    }
            } else {
                notificationService.subscribeToNotification(INVENTORY_LOW_TOPIC)
                    .addOnSuccessListener {
                        item.setIcon(R.drawable.ic_alarm_off)
                    }
            }.addOnFailureListener(this)
            true
        }
        R.id.fragment_locker_info -> {
            BottomSheetFragment().show(fragmentManager!!, "sheet")
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
