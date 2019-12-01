package de.wiomoc.miocheck

import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.snackbar.Snackbar
import java.lang.Exception

interface NetworkErrorSnackbarMixin : OnFailureListener {
    override fun onFailure(p0: Exception) {
        val fragment = (this as Fragment)
        Snackbar.make(fragment.view!!, R.string.network_failure_snackbar_message, Snackbar.LENGTH_LONG).show()
    }
}
