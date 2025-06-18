package com.example.sudokugo.ui.composables.profilePic

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.sudokugo.R

@Composable
fun UserPicture(userPic: String?, modifier: Modifier) {
    val ctx = LocalContext.current

    if (!userPic.isNullOrEmpty()) {
        AsyncImage(
            ImageRequest.Builder(ctx)
                .data(userPic)
                .crossfade(true)
                .build(),
            "Captured image",
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.default_character_icon), // Usa l'avatar utente reale
            contentDescription = "User Profile Image",
            modifier = modifier
        )
    }
}