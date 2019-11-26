package de.wiomoc.miocheck

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_locker.*
import org.koin.android.ext.android.inject


class LockerFragment : Fragment() {

    private val lockerService by inject<LockerService>()
    private val notificationService by inject<NotificationService>()

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
        lockerService.subscribeBalanceChange {
            view!!.findViewById<TextView>(R.id.locker_account_credit_txt).text = it.toString()
        }

        lockerService.subscribeInventoryChange {
            view!!.findViewById<TextView>(R.id.locker_available_txt).text = it.toString()
            takeButton.isEnabled = it > 0
        }

        takeButton.setOnClickListener {
            lockerService.takeMio()
        }

        locker_add_button.setOnClickListener {
            lockerService.addMio()
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
            if (notificationService.hasSubscribedToTopic(INVENTORY_LOW_TOPIC))
                notificationService.unsubscribeToNotification(INVENTORY_LOW_TOPIC)
            else
                notificationService.subscribeToNotification(INVENTORY_LOW_TOPIC)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
