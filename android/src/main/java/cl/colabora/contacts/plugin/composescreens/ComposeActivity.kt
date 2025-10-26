@file:OptIn(ExperimentalMaterial3Api::class)

package cl.colabora.contacts.plugin.composescreens

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import cl.colabora.contacts.plugin.Contact
import androidx.compose.foundation.lazy.items


class ComposeActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       val contacts:ArrayList<Contact>? =

           if(SDK_INT >= Build.VERSION_CODES.TIRAMISU){

        intent.getParcelableArrayListExtra<Contact>("contacts",Contact::class.java)
       }else{
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra<Contact>("contacts") as? ArrayList<Contact>
        }
        setContent {
            MaterialTheme{
                MyComposeHomeScreen(contacts = contacts?: emptyList())
            }
        }

    }
}

@Composable
fun MyComposeHomeScreen(contacts: List<Contact>) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Contactos nativos") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(contacts) { contact ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                    Text(text = contact.name, style = MaterialTheme.typography.titleMedium)
                    contact.phoneNumbers.forEach { phone ->
                        Text(text = phone, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Preview
@Composable
private fun MyComposeHomeScreenPreview() {
    MyComposeHomeScreen(contacts =emptyList())
}