package com.maintainer.app.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.maintainer.app.data.database.dao.MaintenanceRecordDao
import com.maintainer.app.data.database.dao.VehicleDao
import com.maintainer.app.data.database.entity.Vehicle
import com.maintainer.app.data.remote.NhtsaApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepository @Inject constructor(
    private val vehicleDao: VehicleDao,
    private val nhtsaApiService: NhtsaApiService,
    private val maintenanceRecordDao: MaintenanceRecordDao,
    @ApplicationContext private val context: Context
) {
    fun getAllVehicles(): Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    suspend fun getVehicleById(id: String): Vehicle? = vehicleDao.getVehicleById(id)

    suspend fun getVehicleByVin(vin: String): Vehicle? = vehicleDao.getVehicleByVin(vin)

    suspend fun insertVehicle(vehicle: Vehicle) = vehicleDao.insertVehicle(vehicle)

    suspend fun updateVehicle(vehicle: Vehicle) = vehicleDao.updateVehicle(
        vehicle.copy(updatedAt = Date())
    )

    suspend fun deleteVehicle(id: String) = vehicleDao.deleteVehicle(id)

    suspend fun updateMileage(vehicleId: String, mileage: Int) =
        vehicleDao.updateMileage(vehicleId, mileage, Date().time)

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    suspend fun getVehicleInfoFromVin(vin: String): Result<VehicleInfo> {
        if (!isNetworkAvailable()) {
            return Result.failure(Exception("No internet connection available. Please check your network and try again."))
        }
        return try {
            val response = nhtsaApiService.decodeVin(vin)
            if (response.isSuccessful && response.body() != null) {
                val vinData = response.body()!!
                Result.success(
                    VehicleInfo(
                        make = vinData.Results.find { it.Variable == "Make" }?.Value ?: "",
                        model = vinData.Results.find { it.Variable == "Model" }?.Value ?: "",
                        year = vinData.Results.find { it.Variable == "Model Year" }?.Value?.toIntOrNull() ?: 0,
                        engineSize = vinData.Results.find { it.Variable == "Engine Number of Cylinders" }?.Value,
                        fuelType = vinData.Results.find { it.Variable == "Fuel Type - Primary" }?.Value,
                        transmission = vinData.Results.find { it.Variable == "Transmission Style" }?.Value
                    )
                )
            } else {
                Result.failure(Exception("Failed to decode VIN"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTotalMaintenanceCostForVehicle(vehicleId: String): Double =
        maintenanceRecordDao.getTotalCostForVehicle(vehicleId) ?: 0.0

    suspend fun getLatestServiceMileageForVehicle(vehicleId: String): Int? =
        maintenanceRecordDao.getLatestServiceMileage(vehicleId)
}

data class VehicleInfo(
    val make: String,
    val model: String,
    val year: Int,
    val engineSize: String?,
    val fuelType: String?,
    val transmission: String?
)