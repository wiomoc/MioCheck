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
        lockerService.subscribeBalanceChange(this) {
            view!!.findViewById<TextView>(R.id.locker_account_credit_txt).text = it.toString()
        }

        lockerService.subscribeInventoryChange(this) {
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
            setViewPortOffsets(0f, 0f, 0f, 0f)
            lockerService.subscribeHistoryChange(this@LockerFragment) { history ->
                val dataSet = LineDataSet(
                    history.map { Entry(it.timestamp.toFloat(), it.inventory.toFloat()) },
                    getString(R.string.locker_available)
                )

                dataSet.apply {
                    mode = LineDataSet.Mode.HORIZONTAL_BEZIER;
                    fillAlpha = 255;
                    fillColor = resources.getColor(R.color.colorAccent);
                    setDrawFilled(true)
                    setDrawValues(false)
                    color = fillColor
                    setDrawCircles(false)
                }

                data = LineData(dataSet)
                invalidate()

                data.isHighlightEnabled = false
            }
            description = null

            setTouchEnabled(true)

            dragDecelerationFrictionCoef = 0.9f

            isDragEnabled = true
            setScaleEnabled(true)
            setDrawGridBackground(false)
            isHighlightPerDragEnabled = true


            // get the legend (only possible after setting data)
            legend.isEnabled = false

            xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
            xAxis.textSize = 10f
            xAxis.textColor = Color.WHITE
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(false)
            xAxis.textColor = Color.BLACK
            xAxis.setCenterAxisLabels(true)
            xAxis.granularity = 1f

            xAxis.valueFormatter = object :
                ValueFormatter() {
                private val format = SimpleDateFormat("dd MMM HH:mm")

                override fun getFormattedValue(value: Float): String {
                    return format.format(Date(value.toLong()))
                }
            }

            val leftAxis = axisLeft
            leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = true
            leftAxis.axisMinimum = 0f
            leftAxis.textColor = Color.BLACK
            leftAxis.yOffset = -5f

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
