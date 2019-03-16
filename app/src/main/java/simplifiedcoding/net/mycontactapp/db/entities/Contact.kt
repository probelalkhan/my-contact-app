package simplifiedcoding.net.mycontactapp.db.entities

import java.io.Serializable

data class Contact(
    val name: String,
    val photoUri: String,
    val number: String,
    val email: String?
):Serializable