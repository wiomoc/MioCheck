package de.wiomoc.miocheck

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import org.koin.android.ext.android.inject
import java.text.DateFormat


class LockerFragment : Fragment() {

    val lockerService by inject<LockerService>()

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

        val takeButton = view!!.findViewById<Button>(R.id.button_take)
        lockerService.subscribeBalanceChange {
            view!!.findViewById<TextView>(R.id.account_credit).text = it.toString()
        }

        lockerService.subscribeInventoryChange {
            view!!.findViewById<TextView>(R.id.locker_available).text = it.toString()
            takeButton.isEnabled = it > 0
        }

        takeButton.setOnClickListener {
            lockerService.takeMio()
        }

        view!!.findViewById<Button>(R.id.button_add).setOnClickListener {
            lockerService.addMio()
        }

    }

    override fun onStop() {
        super.onStop()
    }
}
