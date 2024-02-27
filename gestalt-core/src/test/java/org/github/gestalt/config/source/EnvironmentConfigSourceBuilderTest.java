package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentConfigSourceBuilderTest {

    @Test
    void testBuild() throws GestaltException {
        // Given
        String prefix = "TEST";
        boolean ignorePrefixCase = false;
        boolean removePrefix = true;
        boolean failOnErrors = false;

        // When
        EnvironmentConfigSourceBuilder builder = EnvironmentConfigSourceBuilder.builder()
            .setPrefix(prefix)
            .setIgnoreCaseOnPrefix(ignorePrefixCase)
            .setRemovePrefix(removePrefix)
            .setFailOnErrors(failOnErrors);

        // Then
        assertAll(
            () -> assertEquals(prefix, builder.getPrefix()),
            () -> assertEquals(ignorePrefixCase, builder.isIgnoreCaseOnPrefix()),
            () -> assertEquals(removePrefix, builder.isRemovePrefix()),
            () -> assertEquals(failOnErrors, builder.isFailOnErrors())
        );

        assertDoesNotThrow(builder::build);

        var result = builder.build();
        assertEquals(0, result.getConfigReloadStrategies().size());
        assertNotNull(result.getConfigSource());
    }
}

