package com.example.sudokugo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import androidx.navigation.compose.rememberNavController
import com.example.sudokugo.data.models.Theme
import com.example.sudokugo.ui.SudokuGONavGraph
import com.example.sudokugo.ui.screens.settings.SettingsViewModel
import com.example.sudokugo.ui.theme.SudokuGOTheme

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

val supabase: SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://mkncxrzzbmjlrmmwmwkj.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1rbmN4cnp6Ym1qbHJtbXdtd2tqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDcwMTI2NDksImV4cCI6MjA2MjU4ODY0OX0.ZtetcerEvRH9yiyA523c31J57Vi5ySNxEjRMcWNQIwQ"
) {
    install(Postgrest)
}

//@Serializable
//data class Users(
//    val email: String,
//    val name: String,
//    val username: String,
//    val password: String
//)
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            SudokuGOTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    UsersList()
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun UsersList() {
//    var users by remember { mutableStateOf<List<Users>>(listOf()) }
//    LaunchedEffect(Unit) {
//        val data = supabase.from("users")
//            .select()
//            .decodeList<Users>()
//        users = data
//        }
//
//    Column {
//        LazyColumn {
//            items(users) { instrument ->
//                Text(text = instrument.email, modifier = Modifier.padding(16.dp))
//            }
//        }
//    }
//}
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Configuration.getInstance().userAgentValue = packageName

        requestPermissionsIfNecessary(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        ))

        setContent {
            val settingsViewModel = koinViewModel<SettingsViewModel>()
            val themeState by settingsViewModel.state.collectAsStateWithLifecycle()

            SudokuGOTheme(
                darkTheme = when (themeState.theme){
                    Theme.Dark -> true
                    Theme.Light -> false
                    Theme.System -> isSystemInDarkTheme()
                }
            ) {
                val navController = rememberNavController()
                SudokuGONavGraph(navController, settingsViewModel, themeState)
            }
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, toRequest.toTypedArray(), 1
            )
        }
    }
}
