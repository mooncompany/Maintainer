package com.maintainer.app.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONArray
import org.json.JSONException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoManager @Inject constructor(
    private val context: Context
) {

    private val photosDir by lazy {
        File(context.filesDir, "vehicle_photos").apply {
            if (!exists()) mkdirs()
        }
    }

    /**
     * Saves a bitmap as a JPEG file and returns the file path
     */
    fun savePhoto(bitmap: Bitmap, vehicleId: String): Result<String> {
        return try {
            val fileName = "${vehicleId}_${UUID.randomUUID()}.jpg"
            val file = File(photosDir, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            Result.success(file.absolutePath)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a photo file from storage
     */
    fun deletePhoto(photoPath: String): Boolean {
        return try {
            val file = File(photoPath)
            if (file.exists() && file.parentFile == photosDir) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Deletes all photos for a specific vehicle
     */
    fun deleteAllPhotosForVehicle(vehicleId: String): Int {
        var deletedCount = 0
        photosDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("${vehicleId}_")) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        }
        return deletedCount
    }

    /**
     * Validates that a photo file exists and is readable
     */
    fun isPhotoValid(photoPath: String): Boolean {
        return try {
            val file = File(photoPath)
            file.exists() && file.canRead() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Loads a bitmap from a photo path with optional size constraints
     */
    fun loadPhoto(photoPath: String, maxWidth: Int? = null, maxHeight: Int? = null): Bitmap? {
        return try {
            if (!isPhotoValid(photoPath)) return null

            if (maxWidth != null && maxHeight != null) {
                // Load with size constraints for memory efficiency
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(photoPath, options)

                options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
                options.inJustDecodeBounds = false

                BitmapFactory.decodeFile(photoPath, options)
            } else {
                BitmapFactory.decodeFile(photoPath)
            }
        } catch (e: OutOfMemoryError) {
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculates the optimal sample size for bitmap loading
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Gets the storage directory for vehicle photos
     */
    fun getPhotosDirectory(): File = photosDir

    /**
     * Gets the total size of all photo files in bytes
     */
    fun getTotalPhotoStorageSize(): Long {
        var totalSize = 0L
        photosDir.listFiles()?.forEach { file ->
            totalSize += file.length()
        }
        return totalSize
    }

    /**
     * Cleans up orphaned photo files (photos not referenced by any vehicle)
     */
    fun cleanupOrphanedPhotos(allVehiclePhotos: List<String>): Int {
        var deletedCount = 0
        val referencedFiles = allVehiclePhotos.map { File(it).name }.toSet()

        photosDir.listFiles()?.forEach { file ->
            if (file.name !in referencedFiles) {
                if (file.delete()) {
                    deletedCount++
                }
            }
        }

        return deletedCount
    }
}

/**
 * Utility class for managing vehicle photo arrays stored as JSON
 */
object VehiclePhotoUtils {

    /**
     * Converts a list of photo paths to JSON string
     */
    fun photosToJson(photos: List<String>): String {
        return if (photos.isEmpty()) {
            ""
        } else {
            try {
                val jsonArray = JSONArray()
                photos.forEach { jsonArray.put(it) }
                jsonArray.toString()
            } catch (e: JSONException) {
                ""
            }
        }
    }

    /**
     * Converts JSON string to list of photo paths
     */
    fun photosFromJson(json: String?): List<String> {
        return if (json.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                val jsonArray = JSONArray(json)
                val photos = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    photos.add(jsonArray.getString(i))
                }
                photos
            } catch (e: JSONException) {
                emptyList()
            }
        }
    }

    /**
     * Adds a photo to the vehicle's photo list
     */
    fun addPhoto(currentPhotosJson: String?, newPhotoPath: String): String {
        val currentPhotos = photosFromJson(currentPhotosJson).toMutableList()
        currentPhotos.add(newPhotoPath)
        return photosToJson(currentPhotos)
    }

    /**
     * Removes a photo from the vehicle's photo list
     */
    fun removePhoto(currentPhotosJson: String?, photoPathToRemove: String): String {
        val currentPhotos = photosFromJson(currentPhotosJson).toMutableList()
        currentPhotos.remove(photoPathToRemove)
        return photosToJson(currentPhotos)
    }

    /**
     * Sets the main profile photo index, ensuring it's within valid range
     */
    fun setMainPhotoIndex(photosJson: String?, newIndex: Int): Int {
        val photos = photosFromJson(photosJson)
        return if (photos.isEmpty()) {
            0
        } else {
            newIndex.coerceIn(0, photos.size - 1)
        }
    }

    /**
     * Gets the main profile photo path
     */
    fun getMainPhotoPath(photosJson: String?, mainPhotoIndex: Int): String? {
        val photos = photosFromJson(photosJson)
        return if (photos.isEmpty() || mainPhotoIndex < 0 || mainPhotoIndex >= photos.size) {
            null
        } else {
            photos[mainPhotoIndex]
        }
    }

    /**
     * Reorders photos in the list
     */
    fun reorderPhotos(currentPhotosJson: String?, fromIndex: Int, toIndex: Int): String {
        val currentPhotos = photosFromJson(currentPhotosJson).toMutableList()

        if (fromIndex < 0 || fromIndex >= currentPhotos.size ||
            toIndex < 0 || toIndex >= currentPhotos.size) {
            return currentPhotosJson ?: ""
        }

        val item = currentPhotos.removeAt(fromIndex)
        currentPhotos.add(toIndex, item)

        return photosToJson(currentPhotos)
    }
}