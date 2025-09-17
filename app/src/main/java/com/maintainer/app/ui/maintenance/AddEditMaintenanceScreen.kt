package com.maintainer.app.ui.maintenance

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maintainer.app.data.database.entity.MaintenanceRecord
import com.maintainer.app.data.database.entity.MaintenanceType
import com.maintainer.app.data.database.entity.OilType
import com.maintainer.app.data.database.entity.PowertrainType
import com.maintainer.app.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

// Smart Service Templates
data class ServiceTemplate(
    val id: String,
    val name: String,
    val type: MaintenanceType,
    val icon: ImageVector,
    val color: Color,
    val description: String,
    val estimatedCost: IntRange,
    val suggestedMileageInterval: Int? = null,
    val powertrainTypes: List<PowertrainType>? = null
)

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

    var selectedTemplate by remember { mutableStateOf<ServiceTemplate?>(null) }
    var selectedType by remember { mutableStateOf(MaintenanceType.OIL_CHANGE) }
    var description by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var serviceName by remember { mutableStateOf("") }
    var serviceLocation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var serviceDate by remember { mutableStateOf(Date()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedOilType by remember { mutableStateOf<OilType?>(null) }
    var showReceiptOptions by remember { mutableStateOf(false) }
    var showTemplateSelector by remember { mutableStateOf(!isEditing) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Smart Service Templates
    val serviceTemplates = remember {
        listOf(
            ServiceTemplate(
                id = "oil_change",
                name = "Oil Change",
                type = MaintenanceType.OIL_CHANGE,
                icon = Icons.Default.Opacity,
                color = Color(0xFF2196F3),
                description = "Regular engine oil and filter change",
                estimatedCost = 30..80,
                suggestedMileageInterval = 5000
            ),
            ServiceTemplate(
                id = "tire_rotation",
                name = "Tire Rotation",
                type = MaintenanceType.TIRE_SERVICE,
                icon = Icons.Default.Circle,
                color = Color(0xFF607D8B),
                description = "Rotate tires for even wear",
                estimatedCost = 20..50,
                suggestedMileageInterval = 8000
            ),
            ServiceTemplate(
                id = "brake_service",
                name = "Brake Service",
                type = MaintenanceType.BRAKE_SERVICE,
                icon = Icons.Default.Settings,
                color = Color(0xFFFF5722),
                description = "Brake pad inspection and replacement",
                estimatedCost = 100..300
            ),
            ServiceTemplate(
                id = "air_filter",
                name = "Air Filter",
                type = MaintenanceType.FILTER_CHANGE,
                icon = Icons.Default.FilterAlt,
                color = Color(0xFF4CAF50),
                description = "Replace engine air filter",
                estimatedCost = 15..40,
                suggestedMileageInterval = 12000
            ),
            ServiceTemplate(
                id = "battery_test",
                name = "Battery Service",
                type = MaintenanceType.BATTERY_SERVICE,
                icon = Icons.Default.Battery6Bar,
                color = Color(0xFFFFEB3B),
                description = "Battery testing and maintenance",
                estimatedCost = 20..150
            ),
            ServiceTemplate(
                id = "inspection",
                name = "Vehicle Inspection",
                type = MaintenanceType.INSPECTION,
                icon = Icons.Default.Visibility,
                color = Color(0xFF795548),
                description = "Comprehensive vehicle inspection",
                estimatedCost = 25..75
            ),
            ServiceTemplate(
                id = "coolant_flush",
                name = "Coolant Service",
                type = MaintenanceType.COOLANT_SERVICE,
                icon = Icons.Default.Waves,
                color = Color(0xFF00BCD4),
                description = "Coolant flush and replacement",
                estimatedCost = 80..150,
                suggestedMileageInterval = 30000
            ),
            ServiceTemplate(
                id = "transmission",
                name = "Transmission Service",
                type = MaintenanceType.TRANSMISSION_SERVICE,
                icon = Icons.Default.Settings,
                color = Color(0xFF9C27B0),
                description = "Transmission fluid change",
                estimatedCost = 150..300,
                suggestedMileageInterval = 60000
            )
        )
    }

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
                        val fileName = "receipt_${UUID.randomUUID()}.jpg"
                        val destinationFile = File(context.filesDir, fileName)

                        contentResolver.openInputStream(uri)?.use { input ->
                            destinationFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        viewModel.setReceiptImagePath(destinationFile.absolutePath)

                        val inputStream = contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        bitmap?.let { bmp ->
                            viewModel.processReceiptImage(bmp)
                        }
                    }
                    mimeType == "application/pdf" -> {
                        val fileName = "receipt_${UUID.randomUUID()}.pdf"
                        val destinationFile = File(context.filesDir, fileName)

                        contentResolver.openInputStream(uri)?.use { input ->
                            destinationFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        viewModel.setReceiptImagePath(destinationFile.absolutePath)
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
            if (text.contains("oil", ignoreCase = true)) {
                selectedType = MaintenanceType.OIL_CHANGE
                if (description.isEmpty()) {
                    description = "Oil change"
                }
            }
            val costRegex = "\\$([0-9]+\\.?[0-9]*)".toRegex()
            costRegex.find(text)?.let { matchResult ->
                if (cost.isEmpty()) {
                    cost = matchResult.groupValues[1]
                }
            }
            viewModel.clearOcrText()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isEditing) "Edit Service Record" else "Add Service",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = Spacing.screenHorizontal)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Spacer(modifier = Modifier.height(Spacing.medium))

                // Smart Template Selector (only for new records)
                if (!isEditing && showTemplateSelector) {
                    SmartTemplateSelector(
                        templates = serviceTemplates,
                        selectedTemplate = selectedTemplate,
                        onTemplateSelected = { template ->
                            selectedTemplate = template
                            selectedType = template.type
                            description = template.description
                            cost = template.estimatedCost.first.toString()
                            showTemplateSelector = false
                        }
                    )
                }

                // Service Form Card
                ServiceFormCard(
                    selectedType = selectedType,
                    description = description,
                    cost = cost,
                    mileage = mileage,
                    serviceName = serviceName,
                    serviceLocation = serviceLocation,
                    notes = notes,
                    serviceDate = serviceDate,
                    selectedOilType = selectedOilType,
                    onTypeChange = { selectedType = it },
                    onDescriptionChange = { description = it },
                    onCostChange = { cost = it },
                    onMileageChange = { mileage = it },
                    onServiceNameChange = { serviceName = it },
                    onServiceLocationChange = { serviceLocation = it },
                    onNotesChange = { notes = it },
                    onDateClick = { showDatePicker = true }
                )

                // Receipt Section
                ReceiptSection(
                    hasReceipt = uiState.receiptImagePath != null,
                    onAddReceipt = { showReceiptOptions = true }
                )

                // Save Button
                SmartSaveButton(
                    isEditing = isEditing,
                    isSaving = uiState.isSaving,
                    isValid = description.isNotEmpty() && mileage.isNotEmpty(),
                    onSave = {
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
                    }
                )

                Spacer(modifier = Modifier.height(Spacing.massive))
            }
        }
    }

    // Receipt Options Dialog
    if (showReceiptOptions) {
        ReceiptOptionsDialog(
            onCameraSelected = {
                showReceiptOptions = false
                if (hasCameraPermission) {
                    cameraLauncher.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onUploadSelected = {
                showReceiptOptions = false
                filePicker.launch("image/*,application/pdf")
            },
            onDismiss = { showReceiptOptions = false }
        )
    }
}

@Composable
fun SmartTemplateSelector(
    templates: List<ServiceTemplate>,
    selectedTemplate: ServiceTemplate?,
    onTemplateSelected: (ServiceTemplate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Service Templates",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SteelGray40
                )

                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MechanicBlue40,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = "Choose a template to auto-fill service details",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Spacing.large))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                items(templates) { template ->
                    ServiceTemplateCard(
                        template = template,
                        isSelected = selectedTemplate?.id == template.id,
                        onClick = { onTemplateSelected(template) }
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceTemplateCard(
    template: ServiceTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "template_scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                template.color.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                template.color.copy(alpha = 0.3f),
                                template.color.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    template.icon,
                    contentDescription = null,
                    tint = template.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = template.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) template.color else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.tiny))

            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = "${NumberFormat.getCurrencyInstance(Locale.US).format(template.estimatedCost.first)} - ${NumberFormat.getCurrencyInstance(Locale.US).format(template.estimatedCost.last)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = template.color
            )
        }
    }
}

@Composable
fun ServiceFormCard(
    selectedType: MaintenanceType,
    description: String,
    cost: String,
    mileage: String,
    serviceName: String,
    serviceLocation: String,
    notes: String,
    serviceDate: Date,
    selectedOilType: OilType?,
    onTypeChange: (MaintenanceType) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onMileageChange: (String) -> Unit,
    onServiceNameChange: (String) -> Unit,
    onServiceLocationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDateClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            Text(
                text = "Service Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SteelGray40
            )

            // Service Type
            var showTypeDropdown by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = !showTypeDropdown }
            ) {
                OutlinedTextField(
                    value = selectedType.name.replace('_', ' ').lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Service Type *") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MechanicBlue40
                    )
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
                                onTypeChange(type)
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // Oil Type (only for oil changes)
            if (selectedType == MaintenanceType.OIL_CHANGE) {
                var showOilTypeDropdown by remember { mutableStateOf(false) }
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
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MechanicBlue40
                        )
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
                                    // selectedOilType = oilType
                                    showOilTypeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MechanicBlue40
                )
            )

            // Row for Cost and Mileage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                OutlinedTextField(
                    value = cost,
                    onValueChange = onCostChange,
                    label = { Text("Cost") },
                    leadingIcon = { Text("$", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MechanicBlue40
                    )
                )

                OutlinedTextField(
                    value = mileage,
                    onValueChange = onMileageChange,
                    label = { Text("Mileage *") },
                    trailingIcon = { Text("mi", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MechanicBlue40
                    )
                )
            }

            // Service Date
            OutlinedTextField(
                value = dateFormat.format(serviceDate),
                onValueChange = { },
                label = { Text("Service Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateClick() },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MechanicBlue40
                )
            )

            // Service Name
            OutlinedTextField(
                value = serviceName,
                onValueChange = onServiceNameChange,
                label = { Text("Service Provider") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MechanicBlue40
                )
            )

            // Service Location
            OutlinedTextField(
                value = serviceLocation,
                onValueChange = onServiceLocationChange,
                label = { Text("Service Location") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MechanicBlue40
                )
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Additional Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MechanicBlue40
                )
            )
        }
    }
}

@Composable
fun ReceiptSection(
    hasReceipt: Boolean,
    onAddReceipt: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Receipt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SteelGray40
                )

                if (hasReceipt) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaintenanceGreen.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "ATTACHED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaintenanceGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.medium))

            OutlinedButton(
                onClick = onAddReceipt,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(CornerRadius.medium),
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
                    if (hasReceipt) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                Text(
                    text = if (hasReceipt) "Replace Receipt" else "Add Receipt",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SmartSaveButton(
    isEditing: Boolean,
    isSaving: Boolean,
    isValid: Boolean,
    onSave: () -> Unit
) {
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = isValid && !isSaving,
        shape = RoundedCornerShape(CornerRadius.large),
        colors = ButtonDefaults.buttonColors(
            containerColor = MechanicBlue40
        )
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White
            )
            Spacer(modifier = Modifier.width(Spacing.small))
        }

        Icon(
            if (isEditing) Icons.Default.Save else Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(Spacing.small))

        Text(
            text = if (isEditing) "Update Service Record" else "Save Service Record",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ReceiptOptionsDialog(
    onCameraSelected: () -> Unit,
    onUploadSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Receipt",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Choose how you'd like to add a receipt:")
                Spacer(modifier = Modifier.height(Spacing.medium))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    Card(
                        onClick = onCameraSelected,
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MechanicBlue40.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.medium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MechanicBlue40,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(Spacing.small))
                            Text(
                                text = "Camera",
                                fontWeight = FontWeight.Bold,
                                color = MechanicBlue40
                            )
                            Text(
                                text = "Take photo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        onClick = onUploadSelected,
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = WarningAmber40.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.medium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Upload,
                                contentDescription = null,
                                tint = WarningAmber40,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(Spacing.small))
                            Text(
                                text = "Upload",
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber40
                            )
                            Text(
                                text = "Select file",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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