package org.github.gestalt.config.processor.config.transform;


import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class Distribution100TransformerTest {

    @Test
    public void nameTest() {
        Distribution100Transformer transform = new Distribution100Transformer(10);

        Assertions.assertEquals("dist100", transform.name());
    }

    @Test
    public void dist() {
        Distribution100Transformer transform = new Distribution100Transformer(10);

        GResultOf<String> result = transform.process("path", "10:red,50:green,blue", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals("green", result.results());

        result = transform.process("path", "10:red,50:green,blue", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals("blue", result.results());

        result = transform.process("path", "10:red,50:green,blue", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals("blue", result.results());
    }

    @Test
    public void distSpaces() {
        Distribution100Transformer transform = new Distribution100Transformer(10);

        GResultOf<String> result = transform.process("path", "10 : red ,50 : green,blue ", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals("green", result.results());

        result = transform.process("path", "10:red,50:green,blue", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals("blue", result.results());

        result = transform.process("path", "10:red,50:green,blue", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals("blue", result.results());
    }

    @Test
    public void distMultipleDefaults() {
        Distribution100Transformer transform = new Distribution100Transformer(10);

        GResultOf<String> result = transform.process("path", "10 : red ,50 : green,blue,yellow ", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.WARN, result.getErrors().get(0).level());
        Assertions.assertEquals("Multiple default values found in Dist100 with : 10 : red ,50 : green,blue,yellow , on path: path",
            result.getErrors().get(0).description());

        Assertions.assertEquals("green", result.results());
    }

    @Test
    public void distNullKey() {
        Distribution100Transformer transform = new Distribution100Transformer(10);

        GResultOf<String> result = transform.process("path", null, "");

        Assertions.assertFalse(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        Assertions.assertEquals("Invalid string: null, on path: path in transformer: dist100",
            result.getErrors().get(0).description());
    }

    @Test
    public void distDifferentOrders() {
        Distribution100Transformer transform = new Distribution100Transformer(10);

        GResultOf<String> result = transform.process("path", "10:red,50:green,blue", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals("green", result.results());

        transform = new Distribution100Transformer(10);
        result = transform.process("path", "50:green,10:red,blue", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals("green", result.results());

        transform = new Distribution100Transformer(10);
        result = transform.process("path", "blue,10:red,50:green", "");

        Assertions.assertTrue(result.hasResults());
        Assertions.assertFalse(result.hasErrors());

        Assertions.assertEquals("green", result.results());
    }

}
