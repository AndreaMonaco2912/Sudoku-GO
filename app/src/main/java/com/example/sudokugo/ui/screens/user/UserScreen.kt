package com.example.sudokugo.ui.screens.user

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val userData by userScreenViewModel.userData.collectAsStateWithLifecycle()
    val topUsers by userScreenViewModel.topUsers.collectAsStateWithLifecycle()

    LaunchedEffect(email) {
        if (email == null) {
            navController.navigate(SudokuGORoute.Login) {
                popUpTo(SudokuGORoute.User) { inclusive = true }
            }
        }
    }

    LaunchedEffect(userData) {
        if (userData != null) {
            userScreenViewModel.getUserPoints(email!!)
            userScreenViewModel.getTopUsers()
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
        topBar = { TopSudokuGoAppBar(navController, title = userData?.username) },
        bottomBar = { BottomSudokuGoAppBar(navController, selected = BottomNavSelected.USER) }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                Text(
                    "Classifica", modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                )
                Column(modifier = Modifier.padding(8.dp)) {
                    topUsers?.map { Pair<String, Long>(it.username, it.points) }?.forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                it.first,
                                modifier = Modifier.weight(1f),
                                fontWeight = if (it.first == userData?.username) FontWeight.Bold
                                else FontWeight.Normal
                            )
                            Text(
                                it.second.toString(),
                                modifier = Modifier.weight(1f),
                                fontWeight = if (it.first == userData?.username) FontWeight.Bold
                                else FontWeight.Normal,
                                textAlign = TextAlign.End
                            )
                        }
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    }

                }
            }
        }
    }
}

