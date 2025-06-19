package com.example.sudokugo.ui.screens.solve

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import com.example.sudokugo.ui.SudokuGORoute

@Composable
fun CongratsScreen(
    navController: NavController,
    points: Int,
    duration: Long,
    onTakePhoto: () -> Unit
) {
    val minutes = duration / 60000
    val seconds = (duration % 60000) / 1000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉 Complimenti! 🎉", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Hai guadagnato $points punti!")
        Text("Tempo di risoluzione: ${minutes}m ${seconds}s")

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = onTakePhoto) {
                Text("📸 Scatta una foto")
            }
            Button(onClick = {
                navController.navigate(SudokuGORoute.Home) {
                    popUpTo("solve") { inclusive = true }
                }
            }) {
                Text("Continua")
            }
        }
    }
}
