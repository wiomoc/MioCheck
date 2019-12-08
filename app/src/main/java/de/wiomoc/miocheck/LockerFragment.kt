package de.wiomoc.miocheck

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import de.wiomoc.miocheck.services.LockerId
import de.wiomoc.miocheck.services.LockerService
import de.wiomoc.miocheck.services.PushMessageService
import de.wiomoc.miocheck.services.UserService
import kotlinx.android.synthetic.main.fragment_locker.*
import org.koin.android.ext.android.inject

class LockerFragment : Fragment(), NetworkErrorSnackbarMixin {

    private val lockerService by inject<LockerService>()
    private val userService by inject<UserService>()
    private val notificationService by inject<PushMessageService>()

    private val INVENTORY_LOW_TOPIC = "inventoryLow"

    private var menu: Menu? = null

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

        if (userService.selectedLockerId.value == null) {
            locker_account_credit_label.visibility = View.GONE
            locker_account_credit_txt.visibility = View.GONE
            locker_available_label.visibility = View.GONE
            locker_available_txt.visibility = View.GONE
            locker_add_mio_button.visibility = View.GONE
            locker_take_mio_button.visibility = View.GONE
            locker_create_button.visibility = View.VISIBLE
            locker_create_button.setOnClickListener {
                LockerCreateDialogFragment().show(fragmentManager!!, "create_locker")
            }
            userService.selectedLockerId.observe(this, object: Observer<LockerId> {
                override fun onChanged(lockerId: LockerId?) {
                    if(lockerId != null) {
                        locker_account_credit_label.visibility = View.VISIBLE
                        locker_account_credit_txt.visibility = View.VISIBLE
                        locker_available_label.visibility = View.VISIBLE
                        locker_available_txt.visibility = View.VISIBLE
                        locker_add_mio_button.visibility = View.VISIBLE
                        locker_take_mio_button.visibility = View.VISIBLE
                        locker_create_button.visibility = View.GONE
                        menu?.findItem(R.id.fragment_locker_menu_alarm)?.isVisible = true
                        menu?.findItem(R.id.fragment_locker_menu_info)?.isVisible = true
                        updateAlertMenuItem()
                        userService.selectedLockerId.removeObserver(this)
                    }
                }
            })
        }

        val takeButton = locker_take_mio_button
        lockerService.balance.observe(this, Observer<Long> {
            view!!.findViewById<TextView>(R.id.locker_account_credit_txt).text = it.toString()
        })

        lockerService.inventory.observe(this, Observer<Long> {
            view!!.findViewById<TextView>(R.id.locker_available_txt).text = it.toString()
            takeButton.isEnabled = it > 0
        })

        takeButton.setOnClickListener {
            lockerService.takeMio().addOnFailureListener(this)
        }

        locker_add_mio_button.setOnClickListener {
            lockerService.addMio().addOnFailureListener(this)
        }

        locker_history_chart.apply {
            animation.duration = 0
            gradientFillColors =
                intArrayOf(
                    resources.getColor(R.color.colorAccent),
                    Color.TRANSPARENT
                )

            lockerService.history.observe(this@LockerFragment, Observer<List<LockerService.HistoryEntry>> { history ->
                val lineSet = linkedMapOf<String, Float>()
                history.map { it.timestamp.toString() to it.inventory.toFloat() }
                    .toMap(lineSet)

                animate(lineSet)
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_locker, menu)
        this.menu = menu
        if (userService.selectedLockerId.value == null) {
            menu.findItem(R.id.fragment_locker_menu_alarm).isVisible = false
            menu.findItem(R.id.fragment_locker_menu_info).isVisible = false
        } else {
            updateAlertMenuItem()
        }
    }

    fun updateAlertMenuItem() {
        menu?.findItem(R.id.fragment_locker_menu_alarm)
            ?.setIcon(
                if (notificationService.hasSubscribedToTopic(INVENTORY_LOW_TOPIC))
                    R.drawable.ic_alarm_off
                else
                    R.drawable.ic_alarm_add
            )
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.fragment_locker_menu_alarm -> {
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
        R.id.fragment_locker_menu_info -> {
            LockerInfoBottomSheetFragment().show(fragmentManager!!, "sheet")
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
