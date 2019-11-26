package de.wiomoc.miocheck

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_locker.*
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*


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

        locker_history_chart.apply {
            val list = mutableListOf<Entry>()

            val lineDataSet = LineDataSet(mutableListOf(), getString(R.string.locker_available))

            lockerService.history().addChildEventListener(object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val timestamp = p0.child("timestamp").getValue(Long::class.java)!!
                    val inventory = p0.child("newInventory").getValue(Long::class.java)!!

                    println("$timestamp - $inventory")

                    list.add(Entry(timestamp.toFloat(), inventory.toFloat()))
                    data.dataSets[0] = LineDataSet(list, getString(R.string.locker_available))
                    data.notifyDataChanged()
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            })

            data = LineData(lineDataSet)

            description.isEnabled = false

            // enable touch gestures
            setTouchEnabled(true)

            dragDecelerationFrictionCoef = 0.9f

            // enable scaling and dragging
            isDragEnabled = true
            setScaleEnabled(true)
            setDrawGridBackground(false)
            isHighlightPerDragEnabled = true

            // set an alternative background color
            setBackgroundColor(Color.WHITE)
            setViewPortOffsets(0f, 0f, 0f, 0f)

            // get the legend (only possible after setting data)
            legend.isEnabled = false

            xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
            xAxis.textSize = 10f
            xAxis.textColor = Color.WHITE
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(true)
            xAxis.textColor = Color.rgb(255, 192, 56)
            xAxis.setCenterAxisLabels(true)
            xAxis.granularity = 1f // one hour

            xAxis.valueFormatter = object :
                    ValueFormatter() {
                private val format = SimpleDateFormat("dd MMM HH:mm")

                override fun getFormattedValue(value: Float): String {
                    return format.format(Date(value.toLong()))
                }
            }

            val leftAxis = axisLeft
            leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
            leftAxis.textColor = ColorTemplate.getHoloBlue()
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = true
            leftAxis.axisMinimum = 0f
            leftAxis.yOffset = -9f
            leftAxis.textColor = Color.rgb(255, 192, 56)
            leftAxis.setDrawLabels(false)

            axisRight.isEnabled = false
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
