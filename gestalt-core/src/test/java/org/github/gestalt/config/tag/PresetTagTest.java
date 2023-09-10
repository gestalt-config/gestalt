package org.github.gestalt.config.tag;

import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Colin Redmond (c) 2023.
 */
class PresetTagTest {

    @Test
    void profile() throws GestaltException {
        Assertions.assertEquals(Tags.of("profile", "test"), PresetTag.profile("test"));
    }

    @Test
    void profiles() throws GestaltException {
        Assertions.assertEquals(Tags.of("profile", "test"),
            PresetTag.profiles("test"));

        Assertions.assertEquals(Tags.of("profile", "test", "profile", "cloud"),
            PresetTag.profiles("test", "cloud"));
    }

    @Test
    void environment() throws GestaltException {
        Assertions.assertEquals(Tags.of("environment", "dev"), PresetTag.environment("dev"));
    }

    @Test
    void environments() throws GestaltException {
        Assertions.assertEquals(Tags.of("environment", "dev"),
            PresetTag.environments("dev"));

        Assertions.assertEquals(Tags.of("environment", "dev", "environment", "stage"),
            PresetTag.environments("dev", "stage"));
    }
}
