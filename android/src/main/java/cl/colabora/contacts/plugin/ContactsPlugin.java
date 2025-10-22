package cl.colabora.contacts.plugin;

import com.getcapacitor.Logger;

public class ContactsPlugin {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
