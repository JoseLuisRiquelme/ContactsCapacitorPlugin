package cl.colabora.contacts.plugin

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Parcelable
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
import kotlinx.parcelize.Parcelize
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
    fun openNativeView(call:PluginCall) {
        val context = bridge.activity

        if (getPermissionState("contacts") != PermissionState.GRANTED) {
            requestPermissionForAlias("contacts", call, "openNativeViewPermsCallback")
            return
        }

        openComposeWithContacts(context, call)
    }

    @PermissionCallback
    private fun openNativeViewPermsCallback(call: PluginCall){
        if(getPermissionState("contacts")== PermissionState.GRANTED){
            val context = bridge.activity
            openComposeWithContacts(context,call)
        }else{
            call.reject("Permiso de contactos denegado")
        }
    }
    private fun openComposeWithContacts(context: Context,call: PluginCall){
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val contacts = fetchContacts(context)
                withContext(Dispatchers.Main) {
                    val intent = Intent(context, ComposeActivity::class.java).apply {
                        putParcelableArrayListExtra("contacts", ArrayList(contacts as List<Parcelable>))
                    }
                    context.startActivity(intent)
                    call.resolve()
                }
            }catch (e:kotlin.Exception){
                withContext(Dispatchers.Main) {
                    call.reject("Error al obtener contactos: ${e.message}")
                }
            }
        }
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
}