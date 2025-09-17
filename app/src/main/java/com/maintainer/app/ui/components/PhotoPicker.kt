package com.maintainer.app.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.maintainer.app.data.photo.VehiclePhotoUtils
import com.maintainer.app.ui.theme.*
import java.io.File

@Composable
fun PhotoPickerSection(
    currentPhotosJson: String?,
    mainPhotoIndex: Int,
    onPhotosChanged: (String, Int) -> Unit,
    onPhotoTaken: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val photos = VehiclePhotoUtils.photosFromJson(currentPhotosJson)

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { onPhotoTaken(it) }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onPhotoTaken(bitmap)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            // Header with add photo buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Photos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    // Camera button
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Take Photo",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Gallery button
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.size(40.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Choose from Gallery",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            // Photo grid or empty state
            if (photos.isEmpty()) {
                PhotoEmptyState(
                    onCameraClick = { cameraLauncher.launch(null) },
                    onGalleryClick = { galleryLauncher.launch("image/*") }
                )
            } else {
                PhotoGrid(
                    photos = photos,
                    mainPhotoIndex = mainPhotoIndex,
                    onSetMainPhoto = { index ->
                        onPhotosChanged(currentPhotosJson ?: "", index)
                    },
                    onDeletePhoto = { index ->
                        val updatedPhotos = photos.toMutableList().apply { removeAt(index) }
                        val newPhotosJson = VehiclePhotoUtils.photosToJson(updatedPhotos)
                        val newMainIndex = if (updatedPhotos.isEmpty()) {
                            0
                        } else {
                            when {
                                index < mainPhotoIndex -> mainPhotoIndex - 1
                                index == mainPhotoIndex -> 0
                                else -> mainPhotoIndex
                            }.coerceIn(0, updatedPhotos.size - 1)
                        }
                        onPhotosChanged(newPhotosJson, newMainIndex)
                    }
                )
            }
        }
    }
}

@Composable
fun PhotoEmptyState(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(Spacing.medium))

        Text(
            text = "No photos yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Add photos to showcase your vehicle",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Spacing.large))

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            OutlinedButton(
                onClick = onCameraClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text("Camera")
            }

            OutlinedButton(
                onClick = onGalleryClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text("Gallery")
            }
        }
    }
}

@Composable
fun PhotoGrid(
    photos: List<String>,
    mainPhotoIndex: Int,
    onSetMainPhoto: (Int) -> Unit,
    onDeletePhoto: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        itemsIndexed(photos) { index, photoPath ->
            PhotoGridItem(
                photoPath = photoPath,
                isMain = index == mainPhotoIndex,
                onSetMain = { onSetMainPhoto(index) },
                onDelete = { onDeletePhoto(index) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoGridItem(
    photoPath: String,
    isMain: Boolean,
    onSetMain: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.size(120.dp)
    ) {
        // Photo
        val photoFile = File(photoPath)
        if (photoFile.exists() && photoFile.canRead()) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(CornerRadius.medium)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(photoFile),
                    contentDescription = "Vehicle Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onSetMain() },
                    contentScale = ContentScale.Crop
                )
            }

            // Main photo indicator
            if (isMain) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Main Photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Delete button
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        CircleShape
                    )
                    .clickable { showDeleteDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete Photo",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Set as main button overlay
            if (!isMain) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .clickable { onSetMain() }
                        .padding(Spacing.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Set as Main",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    ConfirmationDialog(
        isVisible = showDeleteDialog,
        title = "Delete Photo",
        message = "Are you sure you want to delete this photo? This action cannot be undone.",
        confirmText = "Delete",
        cancelText = "Cancel",
        icon = Icons.Default.Delete,
        isDestructive = true,
        onConfirm = onDelete,
        onDismiss = { showDeleteDialog = false }
    )
}