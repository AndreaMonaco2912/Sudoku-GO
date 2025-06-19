package com.example.sudokugo.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sudokugo.data.models.Theme
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import com.example.sudokugo.ui.screens.login.LoginViewModel

@Composable
fun SettingsScreen(navController: NavController, userId: String, state: SettingsState, onThemeSelected: (Theme) -> Unit, onLogout: () -> Unit) {
    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "Settings") },
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.NONE) }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(contentPadding).padding(12.dp).fillMaxSize()
        ) {
            var username by rememberSaveable { mutableStateOf(userId) }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(36.dp))
            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge
            )

            var showDialog by remember { mutableStateOf(false) }
            var selected by remember { mutableStateOf(state.theme) }
            val options = Theme.entries

            Button(onClick = { showDialog = true }) {
                Text("Change theme")
            }

            Button(onClick = {
                onLogout()
                navController.navigate(SudokuGORoute.Home)
            }) {
                Text("Logout")
            }

            if (showDialog) {
                RadioOptionsDialog(
                    title = "Choose theme",
                    options = options,
                    selectedOption = selected,
                    onOptionSelected = {
                        selected = it
                        showDialog = false
                        onThemeSelected(it)
                    },
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}

@Composable
fun RadioOptionsDialog(
    title: String,
    options: List<Theme>,
    selectedOption: Theme,
    onOptionSelected: (Theme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onOptionSelected(option) }
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = { onOptionSelected(option) }
                        )
                        Text(text = option.toString())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

