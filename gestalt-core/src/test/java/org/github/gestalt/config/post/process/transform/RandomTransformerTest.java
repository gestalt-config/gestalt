package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.UUID;

import static org.github.gestalt.config.entity.ValidationLevel.ERROR;

class RandomTransformerTest {

    private static final int LOOPS = 10000;

    @Test
    void name() {
        RandomTransformer randomTransformer = new RandomTransformer();
        Assertions.assertEquals("random", randomTransformer.name());
    }

    @Test
    void processInvalidMatch() {
        RandomTransformer randomTransformer = new RandomTransformer();
        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "int;5;");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Unable to parse random expression: int;5;, on path: test.path during post process",
            validateOfResults.getErrors().get(0).description());
        Assertions.assertEquals(ERROR, validateOfResults.getErrors().get(0).level());
    }

    @Test
    void processUnsupported() {
        RandomTransformer randomTransformer = new RandomTransformer();
        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "kitten");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Unsupported random post processor: kitten, on path: test.path during post process",
            validateOfResults.getErrors().get(0).description());
        Assertions.assertEquals(ERROR, validateOfResults.getErrors().get(0).level());
    }

    @Test
    void processBadParameter() {
        RandomTransformer randomTransformer = new RandomTransformer();
        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "int(1.5)");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Unable to parse random parameter: int(1.5), on path: test.path during post process, " +
                "with parameters: 1.5, null of type: int",
            validateOfResults.getErrors().get(0).description());
        Assertions.assertEquals(ERROR, validateOfResults.getErrors().get(0).level());
    }

    @Test
    void processByte() {
        RandomTransformer randomTransformer = new RandomTransformer();
        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "byte");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());

        Assertions.assertNotNull(validateOfResults.results());
        byte[] array = Base64.getDecoder().decode(validateOfResults.results());
        Assertions.assertEquals(1, array.length);

        Assertions.assertEquals(0, validateOfResults.getErrors().size());
    }

    @Test
    void processByteWithParameter() {
        RandomTransformer randomTransformer = new RandomTransformer();
        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "byte(3)");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());

        Assertions.assertNotNull(validateOfResults.results());
        byte[] array = Base64.getDecoder().decode(validateOfResults.results());
        Assertions.assertEquals(3, array.length);

        Assertions.assertEquals(0, validateOfResults.getErrors().size());

    }

    @Test
    void processByteWithParameter2Error() {
        RandomTransformer randomTransformer = new RandomTransformer();
        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "byte(3,5)");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertNotNull(validateOfResults.results());
        byte[] array = Base64.getDecoder().decode(validateOfResults.results());
        Assertions.assertEquals(3, array.length);

        Assertions.assertEquals("Invalid number of parameters for type : byte, from key: byte(3,5), on path: test.path " +
            "during post process. Expected 1 parameters", validateOfResults.getErrors().get(0).description());

    }

    @Test
    void processInt() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "int");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            int myValue = Integer.parseInt(validateOfResults.results());
            Assertions.assertTrue(myValue >= Integer.MIN_VALUE && myValue <= Integer.MAX_VALUE);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processIntWith1param() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "int(100)");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            int myValue = Integer.parseInt(validateOfResults.results());
            Assertions.assertTrue(myValue >= 0 && myValue < 100);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processIntWith2param() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "int(-10, 100)");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            int myValue = Integer.parseInt(validateOfResults.results());
            Assertions.assertTrue(myValue >= -10 && myValue < 100);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processLong() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "long");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            long myValue = Long.parseLong(validateOfResults.results());
            Assertions.assertTrue(myValue >= Long.MIN_VALUE && myValue <= Long.MAX_VALUE);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processLongWith1param() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "long(100)");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            long myValue = Long.parseLong(validateOfResults.results());
            Assertions.assertTrue(myValue >= 0 && myValue < 100);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processLongWith2param() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "long(-10, 100)");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            long myValue = Long.parseLong(validateOfResults.results());
            Assertions.assertTrue(myValue >= -10 && myValue < 100);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processFloat() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "float");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            float myValue = Float.parseFloat(validateOfResults.results());
            Assertions.assertTrue(myValue >= 0.0f && myValue <= 1.0f);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processFloatWith1param() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "float(1.1)");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            float myValue = Float.parseFloat(validateOfResults.results());
            Assertions.assertTrue(myValue >= 0 && myValue < 1.1f);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processFloatWith2param() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "float(-1.1, 1.4)");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            float myValue = Float.parseFloat(validateOfResults.results());
            Assertions.assertTrue(myValue >= -1.1f && myValue < 1.4f);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processDouble() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "double");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            double myValue = Double.parseDouble(validateOfResults.results());
            Assertions.assertTrue(myValue >= 0.0d && myValue <= 1.0d);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processDoubleWith1param() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "double(1.1)");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            double myValue = Double.parseDouble(validateOfResults.results());
            Assertions.assertTrue(myValue >= 0 && myValue < 1.1d);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processDoubleWith2param() {
        RandomTransformer randomTransformer = new RandomTransformer();
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "double(-1.1, 1.4)");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            double myValue = Double.parseDouble(validateOfResults.results());
            Assertions.assertTrue(myValue >= -1.1d && myValue < 1.4d);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }
    }

    @Test
    void processBoolean() {
        RandomTransformer randomTransformer = new RandomTransformer();
        float trueCount = 0;
        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "boolean");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(validateOfResults.results());
            boolean myValue = Boolean.parseBoolean(validateOfResults.results());
            trueCount = trueCount + (myValue ? 1 : 0);

            Assertions.assertEquals(0, validateOfResults.getErrors().size());
        }

        Assertions.assertTrue(trueCount / LOOPS > 0.45 && trueCount / LOOPS < 0.55);
    }

    @Test
    void processBooleanError() {
        RandomTransformer randomTransformer = new RandomTransformer();


        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "boolean(5)");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid number of parameters for type : boolean, from key: boolean(5), on path: test.path " +
            "during post process. Expected 0 parameters", validateOfResults.getErrors().get(0).description());

        Boolean.parseBoolean(validateOfResults.results());
    }

    @Test
    void processStringError() {
        RandomTransformer randomTransformer = new RandomTransformer();

        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "string");

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid number of parameters for type : string, from key: string, on path: " +
            "test.path during post process. Must have 1 parameters", validateOfResults.getErrors().get(0).description());
    }

    @Test
    void processString() {
        RandomTransformer randomTransformer = new RandomTransformer();

        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "string(50)");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertEquals(50, validateOfResults.results().length());
        }
    }

    @Test
    void processChar() {
        RandomTransformer randomTransformer = new RandomTransformer();

        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "char");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertEquals(1, validateOfResults.results().length());
        }
    }

    @Test
    void processCharError() {
        RandomTransformer randomTransformer = new RandomTransformer();


        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "char(5)");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid number of parameters for type : char, from key: char(5), on path: test.path " +
            "during post process. Expected 0 parameters", validateOfResults.getErrors().get(0).description());

        Assertions.assertEquals(1, validateOfResults.results().length());
    }

    @Test
    void processUUID() {
        RandomTransformer randomTransformer = new RandomTransformer();

        for (int i = 0; i < LOOPS; i++) {
            ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "uuid");

            Assertions.assertTrue(validateOfResults.hasResults());
            Assertions.assertFalse(validateOfResults.hasErrors());

            Assertions.assertNotNull(UUID.fromString(validateOfResults.results()));
        }
    }

    @Test
    void processUUIDError() {
        RandomTransformer randomTransformer = new RandomTransformer();


        ValidateOf<String> validateOfResults = randomTransformer.process("test.path", "uuid(5)");

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Invalid number of parameters for type : uuid, from key: uuid(5), on path: test.path " +
            "during post process. Expected 0 parameters", validateOfResults.getErrors().get(0).description());

        Assertions.assertNotNull(UUID.fromString(validateOfResults.results()));
    }
}
