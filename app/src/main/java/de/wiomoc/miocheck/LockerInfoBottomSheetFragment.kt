package de.wiomoc.miocheck

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.wiomoc.miocheck.services.LockerAdminService
import de.wiomoc.miocheck.services.LockerInfoService
import org.koin.android.ext.android.inject
import de.wiomoc.miocheck.services.LockerAdminService.UserInfo
import kotlinx.android.synthetic.main.bottom_sheet_locker_info.view.*

class LockerInfoBottomSheetFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.bottom_sheet_locker_info, container).apply {
            val lockerInfoService by inject<LockerInfoService>()

            lockerInfoService.lockPin.observe(this@LockerInfoBottomSheetFragment, Observer {
                locker_info_code_txt.text = it
            })

            val lockerAdminService by inject<LockerAdminService>()

            lockerAdminService.isAdmin.observe(this@LockerInfoBottomSheetFragment,
                Observer { isAdmin ->
                    if (isAdmin) {
                        locker_info_add_user_btn.visibility = View.VISIBLE
                        locker_info_users.visibility = View.VISIBLE
                        locker_info_users.adapter = object :
                            FirebaseRecyclerAdapter<UserInfo, RecyclerView.ViewHolder>(
                                FirebaseRecyclerOptions.Builder<UserInfo>()
                                    .setQuery(lockerAdminService.userInfo()!!, LockerAdminService.UserInfoParser)
                                    .setLifecycleOwner(this@LockerInfoBottomSheetFragment)
                                    .build()
                            ) {
                            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                                object : RecyclerView.ViewHolder(
                                    layoutInflater.inflate(
                                        R.layout.item_locker_user_info,
                                        parent,
                                        false
                                    )
                                ) {}

                            override fun onBindViewHolder(
                                vh: RecyclerView.ViewHolder,
                                pos: Int,
                                userInfo: UserInfo
                            ) {
                                vh.itemView.apply {
                                    findViewById<TextView>(R.id.item_locker_user_info_name).text = userInfo.name
                                    findViewById<TextView>(R.id.item_locker_user_info_balance).text =
                                        userInfo.balance.toString()
                                }
                            }

                        }
                    } else {
                        locker_info_add_user_btn.visibility = View.GONE
                        locker_info_users.visibility = View.GONE
                    }
                })

            locker_info_add_user_btn.setOnClickListener {
                lockerAdminService.getInvitationCodeUrl { invitationCodeUrl ->
                    if (invitationCodeUrl != null) {
                        val share = Intent.createChooser(Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, invitationCodeUrl)
                            putExtra(Intent.EXTRA_TITLE, "Mio Locker Invitation")

                            // (Optional) Here we're passing a content URI to an image to be displayed
                            // TODO: Use Locker Profilepicture
                            //data = contentUri
                            //flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }, null)
                        startActivity(share)
                    }
                }
            }
        }!!
}
