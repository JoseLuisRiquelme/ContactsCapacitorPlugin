package cl.colabora.contacts.plugin;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.getcapacitor.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsPlugin {
    private final ContentResolver contentResolver;

    public ContactsPlugin(ContentResolver contentResolver){
        this.contentResolver=contentResolver;
    }

    public List<Map<String,String>> getContacts(){
        List<Map<String,String>> contacts = new ArrayList<>();
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        },
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                );
        if(cursor!= null){
            while (cursor.moveToNext()){
                Map<String,String> contact = new HashMap<>();
                contact.put("name",cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                ));
                contact.put("number", cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                ));
                contacts.add(contact);
            }
        cursor.close();
        }
        return contacts;
    }
    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
