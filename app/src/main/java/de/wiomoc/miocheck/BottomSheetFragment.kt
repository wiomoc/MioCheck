package de.wiomoc.miocheck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.wiomoc.miocheck.services.LockerService
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*
import org.koin.android.ext.android.inject

class BottomSheetFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_bottom_sheet, container).apply {
            val lockerService by inject<LockerService>()

            lockerService.lockPin.observe(this@BottomSheetFragment, Observer {
                fragment_bottom_view_lock_txt.text = it
            })
        }
}
