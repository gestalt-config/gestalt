package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.builder.GestaltBuilder
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.getConfigResult
import org.github.gestalt.config.source.MapConfigSourceBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class KSealedClassDecoderTest {
    @Test
    @Throws(GestaltException::class)
    fun testDecodeSealedClass() {
        // Create a map of configurations we wish to inject.
        val configs: MutableMap<String?, String?> = HashMap()
        configs.put("vehicle.car.engineType", "gas")
        configs.put("vehicle.car.wheels", "4")
        configs.put("vehicle.car.size", "medium")

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build()

        gestalt.loadConfigs()

        val results = gestalt.getConfigResult<Vehicle>("vehicle.car")

        Assertions.assertTrue(results.hasResults())
        Assertions.assertFalse(results.hasErrors())
        Assertions.assertInstanceOf(Car::class.java, results.results())

        val car = results.results() as Car
        Assertions.assertEquals("gas", car.engineType)
        Assertions.assertEquals(4, car.wheels)
        Assertions.assertEquals("medium", car.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun testDecodeSealedClassMotorcycle() {
        // Create a map of configurations we wish to inject.
        val configs: MutableMap<String?, String?> = HashMap()
        configs.put("vehicle.motorcycle.engineType", "gas")
        configs.put("vehicle.motorcycle.wheels", "4")
        configs.put("vehicle.motorcycle.size", "100")
        configs.put("vehicle.motorcycle.kickstart", "true")

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build()

        gestalt.loadConfigs()

        val results = gestalt.getConfigResult<Vehicle>("vehicle.motorcycle")

        Assertions.assertTrue(results.hasResults())
        Assertions.assertFalse(results.hasErrors())
        Assertions.assertInstanceOf(Motorcycle::class.java, results.results())

        val car = results.results() as Motorcycle
        Assertions.assertEquals("gas", car.engineType)
        Assertions.assertEquals(4, car.wheels)
        Assertions.assertEquals(100, car.size)
        Assertions.assertTrue(car.kickStart)
    }

    @Test
    @Throws(GestaltException::class)
    fun testDecodeSealedClassAirplane() {
        // Create a map of configurations we wish to inject.
        val configs: MutableMap<String?, String?> = HashMap()
        configs.put("vehicle.airplane.engineType", "gas")
        configs.put("vehicle.airplane.wingSpan", "10")

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build()

        gestalt.loadConfigs()

        val results = gestalt.getConfigResult<Vehicle?>("vehicle.airplane")

        Assertions.assertTrue(results.hasResults())
        Assertions.assertFalse(results.hasErrors())
        Assertions.assertInstanceOf(Airplane::class.java, results.results())

        val airplane = results.results() as Airplane
        Assertions.assertEquals("gas", airplane.engineType)
        Assertions.assertEquals(10, airplane.wingSpan)
    }

    @Test
    @Throws(GestaltException::class)
    fun testDecodeSealedClassBad() {
        // Create a map of configurations we wish to inject.
        val configs: MutableMap<String?, String?> = HashMap()
        configs.put("vehicle.airplane.engineType", "gas")
        configs.put("vehicle.airplane.wingSpan", "10")

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build()

        gestalt.loadConfigs()

        val results = Assertions.assertThrows(
            GestaltException::class.java,
            Executable {
                gestalt.getConfigResult<Vehicle?>("vehicle")
            })

        Assertions.assertEquals(
            "Failed getting config path: vehicle, for class: " +
                "org.github.gestalt.config.kotlin.decoder.KSealedClassDecoderTest\$Vehicle\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: vehicle.wingSpan, " +
                "for class: Airplane, during object decoding\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: vehicle.engineType, " +
                "for class: Airplane, during object decoding",
            results.message
        )
    }

    sealed class Vehicle(var engineType: String?)

    class Car : Vehicle {
        var wheels: Int = 0
        var size: String? = null

        constructor(engineType: String?, wheels: Int) : super(engineType) {
            this.wheels = wheels
        }

        constructor() : super(null)
    }

    class Motorcycle : Vehicle {
        var wheels: Int = 0
        var size: Int = 0
        var kickStart: Boolean = false

        constructor(engineType: String?, wheels: Int, kickStart: Boolean) : super(engineType) {
            this.wheels = wheels
            this.kickStart = kickStart
        }

        constructor() : super(null)
    }

    class Airplane : Vehicle {
        var wingSpan: Int? = null

        constructor(engineType: String?, wingSpan: Int?) : super(engineType) {
            this.wingSpan = wingSpan!!
        }

        constructor() : super(null)
    }

    class AirplaneC : Vehicle {
        var wingSpan: Char? = null

        constructor(engineType: String?, wingSpan: Char?) : super(engineType) {
            this.wingSpan = wingSpan!!
        }

        constructor() : super(null)
    }
}
