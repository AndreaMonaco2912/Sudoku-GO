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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sudokugo.data.models.Theme
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.mutableFloatStateOf

@Composable
fun SettingsScreen(
    navController: NavController,
    state: SettingsState,
    onThemeSelected: (Theme) -> Unit,
    onLogout: () -> Unit,
    setVolume: (Float) -> Unit
) {
    val settingsViwModel = koinViewModel<SettingsViewModel>()
    val email by settingsViwModel.email.collectAsStateWithLifecycle()
    val userData by settingsViwModel.userData.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "Settings") },
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.NONE) }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(contentPadding)
                .padding(12.dp)
                .fillMaxSize()
        ) {

            var showDialog by remember { mutableStateOf(false) }
            var selected by remember { mutableStateOf(state.theme) }
            val options = Theme.entries

            Column {
                if (email != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Name: ${userData?.name ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Username: ${userData?.username ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Email: ${userData?.email ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            "Points: ${userData?.points ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLogout()
                                navController.navigate(SudokuGORoute.Home) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            "Logout",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLogout()
                                navController.navigate(SudokuGORoute.Login)
                            }
                            .padding(16.dp)
                    ) {
                        Text(
                            "Login",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = Bold
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDialog = true }
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Change theme",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = Bold
                    )
                }
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

            Spacer(modifier = Modifier.size(24.dp))

            val volume by settingsViwModel.volume.collectAsStateWithLifecycle()

            Text("Volume", style = MaterialTheme.typography.bodyLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = if (volume == 0f) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )

                Slider(
                    value = volume,
                    onValueChange = {
                        setVolume(volume)
                        settingsViwModel.changeVolume(it)
                    },
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
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

