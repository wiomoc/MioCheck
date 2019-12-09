package de.wiomoc.miocheck

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import de.wiomoc.miocheck.services.*
import kotlinx.android.synthetic.main.fragment_locker.*
import org.koin.android.ext.android.inject

class LockerFragment : Fragment(), NetworkErrorSnackbarMixin {

    private val lockersService by inject<LockersService>()
    private val lockerInfoService by inject<LockerInfoService>()
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

        if (lockersService.selectedLockerId.value == null) {
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
            lockersService.selectedLockerId.observe(this, object: Observer<LockerId> {
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
                        lockersService.selectedLockerId.removeObserver(this)
                    }
                }
            })
        }

        val takeButton = locker_take_mio_button
        lockerInfoService.balance.observe(this, Observer<Long> {
            view!!.findViewById<TextView>(R.id.locker_account_credit_txt).text = it.toString()
        })

        lockerInfoService.inventory.observe(this, Observer<Long> {
            view!!.findViewById<TextView>(R.id.locker_available_txt).text = it.toString()
            takeButton.isEnabled = it > 0
        })

        takeButton.setOnClickListener {
            lockerInfoService.takeMio().addOnFailureListener(this)
        }

        locker_add_mio_button.setOnClickListener {
            lockerInfoService.addMio().addOnFailureListener(this)
        }

        locker_history_chart.apply {
            animation.duration = 0
            gradientFillColors =
                intArrayOf(
                    resources.getColor(R.color.colorAccent),
                    Color.TRANSPARENT
                )

            lockerInfoService.history.observe(this@LockerFragment, Observer<List<LockerInfoService.HistoryEntry>> { history ->
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
        if (lockersService.selectedLockerId.value == null) {
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
