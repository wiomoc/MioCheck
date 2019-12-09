package de.wiomoc.miocheck

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import android.graphics.BitmapFactory
import android.widget.EditText
import de.wiomoc.miocheck.services.LockersService
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream


class LockerCreateDialogFragment : DialogFragment() {
    private val RC_IMAGE = 2
    lateinit var imageView: ImageView
    var image: Bitmap? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_locker_create, null)

        imageView = view.findViewById(R.id.locker_create_image)
        imageView.setOnClickListener {
            startActivityForResult(Intent.createChooser(Intent().apply {
                type = "image/*"
                action = Intent.ACTION_PICK
            }, ""), RC_IMAGE)
        }

        return AlertDialog.Builder(context!!)
            .setView(view)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val compressedImage = image?.let {
                    ByteArrayOutputStream().apply {
                        Bitmap.createScaledBitmap(it, 400, 400, false)
                            .compress(Bitmap.CompressFormat.WEBP, 35, this)
                    }.toByteArray()
                }

                val name = view.findViewById<EditText>(R.id.locker_create_name_txt).text.toString()
                val pin = view.findViewById<EditText>(R.id.locker_create_code_txt).text.toString()

                val lockersService by inject<LockersService>()
                lockersService.createLocker(name, pin, compressedImage).addOnCompleteListener {
                    dialogInterface.dismiss()
                }
            }
            .create()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RC_IMAGE -> {
                data?.data?.let {
                    image = BitmapFactory.decodeStream(context!!.contentResolver.openInputStream(it))
                    imageView.setImageBitmap(image)
                }
            }
        }
    }
}
