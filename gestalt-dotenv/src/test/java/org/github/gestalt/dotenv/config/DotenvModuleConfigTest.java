
package org.github.gestalt.dotenv.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DotenvModuleConfigTest {

    @Test
    void constructorShouldStoreDotenvAndReturnName() {
        Dotenv dotenv = mock(Dotenv.class);
        DotenvModuleConfig config = new DotenvModuleConfig(dotenv);

        assertNotNull(config);
        assertEquals("dotEnv", config.name());
        assertSame(dotenv, config.getDotenv());
    }

    @Test
    void constructorShouldThrowWhenDotenvNull() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> new DotenvModuleConfig(null));
        assertTrue(ex.getMessage().contains("Dotenv instance should not be null"));
    }
}

