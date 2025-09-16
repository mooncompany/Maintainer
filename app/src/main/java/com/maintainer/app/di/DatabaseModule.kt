package com.maintainer.app.di

import android.content.Context
import androidx.room.Room
import com.maintainer.app.data.database.MaintainerDatabase
import com.maintainer.app.data.database.dao.MaintenanceRecordDao
import com.maintainer.app.data.database.dao.MaintenanceScheduleDao
import com.maintainer.app.data.database.dao.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MaintainerDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MaintainerDatabase::class.java,
            MaintainerDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideVehicleDao(database: MaintainerDatabase): VehicleDao {
        return database.vehicleDao()
    }

    @Provides
    fun provideMaintenanceRecordDao(database: MaintainerDatabase): MaintenanceRecordDao {
        return database.maintenanceRecordDao()
    }

    @Provides
    fun provideMaintenanceScheduleDao(database: MaintainerDatabase): MaintenanceScheduleDao {
        return database.maintenanceScheduleDao()
    }
}