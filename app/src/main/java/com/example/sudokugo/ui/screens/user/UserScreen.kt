package com.example.sudokugo.ui.screens.user

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.sudokugo.ui.SudokuGORoute
import com.example.sudokugo.ui.composables.BottomNavSelected
import com.example.sudokugo.ui.composables.BottomSudokuGoAppBar
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import com.example.sudokugo.ui.composables.profilePic.UserChangePicture

@Composable
fun UserScreen(navController: NavController, userScreenViewModel: UserScreenViewModel) {

    val email by userScreenViewModel.email.collectAsStateWithLifecycle()
    val points by userScreenViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(email) {
        if (email == null) {
            navController.navigate(SudokuGORoute.Login) {
                popUpTo(SudokuGORoute.User) { inclusive = true }
            }
        }
    }

    if (email == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = { TopSudokuGoAppBar(navController, title = "QueryPerUsername") },
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.USER) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(Modifier.size(8.dp))

            UserChangePicture()
//
//            // Stats Card
//            Card(
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.surfaceVariant
//                ),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text("Statistiche varie")
//                    Text("Sudoku risolti")
//                    Text("Miglior tempo")
//                    Text("Sudoku creati")
//                    Text("Punteggio")
//                    Text("Rank globale")
//                }
//            }
//
//            // Trophies Section
//            Column {
//                Text("Trofei", modifier = Modifier.background(Color(0xFFFDF6FF)).padding(8.dp))
//                Row(
//                    modifier = Modifier.padding(start = 8.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    repeat(3) {
//                        Icon(
//                            imageVector = Icons.Default.Star,
//                            contentDescription = null,
//                            tint = if (it == 0) Color.Black else Color.Black.copy(alpha = 0.3f),
//                            modifier = Modifier.size(32.dp)
//                        )
//                    }
//                }
//            }

            // Ranking Section
            Column {
                Text("Classifica", modifier = Modifier
                    .background(Color(0xFFFDF6FF))
                    .padding(8.dp))

                val rankings = listOf(
                    "1 Classificato",
                    "2 Classificato",
                    "3 Classificato",
                    "4 Classificato",
                    "QueryPerNome"
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        rankings.forEach {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFE3D3FD), shape = CircleShape)
                                ) {
                                    Text("A", color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(it, modifier = Modifier.weight(1f))
                                if (it != "Tu") Text("Punti")
                            }
                        }
                    }
                }
            }
        }
    }
}
