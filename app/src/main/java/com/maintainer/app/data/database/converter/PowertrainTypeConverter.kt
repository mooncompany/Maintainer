package com.maintainer.app.data.database.converter

import androidx.room.TypeConverter
import com.maintainer.app.data.database.entity.PowertrainType

class PowertrainTypeConverter {
    @TypeConverter
    fun fromPowertrainType(type: PowertrainType): String {
        return type.name
    }

    @TypeConverter
    fun toPowertrainType(type: String): PowertrainType {
        return try {
            PowertrainType.valueOf(type)
        } catch (e: IllegalArgumentException) {
            PowertrainType.GAS // Default fallback
        }
    }
}