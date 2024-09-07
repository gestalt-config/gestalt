package org.github.gestalt.config.cdi;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GestaltConfigExceptionTest {

    @Test
    public void exceptionTest() {
        GestaltConfigException gestaltConfigException = new GestaltConfigException("failed", "my.properties");

        Assertions.assertEquals("my.properties", gestaltConfigException.getConfigPropertyName());
        Assertions.assertEquals("failed", gestaltConfigException.getMessage());
    }

    @Test
    public void exceptionTestWithNestedException() {
        GestaltConfigException gestaltConfigException =
            new GestaltConfigException("failed", "my.properties", new GestaltException("failed"));

        Assertions.assertEquals("my.properties", gestaltConfigException.getConfigPropertyName());
        Assertions.assertEquals("failed", gestaltConfigException.getMessage());
    }

    @Test
    public void exceptionTestWithNestedException2() {
        GestaltConfigException gestaltConfigException =
            new GestaltConfigException("failed", new GestaltException("failed"));

        Assertions.assertNull(gestaltConfigException.getConfigPropertyName());
        Assertions.assertEquals("failed", gestaltConfigException.getMessage());
    }
}
