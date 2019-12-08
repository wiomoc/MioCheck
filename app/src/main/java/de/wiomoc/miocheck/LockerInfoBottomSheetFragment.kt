package de.wiomoc.miocheck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.wiomoc.miocheck.services.LockerService
import kotlinx.android.synthetic.main.bottom_sheet_locker_info.*
import org.koin.android.ext.android.inject

class LockerInfoBottomSheetFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.bottom_sheet_locker_info, container).apply {
            val lockerService by inject<LockerService>()

            lockerService.lockPin.observe(this@LockerInfoBottomSheetFragment, Observer {
                locker_create_code_txt.text = it
            })
        }
}
