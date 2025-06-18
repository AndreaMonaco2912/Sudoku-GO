package com.example.sudokugo.ui.screens.login

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sudokugo.R
import com.example.sudokugo.data.models.Theme
import com.example.sudokugo.data.models.UserServer
import com.example.sudokugo.supabase
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import com.example.sudokugo.ui.screens.settings.SettingsState
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController, state: String?, onUserSetted: (UserServer) -> Unit) {
    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "Login") }
    ) { contentPadding ->
        if(state != null) {
                    navController.navigate(SudokuGORoute.Home){
                        popUpTo(SudokuGORoute.Login) { inclusive = true }
                    }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {

            val scope = rememberCoroutineScope()

            var password by rememberSaveable { mutableStateOf("") }
            var passwordVisibility by remember { mutableStateOf(false)}
            var email by rememberSaveable { mutableStateOf("") }

            val icon = if(passwordVisibility)
                painterResource(id = R.drawable.visibility)
            else
                painterResource(id = R.drawable.visibility_off)

            OutlinedTextField(
                value = email,
                onValueChange = { email=it.trim() },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("Password") },
                trailingIcon = {
                    IconButton(onClick = {
                        passwordVisibility = !passwordVisibility
                    }) {
                        Icon(painter = icon,
                            contentDescription = "Visibility Icon")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if(passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            )
            {
                Button(
                    onClick = { navController.navigate(SudokuGORoute.Register) },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Register")
                }
                Spacer(Modifier.size(24.dp))
                Button(onClick = {
                    scope.launch {
                        try {
                            val result = supabase.from("users")
                                .select(){
                                    filter {
                                        eq("email", email)
                                        eq("password", password)
                                    }
                                }
                                .decodeList<UserServer>()

                            if (result.isNotEmpty()) {
                                // Login riuscito
                                onUserSetted(result[0])
                                navController.navigate(SudokuGORoute.Home) {
                                    popUpTo(SudokuGORoute.Login) { inclusive = true }
                                }

                            } else {
                                // Credenziali sbagliate
                                Log.d("Login", "Credenziali non valide")
                            }
                        } catch (e: Exception) {
                            Log.e("Login", "Errore durante il login", e)
                        }
                    }
                }) {
                    Text("Login")
                }
            }

        }
    }
}



