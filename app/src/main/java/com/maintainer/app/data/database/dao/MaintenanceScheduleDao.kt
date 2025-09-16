package com.maintainer.app.data.database.dao

import androidx.room.*
import com.maintainer.app.data.database.entity.MaintenanceSchedule
import com.maintainer.app.data.database.entity.MaintenanceType
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceScheduleDao {
    @Query("SELECT * FROM maintenance_schedules WHERE vehicleId = :vehicleId AND isEnabled = 1")
    fun getSchedulesForVehicle(vehicleId: String): Flow<List<MaintenanceSchedule>>

    @Query("SELECT * FROM maintenance_schedules WHERE id = :id")
    suspend fun getScheduleById(id: String): MaintenanceSchedule?

    @Query("SELECT * FROM maintenance_schedules WHERE vehicleId = :vehicleId AND maintenanceType = :type")
    suspend fun getScheduleByType(vehicleId: String, type: MaintenanceType): MaintenanceSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MaintenanceSchedule)

    @Update
    suspend fun updateSchedule(schedule: MaintenanceSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: MaintenanceSchedule)

    @Query("SELECT * FROM maintenance_schedules WHERE isEnabled = 1 AND (nextDueMileage IS NOT NULL OR nextDueDate IS NOT NULL)")
    suspend fun getUpcomingMaintenanceSchedules(): List<MaintenanceSchedule>

    @Query("SELECT * FROM maintenance_schedules ORDER BY createdAt DESC")
    fun getAllSchedules(): Flow<List<MaintenanceSchedule>>
}