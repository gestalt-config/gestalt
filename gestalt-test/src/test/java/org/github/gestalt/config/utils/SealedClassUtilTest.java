package org.github.gestalt.config.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SealedClassUtilTest {

    private static sealed class Vehicle permits Car, Motorcycle, Airplane {
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
        public String trunkSize;

        public Car(String engineType, String trunkSize) {
            super(engineType);
            this.trunkSize = trunkSize;
        }

        public String getTrunkSize() {
            return trunkSize;
        }
    }

    private static final class Motorcycle extends Vehicle {
        String kickStand;

        public Motorcycle(String engineType, String kickStand) {
            super(engineType);
            this.kickStand = kickStand;
        }

        public String getKickStand() {
            return kickStand;
        }
    }

    private static final class Airplane extends Vehicle {
        Integer wingSpan;

        public Airplane(String engineType, Integer wingSpan) {
            super(engineType);
            this.wingSpan = wingSpan;
        }

        public Integer getWingSpan() {
            return wingSpan;
        }
    }

    @Test
    void isSealed() {
        Assertions.assertTrue(SealedClassUtil.isSealed(Vehicle.class));
        Assertions.assertFalse(SealedClassUtil.isSealed(Airplane.class));
        Assertions.assertFalse(SealedClassUtil.isSealed(Boolean.class));
    }

    @Test
    void getPermittedSubclasses() {
        Class<?> [] permitted = {
            Car.class,
            Motorcycle.class,
            Airplane.class
        };

        Class<?>[] retrieved = SealedClassUtil.getPermittedSubclasses(Vehicle.class);

        Assertions.assertArrayEquals(permitted, retrieved);
    }

    @Test
    void getPermittedSubclassesNonSealed() {
        Class<?>[] retrieved = SealedClassUtil.getPermittedSubclasses(Boolean.class);

        Assertions.assertEquals(0, retrieved.length);
    }
}
