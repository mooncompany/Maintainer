package com.maintainer.app.ui.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintainer.app.data.database.entity.Vehicle
import com.maintainer.app.data.repository.VehicleRepository
import com.maintainer.app.data.repository.VehicleInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    val vehicles = vehicleRepository.getAllVehicles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadVehicle(vehicleId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val vehicle = vehicleRepository.getVehicleById(vehicleId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedVehicle = vehicle
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load vehicle: ${e.message}"
                    )
                }
            }
        }
    }

    fun decodeVin(vin: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDecodingVin = true) }
            try {
                val result = vehicleRepository.getVehicleInfoFromVin(vin)
                result.onSuccess { vehicleInfo ->
                    _uiState.update {
                        it.copy(
                            isDecodingVin = false,
                            decodedVehicleInfo = vehicleInfo
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDecodingVin = false,
                            errorMessage = "Failed to decode VIN: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDecodingVin = false,
                        errorMessage = "Failed to decode VIN: ${e.message}"
                    )
                }
            }
        }
    }

    fun saveVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                vehicleRepository.insertVehicle(vehicle)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        vehicleSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save vehicle: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                vehicleRepository.updateVehicle(vehicle)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        vehicleSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to update vehicle: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                vehicleRepository.deleteVehicle(vehicleId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete vehicle: ${e.message}")
                }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearDecodedVehicleInfo() {
        _uiState.update { it.copy(decodedVehicleInfo = null) }
    }

    fun clearVehicleSaved() {
        _uiState.update { it.copy(vehicleSaved = false) }
    }

    fun onVinScanned(scannedVin: String) {
        if (scannedVin.length == 17) {
            decodeVin(scannedVin)
        }
    }

    fun getTotalMaintenanceCostForVehicle(vehicleId: String, onResult: (Double) -> Unit) {
        viewModelScope.launch {
            try {
                val cost = vehicleRepository.getTotalMaintenanceCostForVehicle(vehicleId)
                onResult(cost)
            } catch (e: Exception) {
                onResult(0.0)
            }
        }
    }

    fun getLatestServiceMileageForVehicle(vehicleId: String, onResult: (Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val mileage = vehicleRepository.getLatestServiceMileageForVehicle(vehicleId)
                onResult(mileage)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}

data class VehicleUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDecodingVin: Boolean = false,
    val selectedVehicle: Vehicle? = null,
    val decodedVehicleInfo: VehicleInfo? = null,
    val vehicleSaved: Boolean = false,
    val errorMessage: String? = null
)