package com.example.sudokugo.ui.composables.profilePic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue

@Composable
fun UserChangePicture() {
    val userPictureViewModel = koinViewModel<UserPictureViewModel>()
    val userPic by userPictureViewModel.userPic.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    val cameraLauncher = rememberCameraLauncher(
        onPictureTaken = { imageUri ->
            userPictureViewModel.processAndSaveUserPic(imageUri, ctx.contentResolver)
        }
    )
    val imgModifier = Modifier
        .size(120.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        PictureOrDefault(userPic, imgModifier)

        IconButton(
            onClick = cameraLauncher::captureImage,
            modifier = Modifier
                .offset(x = 40.dp)
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = "Change image",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}