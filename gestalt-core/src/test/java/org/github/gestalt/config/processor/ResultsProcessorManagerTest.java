package org.github.gestalt.config.processor;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.processor.result.ResultsProcessorManager;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ResultsProcessorManagerTest {

    @Test
    public void testResultProcessorOk() throws GestaltException {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(true)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("test", results.results());
    }

    @Test
    public void testTwoResultProcessorOk() throws GestaltException {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(true), new TestResultProcessor2(true)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("test", results.results());
    }

    @Test
    public void testResultProcessorOneError() throws GestaltException {
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
    public void testTwoResultProcessorOneError() throws GestaltException {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(true), new TestResultProcessor2(false)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke2", results.getErrors().get(0).description());
    }

    @Test
    public void testTwoResultProcessorOneError2() throws GestaltException {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(false), new TestResultProcessor2(true)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke", results.getErrors().get(0).description());
    }

    @Test
    public void testTwoResultProcessorBothError() throws GestaltException {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor(false), new TestResultProcessor2(false)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke2", results.getErrors().get(0).description());
    }

    @Test
    public void testTwoResultProcessorBothErrorDifferentOrder() throws GestaltException {
        var validationManager = new ResultsProcessorManager(List.of(new TestResultProcessor2(false), new TestResultProcessor(false)));

        var results = validationManager.processResults(GResultOf.result("test"), "my.path", false, null,
            TypeCapture.of(String.class), Tags.of());

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("something broke2", results.getErrors().get(0).description());
    }

    @Test
    public void testTwoResultProcessorTwoError() throws GestaltException {
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
    public void testTwoResultProcessorResultHasError() throws GestaltException {
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

    @Test
    public void testTwoResultProcessorResultHasError2() throws GestaltException {
        var validationManager = new ResultsProcessorManager(List.of(
            new TestResultProcessor(false), new TestResultProcessor(false), new TestResultProcessor3(false)));

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
