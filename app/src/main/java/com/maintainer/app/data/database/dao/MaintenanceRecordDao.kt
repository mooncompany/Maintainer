package com.maintainer.app.data.database.dao

import androidx.room.*
import com.maintainer.app.data.database.entity.MaintenanceRecord
import com.maintainer.app.data.database.entity.MaintenanceType
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceRecordDao {
    @Query("SELECT * FROM maintenance_records WHERE vehicleId = :vehicleId ORDER BY serviceDate DESC")
    fun getMaintenanceRecordsForVehicle(vehicleId: String): Flow<List<MaintenanceRecord>>

    @Query("SELECT * FROM maintenance_records WHERE id = :id")
    suspend fun getMaintenanceRecordById(id: String): MaintenanceRecord?

    @Query("SELECT * FROM maintenance_records WHERE vehicleId = :vehicleId AND type = :type ORDER BY serviceDate DESC LIMIT 1")
    suspend fun getLastMaintenanceOfType(vehicleId: String, type: MaintenanceType): MaintenanceRecord?

    @Query("SELECT * FROM maintenance_records ORDER BY serviceDate DESC")
    fun getAllMaintenanceRecords(): Flow<List<MaintenanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceRecord(record: MaintenanceRecord)

    @Update
    suspend fun updateMaintenanceRecord(record: MaintenanceRecord)

    @Delete
    suspend fun deleteMaintenanceRecord(record: MaintenanceRecord)

    @Query("SELECT SUM(cost) FROM maintenance_records WHERE vehicleId = :vehicleId")
    suspend fun getTotalCostForVehicle(vehicleId: String): Double?

    @Query("SELECT SUM(cost) FROM maintenance_records WHERE vehicleId = :vehicleId AND serviceDate BETWEEN :startDate AND :endDate")
    suspend fun getCostForVehicleInPeriod(vehicleId: String, startDate: Long, endDate: Long): Double?

    @Query("SELECT mileage FROM maintenance_records WHERE vehicleId = :vehicleId ORDER BY serviceDate DESC LIMIT 1")
    suspend fun getLatestServiceMileage(vehicleId: String): Int?
}