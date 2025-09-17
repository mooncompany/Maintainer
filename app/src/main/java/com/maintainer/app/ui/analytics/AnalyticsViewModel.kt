package com.maintainer.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maintainer.app.data.database.entity.MaintenanceRecord
import com.maintainer.app.data.database.entity.MaintenanceType
import com.maintainer.app.data.database.entity.Vehicle
import com.maintainer.app.data.repository.MaintenanceRepository
import com.maintainer.app.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AnalyticsData(
    val totalMaintenanceCost: Double = 0.0,
    val totalVehicles: Int = 0,
    val maintenanceRecords: List<MaintenanceRecord> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val monthlySpending: List<MonthlySpending> = emptyList(),
    val maintenanceTypeBreakdown: List<MaintenanceTypeData> = emptyList(),
    val vehicleCostBreakdown: List<VehicleCostData> = emptyList(),
    val recentTrends: TrendData = TrendData(),
    val averageCostPerVehicle: Double = 0.0,
    val mostExpensiveMaintenanceType: MaintenanceType? = null,
    val costPerMile: Double = 0.0
)

data class MonthlySpending(
    val month: String,
    val year: Int,
    val amount: Double,
    val count: Int
)

data class MaintenanceTypeData(
    val type: MaintenanceType,
    val count: Int,
    val totalCost: Double,
    val averageCost: Double
)

data class VehicleCostData(
    val vehicle: Vehicle,
    val totalCost: Double,
    val maintenanceCount: Int,
    val costPerMile: Double
)

data class TrendData(
    val isSpendingIncreasing: Boolean = false,
    val monthOverMonthChange: Double = 0.0,
    val totalRecordsThisMonth: Int = 0,
    val totalRecordsLastMonth: Int = 0
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    private val _analyticsData = MutableStateFlow(AnalyticsData())
    val analyticsData: StateFlow<AnalyticsData> = _analyticsData.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                vehicleRepository.getAllVehicles().collect { vehicles ->
                    // For now, we'll load data once for the analytics
                    // In a production app, you might want to combine flows more efficiently
                    val allRecords = getAllMaintenanceRecordsSync(vehicles)
                    val analytics = calculateAnalytics(vehicles, allRecords)
                    _analyticsData.value = analytics
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                // Handle error - could emit error state
            }
        }
    }

    private suspend fun getAllMaintenanceRecordsSync(vehicles: List<Vehicle>): List<MaintenanceRecord> {
        val allRecords = mutableListOf<MaintenanceRecord>()

        // For simplicity, we'll just collect the current state
        // In a more complex implementation, you'd want to use combine() with all flows
        vehicles.forEach { vehicle ->
            try {
                // Collect just the first emission (current state)
                maintenanceRepository.getMaintenanceRecordsForVehicle(vehicle.id)
                    .collect { records ->
                        allRecords.addAll(records)
                        return@collect // Exit after first emission
                    }
            } catch (e: Exception) {
                // Handle individual vehicle error
            }
        }

        return allRecords
    }

    private fun calculateAnalytics(vehicles: List<Vehicle>, records: List<MaintenanceRecord>): AnalyticsData {
        val totalCost = records.sumOf { it.cost }
        val totalVehicles = vehicles.size

        // Monthly spending calculation
        val monthlySpending = calculateMonthlySpending(records)

        // Maintenance type breakdown
        val typeBreakdown = calculateMaintenanceTypeBreakdown(records)

        // Vehicle cost breakdown
        val vehicleCostBreakdown = calculateVehicleCostBreakdown(vehicles, records)

        // Trend analysis
        val trends = calculateTrends(records)

        // Cost metrics
        val averageCostPerVehicle = if (totalVehicles > 0) totalCost / totalVehicles else 0.0
        val mostExpensiveType = typeBreakdown.maxByOrNull { it.totalCost }?.type

        // Calculate cost per mile
        val totalMiles = vehicles.sumOf { it.currentMileage }
        val costPerMile = if (totalMiles > 0) totalCost / totalMiles else 0.0

        return AnalyticsData(
            totalMaintenanceCost = totalCost,
            totalVehicles = totalVehicles,
            maintenanceRecords = records.sortedByDescending { it.serviceDate },
            vehicles = vehicles,
            monthlySpending = monthlySpending,
            maintenanceTypeBreakdown = typeBreakdown,
            vehicleCostBreakdown = vehicleCostBreakdown,
            recentTrends = trends,
            averageCostPerVehicle = averageCostPerVehicle,
            mostExpensiveMaintenanceType = mostExpensiveType,
            costPerMile = costPerMile
        )
    }

    private fun calculateMonthlySpending(records: List<MaintenanceRecord>): List<MonthlySpending> {
        val calendar = Calendar.getInstance()
        val monthlyData = mutableMapOf<String, MonthlySpending>()

        records.forEach { record ->
            calendar.time = record.serviceDate
            val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: ""
            val year = calendar.get(Calendar.YEAR)
            val key = "$month $year"

            val existing = monthlyData[key]
            if (existing != null) {
                monthlyData[key] = existing.copy(
                    amount = existing.amount + record.cost,
                    count = existing.count + 1
                )
            } else {
                monthlyData[key] = MonthlySpending(month, year, record.cost, 1)
            }
        }

        return monthlyData.values.sortedWith(compareBy({ it.year }, {
            when(it.month) {
                "January" -> 1; "February" -> 2; "March" -> 3; "April" -> 4
                "May" -> 5; "June" -> 6; "July" -> 7; "August" -> 8
                "September" -> 9; "October" -> 10; "November" -> 11; "December" -> 12
                else -> 0
            }
        })).takeLast(12)
    }

    private fun calculateMaintenanceTypeBreakdown(records: List<MaintenanceRecord>): List<MaintenanceTypeData> {
        val typeData = mutableMapOf<MaintenanceType, MutableList<Double>>()

        records.forEach { record ->
            if (!typeData.containsKey(record.type)) {
                typeData[record.type] = mutableListOf()
            }
            typeData[record.type]?.add(record.cost)
        }

        return typeData.map { (type, costs) ->
            MaintenanceTypeData(
                type = type,
                count = costs.size,
                totalCost = costs.sum(),
                averageCost = costs.average()
            )
        }.sortedByDescending { it.totalCost }
    }

    private fun calculateVehicleCostBreakdown(vehicles: List<Vehicle>, records: List<MaintenanceRecord>): List<VehicleCostData> {
        return vehicles.map { vehicle ->
            val vehicleRecords = records.filter { it.vehicleId == vehicle.id }
            val totalCost = vehicleRecords.sumOf { it.cost }
            val costPerMile = if (vehicle.currentMileage > 0) totalCost / vehicle.currentMileage else 0.0

            VehicleCostData(
                vehicle = vehicle,
                totalCost = totalCost,
                maintenanceCount = vehicleRecords.size,
                costPerMile = costPerMile
            )
        }.sortedByDescending { it.totalCost }
    }

    private fun calculateTrends(records: List<MaintenanceRecord>): TrendData {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.add(Calendar.MONTH, -1)
        val lastMonth = calendar.get(Calendar.MONTH)
        val lastYear = calendar.get(Calendar.YEAR)

        val thisMonthRecords = records.filter { record ->
            calendar.time = record.serviceDate
            calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear
        }

        val lastMonthRecords = records.filter { record ->
            calendar.time = record.serviceDate
            calendar.get(Calendar.MONTH) == lastMonth && calendar.get(Calendar.YEAR) == lastYear
        }

        val thisMonthSpending = thisMonthRecords.sumOf { it.cost }
        val lastMonthSpending = lastMonthRecords.sumOf { it.cost }

        val monthOverMonthChange = if (lastMonthSpending > 0) {
            ((thisMonthSpending - lastMonthSpending) / lastMonthSpending) * 100
        } else 0.0

        return TrendData(
            isSpendingIncreasing = thisMonthSpending > lastMonthSpending,
            monthOverMonthChange = monthOverMonthChange,
            totalRecordsThisMonth = thisMonthRecords.size,
            totalRecordsLastMonth = lastMonthRecords.size
        )
    }

    fun refreshData() {
        loadAnalyticsData()
    }
}