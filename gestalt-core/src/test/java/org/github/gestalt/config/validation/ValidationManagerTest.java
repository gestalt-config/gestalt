package org.github.gestalt.config.validation;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ValidationManagerTest {

    @Test
    public void testValidatorOk() {
        var validationManager = new ValidationManager(List.of(new TestConfigValidator(true)));

        var results = validationManager.validator("test", "my.path", TypeCapture.of(String.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("test", results.results());
    }

    @Test
    public void testTwoValidatorOk() {
        var validationManager = new ValidationManager(List.of(new TestConfigValidator(true), new TestConfigValidator(true)));

        var results = validationManager.validator("test", "my.path", TypeCapture.of(String.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("test", results.results());
    }

    @Test
    public void testValidatorOneError() {
        var validationManager = new ValidationManager(List.of(new TestConfigValidator(false)));

        var results = validationManager.validator("test", "my.path", TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke", results.getErrors().get(0).description());
    }

    @Test
    public void testTwoValidatorOneError() {
        var validationManager = new ValidationManager(List.of(new TestConfigValidator(true), new TestConfigValidator(false)));

        var results = validationManager.validator("test", "my.path", TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke", results.getErrors().get(0).description());
    }

    @Test
    public void testTwoValidatorTwoError() {
        var validationManager = new ValidationManager(List.of(new TestConfigValidator(false), new TestConfigValidator(false)));

        var results = validationManager.validator("test", "my.path", TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(2, results.getErrors().size());

        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke", results.getErrors().get(0).description());

        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(1).level());
        Assertions.assertEquals("something broke", results.getErrors().get(1).description());
    }

}
