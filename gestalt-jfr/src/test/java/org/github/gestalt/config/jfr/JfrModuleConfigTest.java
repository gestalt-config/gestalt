package org.github.gestalt.config.jfr;

import org.github.gestalt.config.jfr.builder.JfrModuleConfigBuilder;
import org.github.gestalt.config.jfr.config.JfrModuleConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for JFR module configuration.
 */
class JfrModuleConfigTest {

    @Test
    void testBuilderDefaults() {
        JfrModuleConfig config = JfrModuleConfigBuilder.builder().build();

        assertFalse(config.isIncludePath());
        assertFalse(config.isIncludeClass());
        assertFalse(config.isIncludeOptional());
        assertFalse(config.isIncludeTags());
        assertEquals("Gestalt Config Access", config.getEventLabel());
    }

    @Test
    void testBuilderCustomSettings() {
        JfrModuleConfig config = JfrModuleConfigBuilder.builder()
            .setIncludePath(true)
            .setIncludeClass(true)
            .setIncludeOptional(true)
            .setIncludeTags(true)
            .setEventLabel("Custom Event")
            .build();

        assertTrue(config.isIncludePath());
        assertTrue(config.isIncludeClass());
        assertTrue(config.isIncludeOptional());
        assertTrue(config.isIncludeTags());
        assertEquals("Custom Event", config.getEventLabel());
    }

    @Test
    void testModuleConfig() {
        JfrModuleConfig config = JfrModuleConfigBuilder.builder().build();
        assertEquals("jfr", config.name());
    }
}
