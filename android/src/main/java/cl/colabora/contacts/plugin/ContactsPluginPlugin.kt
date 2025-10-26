package cl.colabora.contacts.plugin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import cl.colabora.contacts.plugin.composescreens.ComposeActivity
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PermissionState
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.Exception


@CapacitorPlugin(
    name = "ContactsPlugin",
    permissions = [Permission(strings = [Manifest.permission.READ_CONTACTS], alias = "contacts")]
)
class ContactsPluginPlugin : Plugin() {
//    private var implementation: ContactsPlugin? = null
//
//    override fun load() {
//        implementation = ContactsPlugin(getContext().getContentResolver())
//    }

    @PluginMethod
    fun openNativeView(call:PluginCall){
        val context = bridge.activity

        val username= call.getString("username")?: "Invitado"
        val intent = Intent(context, ComposeActivity::class.java)
        intent.putExtra("username",username)
        context.startActivity(intent)
        call.resolve()
    }

    @PluginMethod
    fun getContacts(call: PluginCall) {
        if (getPermissionState("contacts") != PermissionState.GRANTED) {
            requestPermissionForAlias("contacts", call, "contactsPermsCallback")
            return
        }
        fetchContactsAsync(call)
    }

    @PermissionCallback
    private fun contactsPermsCallback(call: PluginCall) {
        if (getPermissionState("contacts") == PermissionState.GRANTED) {
            fetchContactsAsync(call)
        } else {
            call.reject(("Permiso de contactos denegado"))
        }
    }
    private fun fetchContactsAsync(call:PluginCall){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val contactsArray = JSArray()
                val contacts = fetchContacts(context)
                contacts.forEach{contact->
                    val contactObject = JSObject().apply {
                        put("id",contact.id)
                        put("name",contact.name)
                        put("phones", JSArray(contact.phoneNumbers))
                    }

                    contactsArray.put(contactObject)
                }
                withContext(Dispatchers.Main) {
                    val result = JSObject().apply {
                        put("contacts",contactsArray)
                    }
                    call.resolve(result)
                }
            }catch (e: Exception){
                withContext(Dispatchers.Main) {
                    call.reject("Error obteniendo contactos desde le plugin: ${e.message}")
                }
            }
        }
    }

    data class Contact(
        val id: String,
        val name: String,
        val phoneNumbers: List<String>
    )

    suspend fun fetchContacts(context: Context): List<Contact> = withContext(Dispatchers.IO) {

        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
            ),
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME+ " ASC"
        )
        cursor?.use { c ->
            // Transformamos cada fila del cursor en un Contact
            generateSequence {
                if (c.moveToNext()) {
                    val contactId = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val contactName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)) ?: "Unknown"

                    // Obtenemos los teléfonos de este contacto
                    val phoneNumbers = context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )?.use { pc ->
                        generateSequence {
                            if (pc.moveToNext()) {
                                pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            } else null
                        }.toList()
                    } ?: emptyList()

                    Contact(contactId, contactName, phoneNumbers)
                } else null
            }.toList()
        } ?: emptyList() // Retornamos lista vacía si cursor es null
    }

//    suspend fun fetchContacts(context: Context): List<Contact> = withContext(Dispatchers.IO) {
//        val contacts = mutableListOf<Contact>()
//
//        val cursor = context.contentResolver.query(
//            ContactsContract.Contacts.CONTENT_URI,
//            arrayOf(
//                ContactsContract.Contacts._ID,
//                ContactsContract.Contacts.DISPLAY_NAME
//            ),
//            null,
//            null,
//            ContactsContract.Contacts.DISPLAY_NAME+ " ASC"
//        )
//        cursor?.use { c->
//            val idIndex=c.getColumnIndex(ContactsContract.Contacts._ID)
//            val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
//
//            while (c.moveToNext()){
//                val contactId = c.getString(idIndex)
//                val contactName = c.getString(nameIndex)?: "Uknown"
//
//                val phoneNumbers = mutableListOf<String>()
//                val phonesCursor = context.contentResolver.query(
//                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
//                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
//                    arrayOf(contactId),
//                        null
//                    )
//
//                    phonesCursor?.use{ pc->
//                        val numberIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
//                        while (pc.moveToNext()){
//                            pc.getString(numberIndex)?.let{phoneNumbers.add(it)}
//                        }
//                    }
//                    contacts.add(Contact(contactId,contactName,phoneNumbers))
//                )
//            }
//            contacts.toList()
//        }
//    }

//    private fun fetchContacts(call: PluginCall) {
//        try {
//            var contactsList: MutableList<MutableMap<String?, String?>> =
//                implementation!!.contacts.filterNotNull().toMutableList()
//            if (contactsList == null) contactsList = ArrayList<MutableMap<String?, String?>>()
//
//            val jsContacts = JSArray()
//            for (c in contactsList) {
//                val jsContact = JSObject()
//                jsContact.put("name", c.get("name"))
//                jsContact.put("number", c.get("number"))
//                jsContacts.put(jsContact)
//            }
//            val ret = JSObject()
//            ret.put("contacts", jsContacts)
//            call.resolve(ret)
//        } catch (e: Exception) {
//            call.reject("Error al obtener contactos: " + e.message)
//        }
//    }
}
