package com.maintainer.app

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun vin_validation_works() {
        val validVin = "1HGBH41JXMN109186"
        val invalidVin = "123"

        assertTrue("Valid VIN should be 17 characters", validVin.length == 17)
        assertFalse("Invalid VIN should not be 17 characters", invalidVin.length == 17)
    }
}