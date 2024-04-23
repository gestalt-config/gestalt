package org.github.gestalt.config.processor.result.validation;


import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.observations.ObservationManager;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;

class ValidationResultProcessorTest {

    ObservationManager observationManager = Mockito.mock(ObservationManager.class);

    @AfterEach
    public void cleanup() {
        validateMockitoUsage();
        Mockito.clearInvocations(observationManager);
    }

    @Test
    public void testValidatorOk() throws GestaltException {
        GestaltConfig config = new GestaltConfig();
        config.setObservationsEnabled(true);

        var validationManager = new ValidationResultProcessor(List.of(new TestConfigValidator(true)), observationManager);
        validationManager.applyConfig(config);

        var dbInfo = new DBInfo();
        dbInfo.setPassword("test");

        var dbInfo2 = new DBInfo();
        dbInfo2.setPassword("pass");

        var results = validationManager.processResults(GResultOf.result(dbInfo), "my.path", true, dbInfo2,
            TypeCapture.of(DBInfo.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("test", results.results().getPassword());
        Mockito.verify(observationManager, times(0))
            .recordObservation(eq("get.config.validation.error"), anyDouble(), any());
    }

    @Test
    public void testTwoValidatorOk() throws GestaltException {
        GestaltConfig config = new GestaltConfig();
        config.setObservationsEnabled(true);

        var validationManager = new ValidationResultProcessor(
            List.of(new TestConfigValidator(true), new TestConfigValidator(true)), observationManager);
        validationManager.applyConfig(config);

        var dbInfo = new DBInfo();
        dbInfo.setPassword("test");

        var dbInfo2 = new DBInfo();
        dbInfo2.setPassword("pass");

        var results = validationManager.processResults(GResultOf.result(dbInfo), "my.path", true, dbInfo2,
            TypeCapture.of(DBInfo.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("test", results.results().getPassword());

        Mockito.verify(observationManager, times(0))
            .recordObservation(eq("get.config.validation.error"), anyDouble(), eq(Tags.of()));
    }

    @Test
    public void testValidatorOneError() {
        GestaltConfig config = new GestaltConfig();
        config.setObservationsEnabled(true);

        var validationManager = new ValidationResultProcessor(List.of(new TestConfigValidator(false)), observationManager);
        validationManager.applyConfig(config);

        var dbInfo = new DBInfo();
        dbInfo.setPassword("test");

        var ex = Assertions.assertThrows(GestaltException.class, () -> validationManager.processResults(GResultOf.result(dbInfo),
            "my.path", false, null, TypeCapture.of(DBInfo.class), Tags.of()));

        Assertions.assertEquals("Validation failed for config path: my.path, and class: org.github.gestalt.config.test.classes.DBInfo\n" +
            " - level: ERROR, message: something broke", ex.getMessage());

        Mockito.verify(observationManager, times(1))
            .recordObservation(eq("get.config.validation.error"), eq(1.0D), eq(Tags.of()));
    }

    @Test
    public void testTwoValidatorOneError() {
        GestaltConfig config = new GestaltConfig();
        config.setObservationsEnabled(true);

        var validationManager = new ValidationResultProcessor(
            List.of(new TestConfigValidator(true), new TestConfigValidator(false)), observationManager);
        validationManager.applyConfig(config);

        var dbInfo = new DBInfo();
        dbInfo.setPassword("test");

        var ex = Assertions.assertThrows(GestaltException.class, () -> validationManager.processResults(GResultOf.result(dbInfo),
            "my.path", false, null, TypeCapture.of(DBInfo.class), Tags.of()));

        Assertions.assertEquals("Validation failed for config path: my.path, and class: org.github.gestalt.config.test.classes.DBInfo\n" +
            " - level: ERROR, message: something broke", ex.getMessage());

        Mockito.verify(observationManager, times(1))
            .recordObservation(eq("get.config.validation.error"), eq(1.0D), eq(Tags.of()));
    }

    @Test
    public void testTwoValidatorOneErrorNullObservation() {
        GestaltConfig config = new GestaltConfig();
        config.setObservationsEnabled(true);

        var validationManager = new ValidationResultProcessor(
            List.of(new TestConfigValidator(true), new TestConfigValidator(false)), null);
        validationManager.applyConfig(config);

        var dbInfo = new DBInfo();
        dbInfo.setPassword("test");

        var ex = Assertions.assertThrows(GestaltException.class, () -> validationManager.processResults(GResultOf.result(dbInfo),
            "my.path", false, null, TypeCapture.of(DBInfo.class), Tags.of()));

        Assertions.assertEquals("Validation failed for config path: my.path, and class: org.github.gestalt.config.test.classes.DBInfo\n" +
            " - level: ERROR, message: something broke", ex.getMessage());

        Mockito.verify(observationManager, times(0))
            .recordObservation(eq("get.config.validation.error"), anyDouble(), eq(Tags.of()));
    }

    @Test
    public void testTwoValidatorOneErrorNullConfig() {
        var validationManager = new ValidationResultProcessor(
            List.of(new TestConfigValidator(true), new TestConfigValidator(false)), observationManager);

        var dbInfo = new DBInfo();
        dbInfo.setPassword("test");

        var ex = Assertions.assertThrows(GestaltException.class, () -> validationManager.processResults(GResultOf.result(dbInfo),
            "my.path", false, null, TypeCapture.of(DBInfo.class), Tags.of()));

        Assertions.assertEquals("Validation failed for config path: my.path, and class: org.github.gestalt.config.test.classes.DBInfo\n" +
            " - level: ERROR, message: something broke", ex.getMessage());

        Mockito.verify(observationManager, times(0))
            .recordObservation(eq("get.config.validation.error"), anyDouble(), eq(Tags.of()));
    }

    @Test
    public void testTwoValidatorOneErrorDisabledConfig() {
        GestaltConfig config = new GestaltConfig();
        config.setObservationsEnabled(false);

        var validationManager = new ValidationResultProcessor(
            List.of(new TestConfigValidator(true), new TestConfigValidator(false)), observationManager);
        validationManager.applyConfig(config);


        var dbInfo = new DBInfo();
        dbInfo.setPassword("test");

        var ex = Assertions.assertThrows(GestaltException.class, () -> validationManager.processResults(GResultOf.result(dbInfo),
            "my.path", false, null, TypeCapture.of(DBInfo.class), Tags.of()));

        Assertions.assertEquals("Validation failed for config path: my.path, and class: org.github.gestalt.config.test.classes.DBInfo\n" +
            " - level: ERROR, message: something broke", ex.getMessage());

        Mockito.verify(observationManager, times(0))
            .recordObservation(eq("get.config.validation.error"), anyDouble(), eq(Tags.of()));
    }


    @Test
    public void testTwoValidatorTwoError() {
        GestaltConfig config = new GestaltConfig();
        config.setObservationsEnabled(true);

        var validationManager = new ValidationResultProcessor(
            List.of(new TestConfigValidator(false), new TestConfigValidator(false)), observationManager);
        validationManager.applyConfig(config);

        var dbInfo = new DBInfo();
        dbInfo.setPassword("test");

        var ex = Assertions.assertThrows(GestaltException.class, () -> validationManager.processResults(GResultOf.result(dbInfo),
            "my.path", false, null, TypeCapture.of(DBInfo.class), Tags.of()));


        Assertions.assertEquals("Validation failed for config path: my.path, and class: org.github.gestalt.config.test.classes.DBInfo\n" +
            " - level: ERROR, message: something broke\n" +
            " - level: ERROR, message: something broke", ex.getMessage());

        Mockito.verify(observationManager, times(1))
            .recordObservation(eq("get.config.validation.error"), eq(2.0D), eq(Tags.of()));
    }

    @Test
    public void testValidatorOneErrorOptional() throws GestaltException {
        GestaltConfig config = new GestaltConfig();
        config.setObservationsEnabled(true);

        var validationManager = new ValidationResultProcessor(List.of(new TestConfigValidator(false)), observationManager);
        validationManager.applyConfig(config);

        var dbInfo = new DBInfo();
        dbInfo.setPassword("test");

        var dbInfo2 = new DBInfo();
        dbInfo2.setPassword("pass");

        var results = validationManager.processResults(GResultOf.result(dbInfo), "my.path", true, dbInfo2,
            TypeCapture.of(DBInfo.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Mockito.verify(observationManager, times(1))
            .recordObservation(eq("get.config.validation.error"), eq(1.0D), eq(Tags.of()));

    }
}
