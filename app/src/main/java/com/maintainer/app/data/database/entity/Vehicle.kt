package com.maintainer.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class PowertrainType {
    GAS,
    DIESEL,
    HYBRID,
    ELECTRIC,
    HYDROGEN
}

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey
    val id: String,
    val vin: String? = null,
    val make: String,
    val model: String,
    val year: Int,
    val color: String? = null,
    val licensePlate: String? = null,
    val currentMileage: Int = 0,
    val purchaseDate: Date? = null,
    val nickname: String? = null,
    val engineSize: String? = null,
    val fuelType: String? = null,
    val transmission: String? = null,
    val photoPath: String? = null,
    val registrationMonth: Int? = null,
    val registrationYear: Int? = null,
    val powertrainType: PowertrainType = PowertrainType.GAS,
    val photos: String? = null,
    val mainProfilePhotoIndex: Int = 0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isActive: Boolean = true
)