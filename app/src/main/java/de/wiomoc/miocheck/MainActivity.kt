package de.wiomoc.miocheck

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    val RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        pager.adapter =
            object : FragmentStatePagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
                override fun getItem(position: Int) = when (position) {
                    0 -> AvailabilityFragment()
                    1 -> LockerFragment()
                    else -> throw IllegalArgumentException()
                }

                override fun getCount() = 2

                override fun getPageTitle(position: Int) = when (position) {
                    0 -> "Availability"
                    1 -> "Locker"
                    else -> throw IllegalArgumentException()
                }
            }
        tabs.setupWithViewPager(pager)

        navigateToLoginIfNecessary()
    }

    fun navigateToLoginIfNecessary() {
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
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        menuInflater.inflate(R.menu.activity_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_add_shop -> {
            navigateAddShop()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun navigateAddShop() {
        AddShopDialogFragment().show(supportFragmentManager, "add")
    }
}
