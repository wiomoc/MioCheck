package de.wiomoc.miocheck

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import org.koin.android.ext.android.inject
import java.text.DateFormat


class AvailabilityFragment : Fragment() {
    lateinit var adapter: FirebaseListAdapter<ShopStatus>

    val dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        val notificationService by inject<NotificationService>()
        val dbService by inject<AvailabilityService>()

        val dbShopStatus = dbService.shopStatus()

        adapter = object : FirebaseListAdapter<ShopStatus>(
            FirebaseListOptions.Builder<ShopStatus>()
                .setQuery(dbShopStatus.limitToFirst(50), ShopStatusParser())
                .setLayout(R.layout.item_shop)
                .build()
        ) {
            override fun populateView(view: View, item: ShopStatus, position: Int) {
                view.apply {
                    findViewById<TextView>(R.id.item_shop_name).text = item.name
                    findViewById<TextView>(R.id.item_last_changed).text = dateTimeFormatter.format(item.lastChanged)
                    findViewById<Switch>(R.id.item_available).apply {
                        setOnCheckedChangeListener(null)
                        isChecked = item.status == Status.AVAILABLE
                        setOnCheckedChangeListener { compoundButton, checked ->
                            dbService.changeStatus(item.id!!, if (checked) Status.AVAILABLE else Status.EMPTY)
                        }
                    }
                    findViewById<ImageView>(R.id.item_alarm).apply {
                        var subscibed = notificationService.hasSubscripedToTopic(item.id!!)

                        setImageResource(if (subscibed) R.drawable.ic_alarm_off else R.drawable.ic_alarm_add)

                        setOnClickListener {
                            if (subscibed) {
                                notificationService.unsubscribeToNotification(item.id!!)
                                setImageResource(R.drawable.ic_alarm_add)
                                subscibed = false
                            } else {
                                notificationService.subscribeToNotification(item.id!!)
                                setImageResource(R.drawable.ic_alarm_off)
                                subscibed = true
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
            findViewById<ListView>(R.id.list_shops).adapter = adapter
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
