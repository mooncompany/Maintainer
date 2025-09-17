package com.maintainer.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.maintainer.app.data.database.converter.DateConverter
import com.maintainer.app.data.database.converter.PowertrainTypeConverter
import com.maintainer.app.data.database.dao.MaintenanceRecordDao
import com.maintainer.app.data.database.dao.MaintenanceScheduleDao
import com.maintainer.app.data.database.dao.VehicleDao
import com.maintainer.app.data.database.entity.MaintenanceRecord
import com.maintainer.app.data.database.entity.MaintenanceSchedule
import com.maintainer.app.data.database.entity.Vehicle

@Database(
    entities = [
        Vehicle::class,
        MaintenanceRecord::class,
        MaintenanceSchedule::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(DateConverter::class, PowertrainTypeConverter::class)
abstract class MaintainerDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun maintenanceRecordDao(): MaintenanceRecordDao
    abstract fun maintenanceScheduleDao(): MaintenanceScheduleDao

    companion object {
        const val DATABASE_NAME = "maintainer_database"

        @Volatile
        private var INSTANCE: MaintainerDatabase? = null

        fun getDatabase(context: Context): MaintainerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MaintainerDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}