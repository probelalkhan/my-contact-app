package simplifiedcoding.net.mycontactapp.ui.activities

import android.Manifest
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.include_contact_detail.*
import simplifiedcoding.net.mycontactapp.R
import simplifiedcoding.net.mycontactapp.db.entities.Contact
import simplifiedcoding.net.mycontactapp.ui.adapters.KEY_CONTACT
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class ContactActivity : AppCompatActivity() {

    private var contact: Contact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        contact = intent.getSerializableExtra(KEY_CONTACT) as Contact
        Log.d("key_contact", contact.toString())

        supportActionBar?.title = contact?.name
        textViewPhone.text = contact?.number
        textViewEmail.text = contact?.email
        imageView.setImageURI(Uri.parse(contact?.photoUri))

        if (contact!!.number.isEmpty()) {
            buttonCall.isEnabled = false
            buttonSms.isEnabled = false
        }

        buttonCall.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {

                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        CODE_REQUEST_PHONE
                    )
                }
            } else {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + contact?.number))
                startActivity(intent)
            }
        }

        buttonSms.setOnClickListener {
            val uri = Uri.parse("smsto:${contact?.number}")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CODE_REQUEST_PHONE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(applicationContext, "You must allow phone permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        private const val CODE_REQUEST_PHONE = 101
    }
}
