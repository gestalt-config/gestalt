package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SealedClassDecoderTest {

    @Test
    public void testDecodeSealedClass() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("vehicle.car.engineType", "gas");
        configs.put("vehicle.car.wheels", "4");
        configs.put("vehicle.car.size", "medium");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        var results = gestalt.getConfigResult("vehicle.car", TypeCapture.of(Vehicle.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());
        Assertions.assertInstanceOf(Car.class, results.results());

        Car car = (Car) results.results();
        Assertions.assertEquals("gas", car.getEngineType());
        Assertions.assertEquals(4, car.getWheels());
        Assertions.assertEquals("medium", car.getSize());
    }

    @Test
    public void testDecodeSealedClassMotorcycle() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("vehicle.motorcycle.engineType", "gas");
        configs.put("vehicle.motorcycle.wheels", "4");
        configs.put("vehicle.motorcycle.size", "100");
        configs.put("vehicle.motorcycle.kickstart", "true");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        var results = gestalt.getConfigResult("vehicle.motorcycle", TypeCapture.of(Vehicle.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());
        Assertions.assertInstanceOf(Motorcycle.class, results.results());

        Motorcycle car = (Motorcycle) results.results();
        Assertions.assertEquals("gas", car.getEngineType());
        Assertions.assertEquals(4, car.getWheels());
        Assertions.assertEquals(100, car.getSize());
        Assertions.assertTrue(car.isKickStart());
    }

    @Test
    public void testDecodeSealedClassAirplane() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("vehicle.airplane.engineType", "gas");
        configs.put("vehicle.airplane.wingSpan", "10");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        var results = gestalt.getConfigResult("vehicle.airplane", TypeCapture.of(Vehicle.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());
        Assertions.assertInstanceOf(Airplane.class, results.results());

        Airplane airplane = (Airplane) results.results();
        Assertions.assertEquals("gas", airplane.getEngineType());
        Assertions.assertEquals(10, airplane.getWingSpan().get());
    }

    @Test
    public void testDecodeSealedClassBad() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("vehicle.airplane.engineType", "gas");
        configs.put("vehicle.airplane.wingSpan", "10");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        var results = Assertions.assertThrows(GestaltException.class,
            () -> gestalt.getConfigResult("vehicle", TypeCapture.of(Vehicle.class), Tags.of()));

        Assertions.assertEquals("Failed getting config path: vehicle, for class: " +
                "org.github.gestalt.config.decoder.SealedClassDecoderTest$Vehicle\n" +
                " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding Object on path: vehicle.wingSpan, " +
                "with node: MapNode{airplane=MapNode{wingspan=LeafNode{value='10'}, enginetype=LeafNode{value='gas'}}}, with class: Airplane\n" +
                " - level: MISSING_VALUE, message: Unable to find node matching path: vehicle.engineType, for class: Airplane, " +
                "during object decoding",
            results.getMessage());

    }

    private static sealed class Vehicle permits Car, Motorcycle, Airplane, AirplaneC {
        String engineType;

        public Vehicle(String engineType) {
            this.engineType = engineType;
        }

        public String getEngineType() {
            return engineType;
        }

        public void setEngineType(String engineType) {
            this.engineType = engineType;
        }

    }

    private static final class Car extends Vehicle {
        public int wheels;
        public String size;

        public Car(String engineType, int wheels) {
            super(engineType);
            this.wheels = wheels;
        }

        public Car() {
            super(null);
        }

        public int getWheels() {
            return wheels;
        }

        public String getSize() {
            return size;
        }
    }

    private static final class Motorcycle extends Vehicle {
        int wheels;
        int size;
        boolean kickStart;

        public Motorcycle(String engineType, int wheels, boolean kickStart) {
            super(engineType);
            this.wheels = wheels;
            this.kickStart = kickStart;
        }

        public Motorcycle() {
            super(null);
        }

        public int getWheels() {
            return wheels;
        }

        public int getSize() {
            return size;
        }

        public boolean isKickStart() {
            return kickStart;
        }
    }

    private static final class Airplane extends Vehicle {
        Optional<Integer> wingSpan;

        public Airplane(String engineType, Integer wingSpan) {
            super(engineType);
            this.wingSpan = Optional.of(wingSpan);
        }

        public Airplane() {
            super(null);
        }

        public Optional<Integer> getWingSpan() {
            return wingSpan;
        }
    }

    private static final class AirplaneC extends Vehicle {
        Optional<Character> wingSpan;

        public AirplaneC(String engineType, Character wingSpan) {
            super(engineType);
            this.wingSpan = Optional.of(wingSpan);
        }

        public AirplaneC() {
            super(null);
        }

        public Optional<Character> getWingSpan() {
            return wingSpan;
        }
    }
}
