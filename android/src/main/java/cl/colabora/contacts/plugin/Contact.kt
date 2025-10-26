package cl.colabora.contacts.plugin

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val id: String,
    val name: String,
    val phoneNumbers: List<String>
) : Parcelable