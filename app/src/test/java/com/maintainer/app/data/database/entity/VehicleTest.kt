package com.maintainer.app.data.database.entity

import org.junit.Test
import org.junit.Assert.*
import java.util.*

class VehicleTest {

    @Test
    fun vehicle_creation_works() {
        val vehicle = Vehicle(
            id = "test-id",
            make = "Toyota",
            model = "Camry",
            year = 2020,
            currentMileage = 25000
        )

        assertEquals("Toyota", vehicle.make)
        assertEquals("Camry", vehicle.model)
        assertEquals(2020, vehicle.year)
        assertEquals(25000, vehicle.currentMileage)
        assertTrue(vehicle.isActive)
    }

    @Test
    fun vehicle_with_vin_creation() {
        val testVin = "1HGBH41JXMN109186"
        val vehicle = Vehicle(
            id = "test-id-2",
            vin = testVin,
            make = "Honda",
            model = "Accord",
            year = 2019
        )

        assertEquals(testVin, vehicle.vin)
        assertEquals("Honda", vehicle.make)
        assertEquals("Accord", vehicle.model)
    }
}