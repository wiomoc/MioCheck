package de.wiomoc.miocheck

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_availability.*
import org.koin.android.ext.android.inject
import java.text.DateFormat


class AvailabilityFragment : Fragment() {
    lateinit var adapter: FirebaseListAdapter<ShopStatus>

    private val dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val notificationService by inject<NotificationService>()
        val dbService by inject<AvailabilityService>()

        adapter = object : FirebaseListAdapter<ShopStatus>(
            FirebaseListOptions.Builder<ShopStatus>()
                .setQuery(dbService.getShopStatuses(), ShopStatusParser)
                .setLayout(R.layout.item_availability_shop)
                .setLifecycleOwner(this)
                .build()
        ) {
            override fun populateView(view: View, item: ShopStatus, position: Int) {
                view.apply {
                    findViewById<TextView>(R.id.item_availability_shop_shop_name_tat).text = item.name
                    findViewById<TextView>(R.id.item_availability_shop_last_changed_tat).text =
                        dateTimeFormatter.format(item.lastChanged)
                    findViewById<Switch>(R.id.item_availability_shop_available_switch).apply {
                        setOnCheckedChangeListener(null)
                        isChecked = item.status == Status.AVAILABLE
                        setOnCheckedChangeListener { _, checked ->
                            dbService.changeStatus(item.id!!, if (checked) Status.AVAILABLE else Status.EMPTY)
                        }
                    }
                    findViewById<ImageView>(R.id.item_availability_shop_alarm_image).apply {
                        var subscribed = notificationService.hasSubscribedToTopic(item.id!!)

                        setImageResource(if (subscribed) R.drawable.ic_alarm_off else R.drawable.ic_alarm_add)

                        setOnClickListener {
                            if (subscribed) {
                                notificationService.unsubscribeToNotification(item.id!!)
                                setImageResource(R.drawable.ic_alarm_add)
                                subscribed = false
                            } else {
                                notificationService.subscribeToNotification(item.id!!)
                                setImageResource(R.drawable.ic_alarm_off)
                                subscribed = true
                            }
                        }
                    }

                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_availability, container, false).apply {
            findViewById<ListView>(R.id.fragment_availability_shops_list).adapter = adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.fragment_availability, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.fragment_availability_add_shop -> {
            navigateAddShop()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun navigateAddShop() {
        AddShopDialogFragment().show(fragmentManager!!, "add")
    }
}
