package com.maintainer.app.ui.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintainer.app.data.database.entity.MaintenanceRecord
import com.maintainer.app.data.database.entity.MaintenanceSchedule
import com.maintainer.app.data.repository.MaintenanceRepository
import com.maintainer.app.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MaintenanceUiState())
    val uiState: StateFlow<MaintenanceUiState> = _uiState.asStateFlow()

    private val _selectedVehicleId = MutableStateFlow<String?>(null)

    val maintenanceRecords = _selectedVehicleId
        .filterNotNull()
        .flatMapLatest { vehicleId ->
            maintenanceRepository.getMaintenanceRecordsForVehicle(vehicleId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val maintenanceSchedules = _selectedVehicleId
        .filterNotNull()
        .flatMapLatest { vehicleId ->
            maintenanceRepository.getSchedulesForVehicle(vehicleId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectVehicle(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
    }

    fun loadMaintenanceRecord(recordId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val record = maintenanceRepository.getMaintenanceRecordById(recordId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedRecord = record
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load maintenance record: ${e.message}"
                    )
                }
            }
        }
    }

    fun saveMaintenanceRecord(record: MaintenanceRecord) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                maintenanceRepository.insertMaintenanceRecord(record)

                // Update vehicle's current mileage to the latest service mileage
                vehicleRepository.updateMileage(record.vehicleId, record.mileage)

                // Update corresponding schedule
                maintenanceRepository.updateScheduleAfterMaintenance(
                    record.vehicleId,
                    record.type,
                    record.mileage,
                    record.serviceDate
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        recordSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save maintenance record: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateMaintenanceRecord(record: MaintenanceRecord) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                maintenanceRepository.updateMaintenanceRecord(record)

                // Update vehicle's current mileage to latest service mileage
                // (in case this updated record is now the latest)
                val latestMileage = vehicleRepository.getLatestServiceMileageForVehicle(record.vehicleId)
                latestMileage?.let { mileage ->
                    vehicleRepository.updateMileage(record.vehicleId, mileage)
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        recordSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to update maintenance record: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteMaintenanceRecord(record: MaintenanceRecord) {
        viewModelScope.launch {
            try {
                maintenanceRepository.deleteMaintenanceRecord(record)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete maintenance record: ${e.message}")
                }
            }
        }
    }

    fun saveMaintenanceSchedule(schedule: MaintenanceSchedule) {
        viewModelScope.launch {
            try {
                maintenanceRepository.insertSchedule(schedule)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to save schedule: ${e.message}")
                }
            }
        }
    }

    fun processOcrText(ocrText: String) {
        _uiState.update { it.copy(ocrText = ocrText) }
    }

    fun processReceiptImage(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingOcr = true) }
            try {
                val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)
                val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(
                    com.google.mlkit.vision.text.latin.TextRecognizerOptions.DEFAULT_OPTIONS
                )

                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val extractedText = visionText.text
                        _uiState.update {
                            it.copy(
                                isProcessingOcr = false,
                                ocrText = extractedText
                            )
                        }
                    }
                    .addOnFailureListener { e ->
                        _uiState.update {
                            it.copy(
                                isProcessingOcr = false,
                                errorMessage = "Failed to process receipt: ${e.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingOcr = false,
                        errorMessage = "Failed to process receipt: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearRecordSaved() {
        _uiState.update { it.copy(recordSaved = false) }
    }

    fun clearOcrText() {
        _uiState.update { it.copy(ocrText = null) }
    }

    fun setReceiptImagePath(path: String) {
        _uiState.update { it.copy(receiptImagePath = path) }
    }
}

data class MaintenanceUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isProcessingOcr: Boolean = false,
    val selectedRecord: MaintenanceRecord? = null,
    val ocrText: String? = null,
    val receiptImagePath: String? = null,
    val recordSaved: Boolean = false,
    val errorMessage: String? = null
)