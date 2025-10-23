package cl.colabora.contacts.plugin

import android.content.ContentResolver
import android.provider.ContactsContract
import com.getcapacitor.Logger

class ContactsPlugin(private val contentResolver: ContentResolver) {
    val contacts: MutableList<MutableMap<String?, String?>?>
        get() {
            val contacts: MutableList<MutableMap<String?, String?>?> =
                ArrayList<MutableMap<String?, String?>?>()
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf<String>(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val contact: MutableMap<String?, String?> =
                        HashMap<String?, String?>()
                    contact.put(
                        "name", cursor.getString(
                            cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        )
                    )
                    contact.put(
                        "number", cursor.getString(
                            cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        )
                    )
                    contacts.add(contact)
                }
                cursor.close()
            }
            return contacts
        }

    fun echo(value: String?): String? {
        Logger.info("Echo", value)
        return value
    }
}
