package com.maintainer.app.ui.vehicles

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.maintainer.app.data.database.entity.Vehicle
import com.maintainer.app.data.database.entity.PowertrainType
import com.maintainer.app.ui.components.PhotoPickerSection
import com.maintainer.app.data.photo.PhotoManager
import com.maintainer.app.data.photo.VehiclePhotoUtils
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.ArrowDropDown
import android.graphics.Bitmap
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVehicleScreen(
    vehicleId: String? = null,
    onNavigateBack: () -> Unit,
    onVehicleSaved: () -> Unit,
    viewModel: VehicleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = vehicleId != null

    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var vin by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var currentMileage by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var showVinScanner by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    // Camera permission handling
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Create file for camera capture
    val photoFile = remember {
        File(context.filesDir, "vehicle_${UUID.randomUUID()}.jpg")
    }
    val photoUri = remember {
        try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
        } catch (e: Exception) {
            android.util.Log.e("AddEditVehicle", "FileProvider error", e)
            null
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    // Camera capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoPath = photoFile.absolutePath
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Copy file to app directory
            val destinationFile = File(context.filesDir, "vehicle_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            photoPath = destinationFile.absolutePath
        }
    }

    LaunchedEffect(vehicleId) {
        if (vehicleId != null) {
            viewModel.loadVehicle(vehicleId)
        }
    }

    LaunchedEffect(uiState.selectedVehicle) {
        uiState.selectedVehicle?.let { vehicle ->
            make = vehicle.make
            model = vehicle.model
            year = vehicle.year.toString()
            vin = vehicle.vin ?: ""
            nickname = vehicle.nickname ?: ""
            licensePlate = vehicle.licensePlate ?: ""
            color = vehicle.color ?: ""
            currentMileage = if (vehicle.currentMileage > 0) vehicle.currentMileage.toString() else ""
            photoPath = vehicle.photoPath
        }
    }

    LaunchedEffect(uiState.decodedVehicleInfo) {
        uiState.decodedVehicleInfo?.let { info ->
            make = info.make
            model = info.model
            year = info.year.toString()
            viewModel.clearDecodedVehicleInfo()
        }
    }

    LaunchedEffect(uiState.vehicleSaved) {
        if (uiState.vehicleSaved) {
            onVehicleSaved()
            viewModel.clearVehicleSaved()
        }
    }

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Vehicle" else "Add Vehicle") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = vin,
                onValueChange = { vin = it },
                label = { Text("VIN") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = { showVinScanner = true }
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan VIN")
                    }
                },
                supportingText = { Text("17-character vehicle identification number") }
            )

            if (vin.length == 17) {
                Button(
                    onClick = { viewModel.decodeVin(vin) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isDecodingVin
                ) {
                    if (uiState.isDecodingVin) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Decode VIN")
                }
            }

            // Photo Section
            Text(
                text = "Vehicle Photo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { showPhotoOptions = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (photoPath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(File(photoPath)),
                        contentDescription = "Vehicle Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to add photo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = make,
                onValueChange = { make = it },
                label = { Text("Make*") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model*") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Year*") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Optional: A friendly name for your vehicle") }
            )

            OutlinedTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it },
                label = { Text("License Plate") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currentMileage,
                onValueChange = { currentMileage = it },
                label = { Text("Current Mileage") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val vehicle = Vehicle(
                        id = vehicleId ?: UUID.randomUUID().toString(),
                        make = make,
                        model = model,
                        year = year.toIntOrNull() ?: 0,
                        vin = vin.takeIf { it.isNotEmpty() },
                        nickname = nickname.takeIf { it.isNotEmpty() },
                        licensePlate = licensePlate.takeIf { it.isNotEmpty() },
                        color = color.takeIf { it.isNotEmpty() },
                        currentMileage = currentMileage.toIntOrNull() ?: 0,
                        photoPath = photoPath
                    )

                    if (isEditing) {
                        viewModel.updateVehicle(vehicle)
                    } else {
                        viewModel.saveVehicle(vehicle)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = make.isNotEmpty() && model.isNotEmpty() && year.isNotEmpty() && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isEditing) "Update Vehicle" else "Save Vehicle")
            }

            Text(
                text = "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showVinScanner) {
        VinScannerScreen(
            onVinScanned = { scannedVin ->
                vin = scannedVin
                showVinScanner = false
                viewModel.onVinScanned(scannedVin)
            },
            onClose = { showVinScanner = false }
        )
    }

    // Photo options dialog
    if (showPhotoOptions) {
        AlertDialog(
            onDismissRequest = { showPhotoOptions = false },
            title = { Text("Add Vehicle Photo") },
            text = { Text("Choose how you'd like to add a photo of your vehicle") },
            confirmButton = {
                Row {
                    TextButton(
                        onClick = {
                            showPhotoOptions = false
                            if (hasCameraPermission && photoUri != null) {
                                cameraLauncher.launch(photoUri)
                            } else if (hasCameraPermission && photoUri == null) {
                                // Show error that photo capture is not available
                                android.util.Log.e("AddEditVehicle", "PhotoUri is null, cannot launch camera")
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            showPhotoOptions = false
                            galleryLauncher.launch("image/*")
                        }
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}