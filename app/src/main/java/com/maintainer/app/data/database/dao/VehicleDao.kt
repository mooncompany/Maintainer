package com.maintainer.app.data.database.dao

import androidx.room.*
import com.maintainer.app.data.database.entity.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: String): Vehicle?

    @Query("SELECT * FROM vehicles WHERE vin = :vin")
    suspend fun getVehicleByVin(vin: String): Vehicle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Query("UPDATE vehicles SET isActive = 0 WHERE id = :id")
    suspend fun deleteVehicle(id: String)

    @Query("UPDATE vehicles SET currentMileage = :mileage, updatedAt = :updatedAt WHERE id = :vehicleId")
    suspend fun updateMileage(vehicleId: String, mileage: Int, updatedAt: Long)
}