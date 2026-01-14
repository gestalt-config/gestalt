package org.github.gestalt.dotenv.transform;

import io.github.cdimascio.dotenv.Dotenv;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.dotenv.config.DotenvModuleConfig;
import org.github.gestalt.dotenv.errors.DotenvErrors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DotenvPropertiesTransformerTest {

    private DotenvPropertiesTransformer transformer;
    private ConfigNodeProcessorConfig processorConfig;
    private GestaltConfig gestaltConfig;

    @BeforeEach
    void setUp() {
        transformer = new DotenvPropertiesTransformer();
        processorConfig = mock(ConfigNodeProcessorConfig.class);
        gestaltConfig = mock(GestaltConfig.class);
        when(processorConfig.getConfig()).thenReturn(gestaltConfig);
    }

    @Test
    void nullKeyShouldReturnValidationError() {
        GResultOf<String> result = transformer.process("path.to.value", null, "${dotEnv:KEY}");

        assertFalse(result.hasResults());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertInstanceOf(ValidationError.InvalidStringSubstitutionPostProcess.class, result.getErrors().get(0));
        assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        assertEquals("Invalid string: ${dotEnv:KEY}, on path: path.to.value in transformer: dotEnv",
            result.getErrors().get(0).description());
    }

    @Test
    void dotenvNotConfiguredShouldReturnDotenvNotConfiguredError() {
        // Ensure no module config is provided
        when(gestaltConfig.getModuleConfig(any())).thenReturn(null);
        transformer.applyConfig(processorConfig);

        GResultOf<String> result = transformer.process("path.to.value", "KEY", "${dotEnv:KEY}");

        assertFalse(result.hasResults());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertInstanceOf(DotenvErrors.DotenvNotConfiguredPostProcess.class, result.getErrors().get(0));
        assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        assertEquals("Dotenv was not configured and added as a Gestalt Module with Gestalt.addModuleConfig for Property found " +
            "for: KEY, on path: path.to.value during post process", result.getErrors().get(0).description());
    }

    @Test
    void dotenvMissingKeyShouldReturnNoDotenvPropertyFoundError() {
        Dotenv dotenv = mock(Dotenv.class);
        when(dotenv.get("KEY")).thenReturn(null);

        DotenvModuleConfig moduleConfig = mock(DotenvModuleConfig.class);
        when(moduleConfig.getDotenv()).thenReturn(dotenv);

        when(gestaltConfig.getModuleConfig(any())).thenReturn(moduleConfig);
        transformer.applyConfig(processorConfig);

        GResultOf<String> result = transformer.process("path.to.value", "KEY", "${dotEnv:KEY}");

        assertFalse(result.hasResults());
        assertTrue(result.hasErrors());
        assertEquals(1, result.getErrors().size());
        assertInstanceOf(DotenvErrors.NoDotenvPropertyFoundPostProcess.class, result.getErrors().get(0));
        assertEquals(ValidationLevel.ERROR, result.getErrors().get(0).level());
        assertEquals("No Dotenv Property found for: KEY, on path: path.to.value during post process", result.getErrors().get(0).description());
    }

    @Test
    void dotenvSuccessShouldReturnValue() {
        Dotenv dotenv = mock(Dotenv.class);
        when(dotenv.get("KEY")).thenReturn("value");

        DotenvModuleConfig moduleConfig = mock(DotenvModuleConfig.class);
        when(moduleConfig.getDotenv()).thenReturn(dotenv);

        when(gestaltConfig.getModuleConfig(any())).thenReturn(moduleConfig);
        transformer.applyConfig(processorConfig);

        GResultOf<String> result = transformer.process("path.to.value", "KEY", "${dotEnv:KEY}");

        assertTrue(result.hasResults());
        assertFalse(result.hasErrors());
        assertEquals("value", result.results());
    }
}

