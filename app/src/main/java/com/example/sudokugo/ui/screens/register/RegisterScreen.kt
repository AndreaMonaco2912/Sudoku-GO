package com.example.sudokugo.ui.screens.register

import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sudokugo.R
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
@Composable
fun RegisterScreen(navController: NavController,
                   viewModel: RegisterViewModel){
    Scaffold(
        topBar = { TopSudokuGoAppBar(navController,title = "Register") }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {
            val context = LocalContext.current
            val errorMessage by viewModel.errorMessage.collectAsState()
            val success by viewModel.registrationSuccess.collectAsState()

            LaunchedEffect(success) {
                if (success) {
                    Toast.makeText(context, "Registrazione eseguita!", Toast.LENGTH_SHORT).show()
                    navController.navigate(SudokuGORoute.Login) {
                        popUpTo(SudokuGORoute.Register) { inclusive = true }
                    }
                    viewModel.clearSuccess()
                }
            }
            LaunchedEffect(errorMessage) {
                errorMessage?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }

            var password by rememberSaveable { mutableStateOf("") }
            var passwordVisibility by remember { mutableStateOf(false)}
            var email by rememberSaveable { mutableStateOf("") }
            var name by rememberSaveable { mutableStateOf("") }
            var username by rememberSaveable { mutableStateOf("") }

            val icon = if(passwordVisibility)
                painterResource(id = R.drawable.visibility)
            else
                painterResource(id = R.drawable.visibility_off)

            OutlinedTextField(
                value = name,
                onValueChange = { name=it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username=it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email=it },
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
                    onClick = { navController.navigate(SudokuGORoute.Login){
                        popUpTo(SudokuGORoute.Register) { inclusive = true }
                        popUpTo(SudokuGORoute.Login) { inclusive = true }}
                    },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Cancel")
                }
                Spacer(Modifier.size(24.dp))
                Button(
                    onClick = { viewModel.registerUser(email, name, username, password)},
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Register")
                }
            }

        }
    }
}

