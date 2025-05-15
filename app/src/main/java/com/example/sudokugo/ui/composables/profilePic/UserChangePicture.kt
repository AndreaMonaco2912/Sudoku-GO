package com.example.sudokugo.ui.composables.profilePic

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.sudokugo.R
import org.koin.androidx.compose.koinViewModel
import androidx.compose.runtime.getValue
//import com.yalantis.ucrop.UCrop

@Composable
fun UserChangePicture() {
    val userPictureViewModel = koinViewModel<UserPictureViewModel>()
    val userPic by userPictureViewModel.userPic.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

//    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        val resultUri = UCrop.getOutput(result.data!!)
//        if (result.resultCode == android.app.Activity.RESULT_OK && resultUri != null) {
//            // salva l'immagine croppata
//            val savedUri = saveImageToStorage(resultUri, ctx.contentResolver)
//            userPictureViewModel.setUserPic(savedUri)
//        }
//    }

    val cameraLauncher = rememberCameraLauncher(
        onPictureTaken = { imageUri ->
            val savedUri = saveImageToStorage(imageUri, ctx.contentResolver)
            userPictureViewModel.setUserPic(savedUri)
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (!userPic.isNullOrEmpty()) {
            AsyncImage(
                ImageRequest.Builder(ctx)
                    .data(userPic)
                    .crossfade(true)
                    .build(),
                "Captured image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.default_character_icon), // Usa l'avatar utente reale
                contentDescription = "User Profile Image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        }

        IconButton(
            onClick = cameraLauncher::captureImage,
            modifier = Modifier
                .offset(x = 40.dp)
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = "Cambia immagine",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}