package simplifiedcoding.net.mycontactapp.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import kotlinx.android.synthetic.main.recyclerview_contacts_item.view.*
import simplifiedcoding.net.mycontactapp.R
import simplifiedcoding.net.mycontactapp.db.entities.Contact
import simplifiedcoding.net.mycontactapp.ui.activities.ContactActivity

const val KEY_CONTACT = "key_contact"

class ContactAdapter(var contacts: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.recyclerview_contacts_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount() = contacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val color = ColorGenerator.MATERIAL.getColor(contacts[position].name)
        val drawable = TextDrawable.builder()
            .buildRound(contacts[position].name.substring(0,1), color)

        holder.view.main_letter.setImageDrawable(drawable)
        holder.view.main_name.text = contacts[position].name

        holder.view.setOnClickListener {
            holder.view.context.startActivity(
                Intent(holder.view.context, ContactActivity::class.java)
                    .putExtra(KEY_CONTACT, contacts[position])
            )
        }
    }

    fun filterList(filteredContacts: MutableList<Contact>) {
        this.contacts = filteredContacts
        notifyDataSetChanged()
    }


    class ContactViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}