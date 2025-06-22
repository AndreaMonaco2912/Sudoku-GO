package com.example.sudokugo.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.sudokugo.ui.composables.Loading
import com.example.sudokugo.ui.composables.TopSudokuGoAppBar
import com.example.sudokugo.ui.composables.profilePic.UserChangePicture

@Composable
fun UserScreen(navController: NavController, userScreenViewModel: UserScreenViewModel) {

    val email by userScreenViewModel.email.collectAsStateWithLifecycle()
    val userData by userScreenViewModel.userData.collectAsStateWithLifecycle()
    val topUsers by userScreenViewModel.topUsers.collectAsStateWithLifecycle()

    LaunchedEffect(email) {
        if (email != null && userData == null) {
            userScreenViewModel.refreshUserData()
        } else if (email == null) {
            navController.navigate(SudokuGORoute.Login) {
                popUpTo(SudokuGORoute.User) { inclusive = true }
            }
        }
    }

    LaunchedEffect(userData) {
        if (userData != null) {
            userScreenViewModel.getTopUsers()
        }
    }

    if (email == null) {
        Loading("Loading user data")
        return
    }

    Scaffold(topBar = { TopSudokuGoAppBar(navController, title = userData?.username ?: "") },
        bottomBar = {
            BottomSudokuGoAppBar(
                navController, selected = BottomNavSelected.USER
            )
        }) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(contentPadding), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(Modifier.size(8.dp))

            UserChangePicture()

            Column {
                Text(
                    "Leaderboard",
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize

                )
                Column(modifier = Modifier.padding(8.dp)) {
                    topUsers?.map { Pair(it.username, it.points) }?.forEachIndexed { i, it ->
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
                            Text(
                                "   ${i + 1}^",
                                textAlign = TextAlign.End,
                                color = if (it.first == userData?.username) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )


                        }
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    }

                    if (userData?.let {
                            topUsers?.any { user -> user.username == it.username }
                        } == false) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                userData!!.username,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                userData!!.points.toString(),
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                            Text(
                                "   You",
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}

