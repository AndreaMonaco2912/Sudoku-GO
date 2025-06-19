package com.example.sudokugo.ui.composables.profilePic

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.sudokugo.R

@Composable
fun PictureOrDefault(takenPic: String?, modifier: Modifier, defaultPic: Int = R.drawable.default_character_icon) {
    val ctx = LocalContext.current

    if (!takenPic.isNullOrEmpty()) {
        AsyncImage(
            ImageRequest.Builder(ctx)
                .data(takenPic)
                .crossfade(true)
                .build(),
            "Captured image",
            modifier = modifier.clip(CircleShape)
        )
    } else {
        Image(
            painter = painterResource(id = defaultPic),
            contentDescription = "Default Image",
            modifier = modifier
        )
    }
}