package com.maintainer.app.data.service

import com.maintainer.app.data.database.entity.PowertrainType
import com.maintainer.app.data.database.entity.MaintenanceType
import com.maintainer.app.data.database.entity.OilType
import java.util.*

data class ServiceTemplate(
    val name: String,
    val maintenanceType: MaintenanceType,
    val description: String,
    val intervalMiles: Int,
    val intervalMonths: Int,
    val powertrainTypes: List<PowertrainType>,
    val estimatedCostRange: IntRange,
    val priority: ServicePriority,
    val notes: String? = null
)

enum class ServicePriority {
    CRITICAL,    // Safety-related, must be done
    HIGH,        // Important for vehicle health
    MEDIUM,      // Regular maintenance
    LOW          // Optional/cosmetic
}

object ServiceTemplateEngine {

    private val templates = listOf(
        // Oil Changes - Powertrain Specific
        ServiceTemplate(
            name = "Conventional Oil Change",
            maintenanceType = MaintenanceType.OIL_CHANGE,
            description = "Regular oil change with conventional motor oil",
            intervalMiles = 3000,
            intervalMonths = 3,
            powertrainTypes = listOf(PowertrainType.GAS, PowertrainType.DIESEL),
            estimatedCostRange = 25..50,
            priority = ServicePriority.HIGH,
            notes = "Recommended for older vehicles or severe driving conditions"
        ),

        ServiceTemplate(
            name = "Synthetic Oil Change",
            maintenanceType = MaintenanceType.OIL_CHANGE,
            description = "Premium oil change with full synthetic motor oil",
            intervalMiles = 7500,
            intervalMonths = 6,
            powertrainTypes = listOf(PowertrainType.GAS),
            estimatedCostRange = 50..85,
            priority = ServicePriority.HIGH,
            notes = "Best protection for modern engines"
        ),

        ServiceTemplate(
            name = "Diesel Oil Change",
            maintenanceType = MaintenanceType.OIL_CHANGE,
            description = "Oil change with diesel-specific motor oil",
            intervalMiles = 5000,
            intervalMonths = 4,
            powertrainTypes = listOf(PowertrainType.DIESEL),
            estimatedCostRange = 60..100,
            priority = ServicePriority.HIGH,
            notes = "Use diesel-specific oil formulation"
        ),

        ServiceTemplate(
            name = "Hybrid Oil Change",
            maintenanceType = MaintenanceType.OIL_CHANGE,
            description = "Oil change for hybrid powertrain",
            intervalMiles = 10000,
            intervalMonths = 8,
            powertrainTypes = listOf(PowertrainType.HYBRID),
            estimatedCostRange = 45..75,
            priority = ServicePriority.HIGH,
            notes = "Extended intervals due to reduced engine runtime"
        ),

        // Filter Changes
        ServiceTemplate(
            name = "Air Filter Replacement",
            maintenanceType = MaintenanceType.FILTER_CHANGE,
            description = "Replace engine air filter",
            intervalMiles = 15000,
            intervalMonths = 12,
            powertrainTypes = listOf(PowertrainType.GAS, PowertrainType.DIESEL, PowertrainType.HYBRID),
            estimatedCostRange = 20..40,
            priority = ServicePriority.MEDIUM
        ),

        ServiceTemplate(
            name = "Cabin Air Filter",
            maintenanceType = MaintenanceType.FILTER_CHANGE,
            description = "Replace cabin air filter for clean interior air",
            intervalMiles = 15000,
            intervalMonths = 12,
            powertrainTypes = PowertrainType.values().toList(),
            estimatedCostRange = 15..35,
            priority = ServicePriority.LOW
        ),

        ServiceTemplate(
            name = "Fuel Filter Replacement",
            maintenanceType = MaintenanceType.FILTER_CHANGE,
            description = "Replace fuel system filter",
            intervalMiles = 30000,
            intervalMonths = 24,
            powertrainTypes = listOf(PowertrainType.GAS, PowertrainType.DIESEL),
            estimatedCostRange = 50..120,
            priority = ServicePriority.MEDIUM,
            notes = "Critical for diesel engines, especially with modern injection systems"
        ),

        // Brake Service
        ServiceTemplate(
            name = "Brake Pad Inspection",
            maintenanceType = MaintenanceType.BRAKE_SERVICE,
            description = "Inspect brake pads and rotors",
            intervalMiles = 12000,
            intervalMonths = 12,
            powertrainTypes = PowertrainType.values().toList(),
            estimatedCostRange = 50..100,
            priority = ServicePriority.CRITICAL
        ),

        ServiceTemplate(
            name = "Brake Pad Replacement",
            maintenanceType = MaintenanceType.BRAKE_SERVICE,
            description = "Replace brake pads",
            intervalMiles = 50000,
            intervalMonths = 48,
            powertrainTypes = listOf(PowertrainType.GAS, PowertrainType.DIESEL),
            estimatedCostRange = 150..400,
            priority = ServicePriority.CRITICAL
        ),

        ServiceTemplate(
            name = "Hybrid Brake Service",
            maintenanceType = MaintenanceType.BRAKE_SERVICE,
            description = "Brake service for hybrid/electric vehicles",
            intervalMiles = 75000,
            intervalMonths = 60,
            powertrainTypes = listOf(PowertrainType.HYBRID, PowertrainType.ELECTRIC),
            estimatedCostRange = 200..500,
            priority = ServicePriority.CRITICAL,
            notes = "Extended life due to regenerative braking"
        ),

        ServiceTemplate(
            name = "Brake Fluid Change",
            maintenanceType = MaintenanceType.BRAKE_SERVICE,
            description = "Replace brake fluid",
            intervalMiles = 30000,
            intervalMonths = 24,
            powertrainTypes = PowertrainType.values().toList(),
            estimatedCostRange = 80..150,
            priority = ServicePriority.HIGH
        ),

        // Tire Service
        ServiceTemplate(
            name = "Tire Rotation",
            maintenanceType = MaintenanceType.TIRE_SERVICE,
            description = "Rotate tires for even wear",
            intervalMiles = 6000,
            intervalMonths = 6,
            powertrainTypes = PowertrainType.values().toList(),
            estimatedCostRange = 25..60,
            priority = ServicePriority.MEDIUM
        ),

        ServiceTemplate(
            name = "Tire Replacement",
            maintenanceType = MaintenanceType.TIRE_SERVICE,
            description = "Replace worn tires",
            intervalMiles = 60000,
            intervalMonths = 60,
            powertrainTypes = PowertrainType.values().toList(),
            estimatedCostRange = 400..1200,
            priority = ServicePriority.CRITICAL,
            notes = "Electric vehicles may need more frequent replacement due to instant torque"
        ),

        // Battery Service
        ServiceTemplate(
            name = "12V Battery Test",
            maintenanceType = MaintenanceType.BATTERY_SERVICE,
            description = "Test 12V battery condition",
            intervalMiles = 12000,
            intervalMonths = 12,
            powertrainTypes = PowertrainType.values().toList(),
            estimatedCostRange = 20..50,
            priority = ServicePriority.MEDIUM
        ),

        ServiceTemplate(
            name = "12V Battery Replacement",
            maintenanceType = MaintenanceType.BATTERY_SERVICE,
            description = "Replace 12V auxiliary battery",
            intervalMiles = 60000,
            intervalMonths = 48,
            powertrainTypes = PowertrainType.values().toList(),
            estimatedCostRange = 100..250,
            priority = ServicePriority.HIGH
        ),

        ServiceTemplate(
            name = "EV Battery Health Check",
            maintenanceType = MaintenanceType.BATTERY_SERVICE,
            description = "Check high-voltage battery health and charging system",
            intervalMiles = 15000,
            intervalMonths = 12,
            powertrainTypes = listOf(PowertrainType.ELECTRIC, PowertrainType.HYBRID),
            estimatedCostRange = 100..300,
            priority = ServicePriority.HIGH,
            notes = "Important for battery longevity and performance"
        ),

        // Transmission Service
        ServiceTemplate(
            name = "Automatic Transmission Service",
            maintenanceType = MaintenanceType.TRANSMISSION_SERVICE,
            description = "Change transmission fluid and filter",
            intervalMiles = 60000,
            intervalMonths = 48,
            powertrainTypes = listOf(PowertrainType.GAS, PowertrainType.DIESEL),
            estimatedCostRange = 200..400,
            priority = ServicePriority.HIGH
        ),

        ServiceTemplate(
            name = "Manual Transmission Service",
            maintenanceType = MaintenanceType.TRANSMISSION_SERVICE,
            description = "Change manual transmission fluid",
            intervalMiles = 60000,
            intervalMonths = 48,
            powertrainTypes = listOf(PowertrainType.GAS, PowertrainType.DIESEL),
            estimatedCostRange = 100..200,
            priority = ServicePriority.MEDIUM
        ),

        // Coolant Service
        ServiceTemplate(
            name = "Coolant System Flush",
            maintenanceType = MaintenanceType.COOLANT_SERVICE,
            description = "Flush and replace engine coolant",
            intervalMiles = 60000,
            intervalMonths = 48,
            powertrainTypes = listOf(PowertrainType.GAS, PowertrainType.DIESEL, PowertrainType.HYBRID),
            estimatedCostRange = 150..300,
            priority = ServicePriority.HIGH
        ),

        ServiceTemplate(
            name = "EV Thermal Management",
            maintenanceType = MaintenanceType.COOLANT_SERVICE,
            description = "Service battery thermal management system",
            intervalMiles = 50000,
            intervalMonths = 48,
            powertrainTypes = listOf(PowertrainType.ELECTRIC),
            estimatedCostRange = 200..500,
            priority = ServicePriority.HIGH,
            notes = "Critical for battery performance and longevity"
        ),

        // Inspections
        ServiceTemplate(
            name = "Annual Safety Inspection",
            maintenanceType = MaintenanceType.INSPECTION,
            description = "Comprehensive safety and emissions inspection",
            intervalMiles = 12000,
            intervalMonths = 12,
            powertrainTypes = PowertrainType.values().toList(),
            estimatedCostRange = 50..150,
            priority = ServicePriority.CRITICAL,
            notes = "Required by law in many states"
        ),

        ServiceTemplate(
            name = "Multi-Point Inspection",
            maintenanceType = MaintenanceType.INSPECTION,
            description = "Comprehensive vehicle inspection",
            intervalMiles = 6000,
            intervalMonths = 6,
            powertrainTypes = PowertrainType.values().toList(),
            estimatedCostRange = 30..80,
            priority = ServicePriority.MEDIUM
        )
    )

    /**
     * Get all service templates applicable to a specific powertrain type
     */
    fun getTemplatesForPowertrain(powertrainType: PowertrainType): List<ServiceTemplate> {
        return templates.filter { it.powertrainTypes.contains(powertrainType) }
    }

    /**
     * Get recommended services based on mileage and last service date
     */
    fun getRecommendedServices(
        powertrainType: PowertrainType,
        currentMileage: Int,
        lastServiceMileage: Int = 0,
        lastServiceDate: Date = Date(0)
    ): List<ServiceTemplate> {
        val applicableTemplates = getTemplatesForPowertrain(powertrainType)
        val currentDate = Date()
        val monthsSinceLastService = ((currentDate.time - lastServiceDate.time) / (1000L * 60 * 60 * 24 * 30)).toInt()
        val milesSinceLastService = currentMileage - lastServiceMileage

        return applicableTemplates.filter { template ->
            milesSinceLastService >= template.intervalMiles ||
            monthsSinceLastService >= template.intervalMonths
        }.sortedBy { it.priority }
    }

    /**
     * Calculate oil change interval based on oil type and vehicle age
     */
    fun calculateOilChangeInterval(
        powertrainType: PowertrainType,
        oilType: OilType?,
        vehicleAge: Int
    ): Int {
        return when (powertrainType) {
            PowertrainType.ELECTRIC -> 0 // No oil changes needed
            PowertrainType.HYDROGEN -> 0 // No oil changes needed
            PowertrainType.HYBRID -> 10000 // Extended due to reduced engine runtime
            PowertrainType.DIESEL -> when (oilType) {
                OilType.CONVENTIONAL -> 5000
                OilType.SYNTHETIC_BLEND -> 7500
                OilType.FULL_SYNTHETIC -> 10000
                null -> 5000 // Conservative default
            }
            PowertrainType.GAS -> {
                val baseInterval = when (oilType) {
                    OilType.CONVENTIONAL -> 3000
                    OilType.SYNTHETIC_BLEND -> 5000
                    OilType.FULL_SYNTHETIC -> 7500
                    null -> 3000 // Conservative default
                }

                // Older vehicles (15+ years) should use shorter intervals
                if (vehicleAge >= 15) {
                    minOf(baseInterval, 3000)
                } else {
                    baseInterval
                }
            }
        }
    }

    /**
     * Get next recommended oil change mileage
     */
    fun getNextOilChangeMileage(
        powertrainType: PowertrainType,
        currentMileage: Int,
        lastOilChangeMileage: Int,
        oilType: OilType?,
        vehicleAge: Int
    ): Int? {
        val interval = calculateOilChangeInterval(powertrainType, oilType, vehicleAge)
        return if (interval > 0) {
            lastOilChangeMileage + interval
        } else {
            null // No oil changes needed for this powertrain type
        }
    }

    /**
     * Check if any critical services are overdue
     */
    fun getCriticalOverdueServices(
        powertrainType: PowertrainType,
        currentMileage: Int,
        lastServiceMileage: Int = 0,
        lastServiceDate: Date = Date(0)
    ): List<ServiceTemplate> {
        return getRecommendedServices(powertrainType, currentMileage, lastServiceMileage, lastServiceDate)
            .filter { it.priority == ServicePriority.CRITICAL }
    }

    /**
     * Get service templates by maintenance type
     */
    fun getTemplatesByType(maintenanceType: MaintenanceType): List<ServiceTemplate> {
        return templates.filter { it.maintenanceType == maintenanceType }
    }

    /**
     * Get estimated cost range for a service type and powertrain
     */
    fun getEstimatedCost(maintenanceType: MaintenanceType, powertrainType: PowertrainType): IntRange? {
        return templates
            .filter { it.maintenanceType == maintenanceType && it.powertrainTypes.contains(powertrainType) }
            .minByOrNull { it.estimatedCostRange.first }
            ?.estimatedCostRange
    }
}