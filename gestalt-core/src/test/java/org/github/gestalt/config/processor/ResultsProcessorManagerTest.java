package org.github.gestalt.config.processor;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.processor.result.ResultsProcessorManager;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ResultsProcessorManagerTest {

    @Test
    public void testResultProcessorOk() {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(true)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("test", results.results());
    }

    @Test
    public void testTwoResultProcessorOk() {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(true), new TestResultProcessor(true)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("test", results.results());
    }

    @Test
    public void testResultProcessorOneError() {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(false)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke", results.getErrors().get(0).description());
    }

    @Test
    public void testTwoResultProcessorOneError() {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(true), new TestResultProcessor(false)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke", results.getErrors().get(0).description());
    }

    @Test
    public void testTwoResultProcessorTwoError() {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(false), new TestResultProcessor(false)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());

        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke", results.getErrors().get(0).description());
    }

    @Test
    public void testTwoResultProcessorResultHasError() {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(false), new TestResultProcessor(false)));

        var results = validationManager.processResults(GResultOf.errors(new ValidationError(ValidationLevel.ERROR) {
                @Override
                public String description() {
                    return "there was an error";
                }
            }), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());

        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke", results.getErrors().get(0).description());
    }

}
