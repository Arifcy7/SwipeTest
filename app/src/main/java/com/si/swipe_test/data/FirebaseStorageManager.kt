package com.si.swipe_test.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageManager {

    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadImage(imageUri: Uri): String {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${UUID.randomUUID()}")
        val uploadTask = imageRef.putFile(imageUri)

        val downloadUrl = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.await()

        return downloadUrl.toString()
    }
}
