package com.maintainer.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "maintenance_records",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MaintenanceRecord(
    @PrimaryKey
    val id: String,
    val vehicleId: String,
    val type: MaintenanceType,
    val description: String,
    val cost: Double,
    val mileage: Int,
    val serviceDate: Date,
    val serviceName: String? = null,
    val serviceLocation: String? = null,
    val notes: String? = null,
    val receiptImagePath: String? = null,
    val ocrText: String? = null,
    val oilType: OilType? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

enum class MaintenanceType {
    OIL_CHANGE,
    FILTER_CHANGE,
    BRAKE_SERVICE,
    TIRE_SERVICE,
    BATTERY_SERVICE,
    ENGINE_SERVICE,
    TRANSMISSION_SERVICE,
    COOLANT_SERVICE,
    INSPECTION,
    REPAIR,
    OTHER
}

enum class OilType {
    CONVENTIONAL,
    SYNTHETIC_BLEND,
    FULL_SYNTHETIC
}