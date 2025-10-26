@file:OptIn(ExperimentalMaterial3Api::class)

package cl.colabora.contacts.plugin.composescreens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.util.Log

class ComposeActivity: ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("username")?: "Desconocido"
        Log.d("username: ", "${intent.getStringExtra("username")}")
        Log.d("username: ", "$intent")
        Log.d("username: ", username)
        setContent {
            MaterialTheme{
                MyComposeHomeScreen(username=username)
            }
        }

    }
}

@Composable
fun MyComposeHomeScreen(username: String, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Home nativo compose")}
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Column { Text("Hola $username desde compose <3")
                Button(onClick = {}){
                    Text("Click the buttom!")
                }
            }
        }
    }
}
@Preview
@Composable
private fun MyComposeHomeScreenPreview() {
    MyComposeHomeScreen(username = "John Doe")
}