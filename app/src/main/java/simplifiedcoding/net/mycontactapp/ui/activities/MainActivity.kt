package simplifiedcoding.net.mycontactapp.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import simplifiedcoding.net.mycontactapp.R
import simplifiedcoding.net.mycontactapp.db.entities.Contact
import simplifiedcoding.net.mycontactapp.ui.adapters.ContactAdapter


class MainActivity : AppCompatActivity() {

    private var contacts = mutableListOf<Contact>()
    private var adapter: ContactAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(applicationContext, "You must give permission to use this app", Toast.LENGTH_LONG).show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    CODE_REQUEST_CONTACT
                )
            }
        } else {
            layoutContacts.post {
                layoutContacts.isRefreshing = true
                loadContacts()
            }
        }

        layoutContacts.setOnRefreshListener { loadContacts() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchView = menu?.findItem(R.id.action_search)?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }


    private fun filter(text: String?) {
        val filteredContacts = mutableListOf<Contact>()

        for (contact in contacts) {
            if (contact.name.toLowerCase().contains(text?.toLowerCase().toString())) {
                filteredContacts.add(contact)
            }
        }

        adapter?.filterList(filteredContacts)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CODE_REQUEST_CONTACT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    layoutContacts.post {
                        layoutContacts.isRefreshing = true
                        loadContacts()
                    }
                } else {

                }
            }
        }
    }

    private fun loadContacts() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            FetchContacts().execute()
        }else{
            layoutContacts.isRefreshing = false
            Toast.makeText(this, "Permission is needed for this app", Toast.LENGTH_LONG).show()
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", getPackageName(), null)
            intent.data = uri
            startActivity(intent)
        }
    }

    private fun displayContacts() {
        recyclerViewContacts.setHasFixedSize(true)
        recyclerViewContacts.layoutManager = LinearLayoutManager(this)
        adapter = ContactAdapter(contacts)
        recyclerViewContacts.adapter = adapter
        layoutContacts.isRefreshing = false
    }

    companion object {
        private const val CODE_REQUEST_CONTACT = 100
    }


    private inner class FetchContacts : AsyncTask<Void, Void, List<Contact>>() {

        private val DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        private val FILTER = "$DISPLAY_NAME NOT LIKE '%@%'"
        private val ORDER = String.format("%1\$s COLLATE NOCASE", DISPLAY_NAME)

        @SuppressLint("InlinedApi")
        private val PROJECTION =
            arrayOf(ContactsContract.Contacts._ID, DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER)

        override fun doInBackground(vararg params: Void): List<Contact>? {
            try {
                val cr = contentResolver
                val cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, PROJECTION, FILTER, null, ORDER)
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        // get the contact's information
                        val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        val name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME))
                        val hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                        // get the user's email address
                        var email: String? = null
                        val ce = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", arrayOf(id), null
                        )
                        if (ce != null && ce.moveToFirst()) {
                            email = ce.getString(ce.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))
                            ce.close()
                        }

                        // get the user's phone number
                        var phone: String? = null
                        if (hasPhone > 0) {
                            val cp = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(id), null
                            )
                            if (cp != null && cp.moveToFirst()) {
                                phone = cp.getString(cp.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                cp.close()
                            }
                        }


                        val contactUri =
                            ContentUris.withAppendedId(
                                ContactsContract.Contacts.CONTENT_URI,
                                java.lang.Long.parseLong(id)
                            )
                        val photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO)
                        val photo = photoUri?.toString()
                        contacts.add(
                            Contact(name, photo ?: "", phone ?: "", email ?: "")
                        )



                    } while (cursor.moveToNext())

                    // clean up cursor
                    cursor.close()
                }
                return contacts
            } catch (ex: Exception) {
                return null
            }

        }

        override fun onPostExecute(contacts: List<Contact>?) {
            contacts?.let{
                this@MainActivity.contacts = it as MutableList<Contact>
                displayContacts()
            }
        }
    }
}
