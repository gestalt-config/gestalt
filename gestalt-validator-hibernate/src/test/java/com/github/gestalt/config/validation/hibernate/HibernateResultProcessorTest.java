package com.github.gestalt.config.validation.hibernate;

import com.github.gestalt.config.validation.hibernate.builder.HibernateModuleBuilder;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HibernateResultProcessorTest {

    @Test
    public void testHibernateProcessResultsOk() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        var validator = factory.getValidator();
        HibernateModuleBuilder builder = HibernateModuleBuilder.builder().setValidator(validator);
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(builder.build());

        HibernateResultProcessor hibernateValidator = new HibernateResultProcessor();

        hibernateValidator.applyConfig(gestaltConfig);

        Car car = new Car("Morris", "DD-AB-123", 2);

        var results = hibernateValidator.processResults(GResultOf.result(car), "car", false, null, TypeCapture.of(Car.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());
    }

    @Test
    public void testHibernateProcessResultsErrors() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        var validator = factory.getValidator();
        HibernateModuleBuilder builder = HibernateModuleBuilder.builder().setValidator(validator);
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(builder.build());

        HibernateResultProcessor hibernateValidator = new HibernateResultProcessor();

        hibernateValidator.applyConfig(gestaltConfig);

        Car car = new Car("Morris", "DD-AB-123", 1);

        var results = hibernateValidator.processResults(GResultOf.result(car), "car", false, null, TypeCapture.of(Car.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());
        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Hibernate Validator, on path: car, error: must be greater than or equal to 2",
            results.getErrors().get(0).description());
    }

    @Test
    public void testHibernateProcessResultsMultipleErrors() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        var validator = factory.getValidator();
        HibernateModuleBuilder builder = HibernateModuleBuilder.builder().setValidator(validator);
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(builder.build());

        HibernateResultProcessor hibernateValidator = new HibernateResultProcessor();

        hibernateValidator.applyConfig(gestaltConfig);

        Car car = new Car("Morris", "A", 1);

        var results = hibernateValidator.processResults(GResultOf.result(car), "car", false, null, TypeCapture.of(Car.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());
        Assertions.assertEquals(2, results.getErrors().size());

        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());

        org.assertj.core.api.Assertions.assertThat(results.getErrors().get(0).description()).containsAnyOf(
            "Hibernate Validator, on path: car, error: size must be between 2 and 14",
            "Hibernate Validator, on path: car, error: must be greater than or equal to 2");

        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(1).level());
        org.assertj.core.api.Assertions.assertThat(results.getErrors().get(1).description()).containsAnyOf(
            "Hibernate Validator, on path: car, error: size must be between 2 and 14",
            "Hibernate Validator, on path: car, error: must be greater than or equal to 2");
    }

    @Test
    public void testHibernateProcessResultsHadErrors() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        var validator = factory.getValidator();
        HibernateModuleBuilder builder = HibernateModuleBuilder.builder().setValidator(validator);
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(builder.build());

        HibernateResultProcessor hibernateValidator = new HibernateResultProcessor();

        hibernateValidator.applyConfig(gestaltConfig);

        var results = hibernateValidator.processResults(GResultOf.errors(new ValidationError(ValidationLevel.ERROR) {
            @Override
            public String description() {
                return "Something bad";
            }
        }), "car", false, null, TypeCapture.of(Car.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());
        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Something bad", results.getErrors().get(0).description());
    }

    public static class Car {

        @NotNull
        private final String manufacturer;

        @NotNull
        @Size(min = 2, max = 14)
        private final String licensePlate;

        @Min(2)
        private final int seatCount;

        public Car(String manufacturer, String licencePlate, int seatCount) {
            this.manufacturer = manufacturer;
            this.licensePlate = licencePlate;
            this.seatCount = seatCount;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public int getSeatCount() {
            return seatCount;
        }
    }
}

