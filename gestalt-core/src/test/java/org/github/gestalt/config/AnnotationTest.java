package org.github.gestalt.config;

import org.github.gestalt.config.annotations.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnnotationTest {

    private static class TestClass {
        @Config(path = "value", defaultVal = "10")
        public int myVal = 100;

        @Config(defaultVal = "20")
        public int myVal2 = 200;

        @Config(path = "value3")
        public int myVal3 = 300;
    }

    @Test

    public void testAnnotation() throws NoSuchFieldException {
        TestClass test = new TestClass();
        Assertions.assertEquals(100, test.myVal);
        Assertions.assertEquals(200, test.myVal2);
        Assertions.assertEquals(300, test.myVal3);

        Config annotation = test.getClass().getDeclaredField("myVal").getAnnotation(Config.class);

        Assertions.assertEquals("value", annotation.path());
        Assertions.assertEquals("10", annotation.defaultVal());

        annotation = test.getClass().getDeclaredField("myVal2").getAnnotation(Config.class);

        Assertions.assertEquals("", annotation.path());
        Assertions.assertEquals("20", annotation.defaultVal());

        annotation = test.getClass().getDeclaredField("myVal3").getAnnotation(Config.class);

        Assertions.assertEquals("value3", annotation.path());
        Assertions.assertEquals("", annotation.defaultVal());
    }
}
