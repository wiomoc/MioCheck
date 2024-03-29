package de.wiomoc.miocheck

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseListOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import de.wiomoc.miocheck.services.ConnectionService
import de.wiomoc.miocheck.services.LockerId
import de.wiomoc.miocheck.services.LockersService
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    val RC_SIGN_IN = 1
    val SPINNER_ID_ADD_LOCKER = 0L

    val lockersService by inject<LockersService>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val connectionService by inject<ConnectionService>()

        connectionService.connected.observe(this, Observer {
            toolbar_progress.visibility = if (it) View.GONE else View.VISIBLE
        })

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

        createToolbarInvolvedLockerSpinner()

        navigateToLoginIfNecessary()

        if (intent?.action == Intent.ACTION_VIEW) {
            intent?.data?.let { handleUrlViewAction(it) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.action == Intent.ACTION_VIEW) {
            intent.data?.let { handleUrlViewAction(it) }
        }
    }

    private fun handleUrlViewAction(uri: Uri) {
        val pathSegments = uri.pathSegments
        if (pathSegments.size != 2) return
        if (pathSegments[0] != "i") return
        val invitationCode = pathSegments[1]

        lockersService.acceptInvitation(invitationCode).addOnSuccessListener {
            Snackbar.make(coordinator, R.string.snackbar_joined_locker, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateToolbarSelection(adapter: FirebaseListAdapter<String>, id: LockerId?) {
        (0 until adapter.count - 1)
            .firstOrNull { adapter.getRef(it).key == id }
            ?.let { toolbar_spinner.setSelection(it) }
    }

    private fun createToolbarInvolvedLockerSpinner() {
        val involvedLockers = lockersService.getInvolvedLockers() ?: return

        val adapter = object : FirebaseListAdapter<String>(
            FirebaseListOptions.Builder<String>()
                .setLifecycleOwner(this)
                .setQuery(involvedLockers, String::class.java)
                .setLayout(R.layout.item_toolbar_spinner_header)
                .build()
        ) {
            override fun onDataChanged() {
                super.onDataChanged()
                if (super.getCount() > 0) {
                    updateToolbarSelection(this, lockersService.selectedLockerId.value)
                    toolbar_spinner.visibility = View.VISIBLE
                    supportActionBar!!.setDisplayShowTitleEnabled(false)

                } else {
                    toolbar_spinner.visibility = View.GONE
                    supportActionBar!!.setDisplayShowTitleEnabled(true)
                }
            }

            override fun getCount(): Int {
                val lockerCount = super.getCount()
                return if (lockerCount == 0) 0 else lockerCount + 1
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup) =
                super.getView(min(position, super.getCount() - 1), convertView, parent)

            override fun getItemId(i: Int) = if (i != super.getCount())
                super.getItemId(i)
            else
                SPINNER_ID_ADD_LOCKER

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?) =
                layoutInflater.inflate(R.layout.item_toolbar_spinner, parent, false).also {
                    if (position == super.getCount()) {
                        (it as TextView).text = getString(R.string.menu_add_locker)
                        it.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add, 0, 0, 0)
                        it.compoundDrawableTintList = ColorStateList.valueOf(Color.BLACK)
                        it.compoundDrawablePadding = 10
                    } else {
                        populateView(it, getItem(position), position)
                    }
                }

            override fun populateView(v: View, model: String, position: Int) {
                (v as TextView).text = model
            }
        }

        toolbar_spinner.adapter = adapter

        lockersService.selectedLockerId.observe(this,
            Observer<LockerId> { id -> updateToolbarSelection(adapter, id) })

        // onItemSelected is called automatically at start. Use `first` to check if onItemSelected is
        // called the first time, to skip any not user intended actions
        var first = true
        toolbar_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, p1: View?, position: Int, id: Long) {
                if (id == SPINNER_ID_ADD_LOCKER) {
                    LockerCreateDialogFragment().show(supportFragmentManager, "create_locker")
                } else if (!first) {
                    lockersService.selectLocker(adapter.getRef(position).key!!)
                } else {
                    first = false
                }
            }
        }
    }

    private fun navigateToLoginIfNecessary() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().setRequireName(true).build(),
                AuthUI.IdpConfig.PhoneBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().setScopes(listOf("profile")).build()
            )

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setLogo(R.drawable.ic_launcher_foreground)
                    .build(),
                RC_SIGN_IN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            // val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                createToolbarInvolvedLockerSpinner()
            }
        }
    }
}
