package cl.colabora.contacts.plugin;

import android.Manifest;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.util.List;
import java.util.Map;

@CapacitorPlugin(name = "ContactsPlugin",
        permissions = {
                @Permission(strings = {Manifest.permission.READ_CONTACTS}, alias = "contacts")
        })
public class ContactsPluginPlugin extends Plugin {

    private ContactsPlugin implementation;

    @Override
    public void load() {
        implementation = new ContactsPlugin(getContext().getContentResolver());
    }

    @PluginMethod
    public void getContacts(PluginCall call) {
        if (getPermissionState("contacts") != PermissionState.GRANTED) {
            requestPermissionForAlias("contacts", call, "contactsPermsCallback");
            return;
        }

        fetchContacts(call);
    }

    @PermissionCallback
    private void contactsPermsCallback(PluginCall call) {
        if (getPermissionState("contacts") == PermissionState.GRANTED) {
            fetchContacts(call);
        } else {
            call.reject(("Permiso de contactos denegado"));
        }
    }

    private void fetchContacts(PluginCall call) {
        try {
            List<Map<String, String>> contactsList = implementation.getContacts();
            if (contactsList == null) contactsList = new java.util.ArrayList<>();

            JSArray jsContacts = new JSArray();
            for (Map<String, String> c : contactsList) {
                JSObject jsContact = new JSObject();
                jsContact.put("name", c.get("name"));
                jsContact.put("number", c.get("number"));
                jsContacts.put(jsContact);
            }
            JSObject ret = new JSObject();
            ret.put("contacts", jsContacts);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Error al obtener contactos: " + e.getMessage());
        }
    }
}
