package com.maintainer.app.data.repository

import com.maintainer.app.data.database.dao.MaintenanceRecordDao
import com.maintainer.app.data.database.dao.MaintenanceScheduleDao
import com.maintainer.app.data.database.entity.MaintenanceRecord
import com.maintainer.app.data.database.entity.MaintenanceSchedule
import com.maintainer.app.data.database.entity.MaintenanceType
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepository @Inject constructor(
    private val maintenanceRecordDao: MaintenanceRecordDao,
    private val maintenanceScheduleDao: MaintenanceScheduleDao
) {
    fun getMaintenanceRecordsForVehicle(vehicleId: String): Flow<List<MaintenanceRecord>> =
        maintenanceRecordDao.getMaintenanceRecordsForVehicle(vehicleId)

    suspend fun getMaintenanceRecordById(id: String): MaintenanceRecord? =
        maintenanceRecordDao.getMaintenanceRecordById(id)

    suspend fun insertMaintenanceRecord(record: MaintenanceRecord) =
        maintenanceRecordDao.insertMaintenanceRecord(record)

    suspend fun updateMaintenanceRecord(record: MaintenanceRecord) =
        maintenanceRecordDao.updateMaintenanceRecord(record.copy(updatedAt = Date()))

    suspend fun deleteMaintenanceRecord(record: MaintenanceRecord) =
        maintenanceRecordDao.deleteMaintenanceRecord(record)

    suspend fun getTotalCostForVehicle(vehicleId: String): Double =
        maintenanceRecordDao.getTotalCostForVehicle(vehicleId) ?: 0.0

    suspend fun getCostForVehicleInPeriod(vehicleId: String, startDate: Date, endDate: Date): Double =
        maintenanceRecordDao.getCostForVehicleInPeriod(vehicleId, startDate.time, endDate.time) ?: 0.0

    fun getSchedulesForVehicle(vehicleId: String): Flow<List<MaintenanceSchedule>> =
        maintenanceScheduleDao.getSchedulesForVehicle(vehicleId)

    suspend fun getScheduleById(id: String): MaintenanceSchedule? =
        maintenanceScheduleDao.getScheduleById(id)

    suspend fun insertSchedule(schedule: MaintenanceSchedule) =
        maintenanceScheduleDao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: MaintenanceSchedule) =
        maintenanceScheduleDao.updateSchedule(schedule.copy(updatedAt = Date()))

    suspend fun deleteSchedule(schedule: MaintenanceSchedule) =
        maintenanceScheduleDao.deleteSchedule(schedule)

    suspend fun getUpcomingMaintenanceSchedules(): List<MaintenanceSchedule> =
        maintenanceScheduleDao.getUpcomingMaintenanceSchedules()

    suspend fun updateScheduleAfterMaintenance(vehicleId: String, maintenanceType: MaintenanceType, mileage: Int, date: Date) {
        val schedule = maintenanceScheduleDao.getScheduleByType(vehicleId, maintenanceType)
        schedule?.let { existing ->
            val nextDueMileage = existing.intervalMiles?.let { mileage + it }
            val nextDueDate = existing.intervalMonths?.let { months ->
                Calendar.getInstance().apply {
                    time = date
                    add(Calendar.MONTH, months)
                }.time
            }

            val updatedSchedule = existing.copy(
                lastServiceMileage = mileage,
                lastServiceDate = date,
                nextDueMileage = nextDueMileage,
                nextDueDate = nextDueDate,
                updatedAt = Date()
            )
            maintenanceScheduleDao.updateSchedule(updatedSchedule)
        }
    }
}