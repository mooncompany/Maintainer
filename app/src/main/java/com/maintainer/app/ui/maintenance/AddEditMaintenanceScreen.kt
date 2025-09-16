package com.maintainer.app.ui.maintenance

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maintainer.app.data.database.entity.MaintenanceRecord
import com.maintainer.app.data.database.entity.MaintenanceType
import com.maintainer.app.data.database.entity.OilType
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMaintenanceScreen(
    vehicleId: String,
    recordId: String? = null,
    onNavigateBack: () -> Unit,
    onMaintenanceSaved: () -> Unit,
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = recordId != null

    var selectedType by remember { mutableStateOf(MaintenanceType.OIL_CHANGE) }
    var description by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var serviceName by remember { mutableStateOf("") }
    var serviceLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var serviceDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var selectedOilType by remember { mutableStateOf<OilType?>(null) }
    var showOilTypeDropdown by remember { mutableStateOf(false) }
    var showReceiptOptions by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Camera permission handling
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.processReceiptImage(it)
        }
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val mimeType = contentResolver.getType(uri)

                when {
                    mimeType?.startsWith("image/") == true -> {
                        // Handle image files - save to app storage and process for OCR
                        val fileName = "receipt_${UUID.randomUUID()}.jpg"
                        val destinationFile = File(context.filesDir, fileName)

                        contentResolver.openInputStream(uri)?.use { input ->
                            destinationFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        // Save the file path
                        viewModel.setReceiptImagePath(destinationFile.absolutePath)

                        // Also process for OCR
                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        bitmap?.let { bmp ->
                            viewModel.processReceiptImage(bmp)
                        }
                    }
                    mimeType == "application/pdf" -> {
                        // Handle PDF files - save to app storage for later access
                        val fileName = "receipt_${UUID.randomUUID()}.pdf"
                        val destinationFile = File(context.filesDir, fileName)

                        contentResolver.openInputStream(uri)?.use { input ->
                            destinationFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        // Save the file path (PDFs can't be processed for OCR easily)
                        viewModel.setReceiptImagePath(destinationFile.absolutePath)
                        android.util.Log.d("FilePicker", "PDF saved to: ${destinationFile.absolutePath}")
                    }
                    else -> {
                        android.util.Log.w("FilePicker", "Unsupported file type: $mimeType")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("FilePicker", "Error processing selected file", e)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    LaunchedEffect(recordId) {
        if (recordId != null) {
            viewModel.loadMaintenanceRecord(recordId)
        }
    }

    LaunchedEffect(uiState.selectedRecord) {
        uiState.selectedRecord?.let { record ->
            selectedType = record.type
            description = record.description
            cost = if (record.cost > 0) record.cost.toString() else ""
            mileage = record.mileage.toString()
            serviceName = record.serviceName ?: ""
            serviceLocation = record.serviceLocation ?: ""
            notes = record.notes ?: ""
            serviceDate = record.serviceDate
            selectedOilType = record.oilType
        }
    }

    LaunchedEffect(uiState.recordSaved) {
        if (uiState.recordSaved) {
            onMaintenanceSaved()
            viewModel.clearRecordSaved()
        }
    }

    LaunchedEffect(uiState.ocrText) {
        uiState.ocrText?.let { text ->
            // Simple OCR text processing - in a real app, this would be more sophisticated
            if (text.contains("oil", ignoreCase = true)) {
                selectedType = MaintenanceType.OIL_CHANGE
                if (description.isEmpty()) {
                    description = "Oil change"
                }
            }
            // Extract potential cost from OCR text
            val costRegex = "\\$([0-9]+\\.?[0-9]*)".toRegex()
            costRegex.find(text)?.let { matchResult ->
                if (cost.isEmpty()) {
                    cost = matchResult.groupValues[1]
                }
            }
            viewModel.clearOcrText()
        }
    }

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearErrorMessage()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                serviceDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Maintenance" else "Add Maintenance") },
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
            // Maintenance Type Dropdown
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = !showTypeDropdown }
            ) {
                OutlinedTextField(
                    value = selectedType.name.replace('_', ' ').lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Maintenance Type") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    MaintenanceType.values().forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    type.name.replace('_', ' ').lowercase()
                                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                )
                            },
                            onClick = {
                                selectedType = type
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // Oil Type Dropdown (only show for oil changes)
            if (selectedType == MaintenanceType.OIL_CHANGE) {
                ExposedDropdownMenuBox(
                    expanded = showOilTypeDropdown,
                    onExpandedChange = { showOilTypeDropdown = !showOilTypeDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedOilType?.name?.replace('_', ' ')?.lowercase()
                            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Select oil type",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Oil Type") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showOilTypeDropdown,
                        onDismissRequest = { showOilTypeDropdown = false }
                    ) {
                        OilType.values().forEach { oilType ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (oilType) {
                                            OilType.CONVENTIONAL -> "Conventional"
                                            OilType.SYNTHETIC_BLEND -> "Synthetic Blend"
                                            OilType.FULL_SYNTHETIC -> "Full Synthetic"
                                        }
                                    )
                                },
                                onClick = {
                                    selectedOilType = oilType
                                    showOilTypeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description*") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Brief description of the maintenance performed") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text("Cost") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("$") }
                )

                OutlinedTextField(
                    value = mileage,
                    onValueChange = { mileage = it },
                    label = { Text("Mileage*") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            OutlinedTextField(
                value = dateFormat.format(serviceDate),
                onValueChange = { },
                label = { Text("Service Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Change")
                    }
                }
            )

            OutlinedTextField(
                value = serviceName,
                onValueChange = { serviceName = it },
                label = { Text("Service Provider") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = serviceLocation,
                onValueChange = { serviceLocation = it },
                label = { Text("Service Location") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Button(
                onClick = { showReceiptOptions = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Attachment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Receipt")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val record = MaintenanceRecord(
                        id = recordId ?: UUID.randomUUID().toString(),
                        vehicleId = vehicleId,
                        type = selectedType,
                        description = description,
                        cost = cost.toDoubleOrNull() ?: 0.0,
                        mileage = mileage.toIntOrNull() ?: 0,
                        serviceDate = serviceDate,
                        serviceName = serviceName.takeIf { it.isNotEmpty() },
                        serviceLocation = serviceLocation.takeIf { it.isNotEmpty() },
                        notes = notes.takeIf { it.isNotEmpty() },
                        receiptImagePath = uiState.receiptImagePath,
                        ocrText = uiState.ocrText,
                        oilType = selectedOilType
                    )

                    if (isEditing) {
                        viewModel.updateMaintenanceRecord(record)
                    } else {
                        viewModel.saveMaintenanceRecord(record)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = description.isNotEmpty() && mileage.isNotEmpty() && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isEditing) "Update Record" else "Save Record")
            }

            Text(
                text = "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Receipt Options Dialog
    if (showReceiptOptions) {
        AlertDialog(
            onDismissRequest = { showReceiptOptions = false },
            title = { Text("Add Receipt") },
            text = {
                Column {
                    Text("Choose how you'd like to add a receipt for this maintenance:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Camera: Take a photo of your receipt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "• Upload: Select images or PDF files",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showReceiptOptions = false
                            if (hasCameraPermission) {
                                cameraLauncher.launch(null)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Camera")
                    }

                    Button(
                        onClick = {
                            showReceiptOptions = false
                            // Accept images and PDFs specifically
                            filePicker.launch("image/*,application/pdf")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showReceiptOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Date(millis))
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}