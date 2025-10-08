package com.example.butterflydetector.ui.photoselection

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.butterflydetector.data.PhotoDatabase
import com.example.butterflydetector.data.PhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class PhotoSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val photoDao = PhotoDatabase.getDatabase(application).photoDao()

    private val _selectedPhotos = MutableLiveData<Set<Int>>().apply {
        value = emptySet()
    }
    val selectedPhotos: LiveData<Set<Int>> = _selectedPhotos

    private val _isProcessing = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _processingMessage = MutableLiveData<String>().apply {
        value = ""
    }
    val processingMessage: LiveData<String> = _processingMessage

    fun togglePhotoSelection(position: Int) {
        val currentSelection = _selectedPhotos.value ?: emptySet()
        val newSelection = if (currentSelection.contains(position)) {
            currentSelection - position
        } else {
            currentSelection + position
        }
        _selectedPhotos.value = newSelection
        Log.d("PhotoSelectionViewModel", "[v0] Photo selection toggled. Position: $position, Selected: ${newSelection.contains(position)}")
    }

    fun clearSelection() {
        _selectedPhotos.value = emptySet()
        Log.d("PhotoSelectionViewModel", "[v0] Selection cleared")
    }

    fun sendSelectedPhotosToDatabase(allPhotos: List<Bitmap>) {
        val selectedIndices = _selectedPhotos.value ?: emptySet()
        if (selectedIndices.isEmpty()) {
            _processingMessage.value = "No photos selected"
            return
        }

        _isProcessing.value = true
        Log.d("PhotoSelectionViewModel", "[v0] Starting to process ${selectedIndices.size} selected photos")

        viewModelScope.launch {
            try {
                val photoEntities = mutableListOf<PhotoEntity>()
                val currentTime = System.currentTimeMillis()

                val mockLatitude = 52.5200
                val mockLongitude = 13.4050

                withContext(Dispatchers.IO) {
                    selectedIndices.forEach { index ->
                        if (index < allPhotos.size) {
                            val bitmap = allPhotos[index]

                            val filename = "photo_${currentTime}_$index.jpg"
                            val file = File(getApplication<Application>().filesDir, filename)

                            FileOutputStream(file).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }

                            val imageHash = generateImageHash(bitmap)

                            val photoEntity = PhotoEntity(
                                imageHash = imageHash,
                                imagePath = file.absolutePath,
                                latitude = mockLatitude,
                                longitude = mockLongitude,
                                photoTakenTimestamp = currentTime - (selectedIndices.size - selectedIndices.indexOf(index)) * 500,
                                photoSentTimestamp = currentTime,
                                userName = "Buttershy"
                            )

                            photoEntities.add(photoEntity)
                        }
                    }

                    val insertedIds = photoDao.insertPhotos(photoEntities)
                    Log.d("PhotoSelectionViewModel", "[v0] Inserted ${insertedIds.size} photos into database")
                }

                _processingMessage.value = "Successfully sent ${photoEntities.size} photos to AI Identification!"
                clearSelection()

            } catch (e: Exception) {
                Log.e("PhotoSelectionViewModel", "[v0] Error processing photos", e)
                _processingMessage.value = "Error processing photos: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private fun generateImageHash(bitmap: Bitmap): String {
        val bytes = bitmap.toString().toByteArray()
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}