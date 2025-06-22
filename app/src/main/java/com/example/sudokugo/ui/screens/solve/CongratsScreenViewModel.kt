package com.example.sudokugo.ui.screens.solve

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudokugo.data.repositories.SudokuRepository
import com.example.sudokugo.data.repositories.UserDSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class CongratsScreenViewModel(
    private val sudokuRepository: SudokuRepository,
    private val repositoryUser: UserDSRepository
) : ViewModel() {

    private val _email = MutableStateFlow<String?>(null)
    val email: MutableStateFlow<String?> = _email

    init {
        viewModelScope.launch {
            repositoryUser.email.collect { savedEmail ->
                _email.value = savedEmail
            }
        }
    }

    fun processAndSaveUserPic(imageUri: Uri, contentResolver: ContentResolver, sudokuId: Long) =
        viewModelScope.launch {
            val bitmap = uriToBitmap(imageUri, contentResolver)
            val cropped = cropSquareBitmap(bitmap)
            val savedUri = saveBitmapToStorage(cropped, contentResolver)
            sudokuRepository.changePic(sudokuId, savedUri.toString())
        }

    private fun uriToBitmap(imageUri: Uri, contentResolver: ContentResolver): Bitmap {
        val source = ImageDecoder.createSource(contentResolver, imageUri)
        return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun cropSquareBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)

        // Calcola gli offset per centrare il crop
        val xOffset = (bitmap.width - size) / 2
        val yOffset = (bitmap.height - size) / 2

        return Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
    }

    private fun saveBitmapToStorage(
        bitmap: Bitmap,
        contentResolver: ContentResolver,
        name: String = "IMG_${SystemClock.uptimeMillis()}"
    ): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
        }

        val savedImageUri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        ) ?: throw FileNotFoundException()

        val outputStream = contentResolver.openOutputStream(savedImageUri)
            ?: throw FileNotFoundException()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        return savedImageUri
    }
}