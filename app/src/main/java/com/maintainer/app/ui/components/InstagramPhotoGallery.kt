package com.maintainer.app.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.maintainer.app.data.photo.VehiclePhotoUtils
import com.maintainer.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

@Composable
fun InstagramPhotoGallery(
    currentPhotosJson: String?,
    mainPhotoIndex: Int,
    onPhotosChanged: (String, Int) -> Unit,
    onPhotoTaken: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photos = VehiclePhotoUtils.photosFromJson(currentPhotosJson)
    var showFullScreenGallery by remember { mutableStateOf(false) }
    var fullScreenStartIndex by remember { mutableStateOf(0) }

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
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.extraLarge),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            // Header with count and add buttons
            InstagramGalleryHeader(
                photoCount = photos.size,
                onCameraClick = { cameraLauncher.launch(null) },
                onGalleryClick = { galleryLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            // Photo content
            if (photos.isEmpty()) {
                InstagramEmptyState(
                    onCameraClick = { cameraLauncher.launch(null) },
                    onGalleryClick = { galleryLauncher.launch("image/*") }
                )
            } else {
                InstagramPhotoGrid(
                    photos = photos,
                    mainPhotoIndex = mainPhotoIndex,
                    onPhotoClick = { index ->
                        fullScreenStartIndex = index
                        showFullScreenGallery = true
                    },
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

    // Full-screen photo gallery
    if (showFullScreenGallery) {
        FullScreenPhotoGallery(
            photos = photos,
            startIndex = fullScreenStartIndex,
            mainPhotoIndex = mainPhotoIndex,
            onDismiss = { showFullScreenGallery = false },
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
                if (updatedPhotos.isEmpty()) {
                    showFullScreenGallery = false
                }
            }
        )
    }
}

@Composable
fun InstagramGalleryHeader(
    photoCount: Int,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MechanicBlue40,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(Spacing.small))

                Text(
                    text = "Photo Gallery",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SteelGray40
                )
            }

            Text(
                text = "$photoCount photo${if (photoCount != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // Camera FAB
            FloatingActionButton(
                onClick = onCameraClick,
                modifier = Modifier.size(48.dp),
                containerColor = MechanicBlue40,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp
                )
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Gallery FAB
            FloatingActionButton(
                onClick = onGalleryClick,
                modifier = Modifier.size(48.dp),
                containerColor = WarningAmber40,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp
                )
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun InstagramEmptyState(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Instagram-style camera icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MechanicBlue40.copy(alpha = 0.2f),
                            MechanicBlue40.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 120f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MechanicBlue40.copy(alpha = 0.3f),
                                MechanicBlue40.copy(alpha = 0.1f)
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
                    tint = MechanicBlue40
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.large))

        Text(
            text = "Share Your Vehicle",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = SteelGray40,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        Text(
            text = "Add photos to showcase your vehicle's condition and create a visual history of your maintenance journey",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(Spacing.extraLarge))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Button(
                onClick = onCameraClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(CornerRadius.large),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MechanicBlue40
                )
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text("Take Photo", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onGalleryClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(CornerRadius.large),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MechanicBlue40
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.horizontalGradient(
                        colors = listOf(MechanicBlue40, MechanicBlue40.copy(alpha = 0.7f))
                    )
                )
            ) {
                Icon(
                    Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text("Choose Photo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun InstagramPhotoGrid(
    photos: List<String>,
    mainPhotoIndex: Int,
    onPhotoClick: (Int) -> Unit,
    onSetMainPhoto: (Int) -> Unit,
    onDeletePhoto: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(300.dp) // Fixed height for Instagram-style grid
    ) {
        itemsIndexed(photos) { index, photoPath ->
            InstagramPhotoGridItem(
                photoPath = photoPath,
                isMain = index == mainPhotoIndex,
                onClick = { onPhotoClick(index) },
                onSetMain = { onSetMainPhoto(index) },
                onDelete = { onDeletePhoto(index) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramPhotoGridItem(
    photoPath: String,
    isMain: Boolean,
    onClick: () -> Unit,
    onSetMain: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "photo_scale"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        // Photo
        val photoFile = File(photoPath)
        if (photoFile.exists() && photoFile.canRead()) {
            Image(
                painter = rememberAsyncImagePainter(photoFile),
                contentDescription = "Vehicle Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            // Main photo indicator (top-left)
            if (isMain) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    Color.White.copy(alpha = 0.9f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Main Photo",
                        tint = Color(0xFFFFD700), // Gold color
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Delete button (top-right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showDeleteDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Delete Photo",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Set as main button (bottom)
            if (!isMain) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(6.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onSetMain() }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Set Main",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenPhotoGallery(
    photos: List<String>,
    startIndex: Int,
    mainPhotoIndex: Int,
    onDismiss: () -> Unit,
    onSetMainPhoto: (Int) -> Unit,
    onDeletePhoto: (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { photos.size }
    )

    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteIndex by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Photo pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                FullScreenPhotoItem(
                    photoPath = photos[page],
                    onDismiss = onDismiss
                )
            }

            // Top bar
            FullScreenTopBar(
                currentIndex = pagerState.currentPage,
                totalCount = photos.size,
                isMain = pagerState.currentPage == mainPhotoIndex,
                onDismiss = onDismiss,
                onSetMain = { onSetMainPhoto(pagerState.currentPage) },
                onDelete = {
                    deleteIndex = pagerState.currentPage
                    showDeleteDialog = true
                }
            )

            // Bottom indicator dots
            if (photos.size > 1) {
                FullScreenIndicatorDots(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = Spacing.extraLarge),
                    count = photos.size,
                    currentIndex = pagerState.currentPage
                )
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
        onConfirm = { onDeletePhoto(deleteIndex) },
        onDismiss = { showDeleteDialog = false }
    )
}

@Composable
fun FullScreenPhotoItem(
    photoPath: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offset = if (scale > 1f) {
                        Offset(
                            x = (offset.x + pan.x).coerceIn(
                                -(size.width * (scale - 1) / 2),
                                size.width * (scale - 1) / 2
                            ),
                            y = (offset.y + pan.y).coerceIn(
                                -(size.height * (scale - 1) / 2),
                                size.height * (scale - 1) / 2
                            )
                        )
                    } else {
                        Offset.Zero
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = if (scale > 1f) 1f else 2f
                        offset = Offset.Zero
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val photoFile = File(photoPath)
        if (photoFile.exists() && photoFile.canRead()) {
            Image(
                painter = rememberAsyncImagePainter(photoFile),
                contentDescription = "Full Screen Photo",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun FullScreenTopBar(
    currentIndex: Int,
    totalCount: Int,
    isMain: Boolean,
    onDismiss: () -> Unit,
    onSetMain: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.7f),
                        Color.Transparent
                    )
                )
            )
            .padding(Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }

        // Photo counter
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.5f)
        ) {
            Text(
                text = "${currentIndex + 1} of $totalCount",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
            )
        }

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // Set as main button
            IconButton(
                onClick = onSetMain,
                modifier = Modifier
                    .background(
                        if (isMain) Color.Yellow.copy(alpha = 0.8f) else Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = if (isMain) "Main Photo" else "Set as Main",
                    tint = if (isMain) Color.Black else Color.White
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .background(
                        Color.Red.copy(alpha = 0.7f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun FullScreenIndicatorDots(
    modifier: Modifier = Modifier,
    count: Int,
    currentIndex: Int
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(count) { index ->
            val isSelected = index == currentIndex
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "dot_scale"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .background(
                        if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                        CircleShape
                    )
            )
        }
    }
}