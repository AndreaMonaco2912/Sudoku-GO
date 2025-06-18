package com.example.sudokugo.ui.composables.profilePic

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.SystemClock
import android.provider.MediaStore
import java.io.FileNotFoundException
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import androidx.core.graphics.createBitmap
import com.example.sudokugo.data.repositories.UserDAORepository
import com.example.sudokugo.data.repositories.UserDSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class UserPictureViewModel(
    private val userDaoRep: UserDAORepository,
    private val userDSRepository: UserDSRepository
) : ViewModel() {

    private val _userPic = MutableStateFlow<String?>(null)
    val userPic: StateFlow<String?> = _userPic

    private val _email = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            userDSRepository.email.collect { savedEmail ->
                _email.value = savedEmail
                if(savedEmail != null){
                    _userPic.value = userDaoRep.getPictureByEmail(savedEmail)
                }
            }
        }
    }

    fun processAndSaveUserPic(imageUri: Uri, contentResolver: ContentResolver) =
        viewModelScope.launch {
            val bitmap = uriToBitmap(imageUri, contentResolver)
            val cropped = cropCircleBitmap(bitmap)
            val savedUri = saveBitmapToStorage(cropped, contentResolver)
            userDaoRep.changePic(_email.value!!, savedUri.toString())
            _userPic.value = savedUri.toString()
        }

    private fun uriToBitmap(imageUri: Uri, contentResolver: ContentResolver): Bitmap {
        val source = ImageDecoder.createSource(contentResolver, imageUri)
        return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun cropCircleBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = createBitmap(size, size)

        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, null, rect, paint)

        return output
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
