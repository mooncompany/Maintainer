package com.maintainer.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "maintenance_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MaintenanceSchedule(
    @PrimaryKey
    val id: String,
    val vehicleId: String,
    val maintenanceType: MaintenanceType,
    val intervalMiles: Int? = null,
    val intervalMonths: Int? = null,
    val lastServiceMileage: Int = 0,
    val lastServiceDate: Date? = null,
    val nextDueMileage: Int? = null,
    val nextDueDate: Date? = null,
    val isEnabled: Boolean = true,
    val isCustom: Boolean = false,
    val description: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)