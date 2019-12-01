package de.wiomoc.miocheck

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import de.wiomoc.miocheck.services.ConnectionService
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {
    val RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val connectionService by inject<ConnectionService>()

        connectionService.onConnected {
            toolbar_progress.visibility = if (it) View.GONE else View.VISIBLE
        }

        pager.adapter =
            object : FragmentStatePagerAdapter(
                supportFragmentManager,
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
            ) {
                override fun getItem(position: Int): Fragment = when (position) {
                    0 -> AvailabilityFragment()
                    1 -> LockerFragment()
                    else -> throw IllegalArgumentException()
                }

                override fun getCount() = 2

                override fun getPageTitle(position: Int) = when (position) {
                    0 -> getString(R.string.page_title_availability)
                    1 -> getString(R.string.page_title_locker)
                    else -> throw IllegalArgumentException()
                }
            }
        tabs.setupWithViewPager(pager)

        navigateToLoginIfNecessary()
    }

    private fun navigateToLoginIfNecessary() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                // ...
            } else {

            }
        }
    }
}
