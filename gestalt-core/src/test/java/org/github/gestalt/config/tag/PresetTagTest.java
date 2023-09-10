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
        Assertions.assertEquals(Tags.of("profile", "test"), Tags.profile("test"));
    }

    @Test
    void profiles() throws GestaltException {
        Assertions.assertEquals(Tags.of("profile", "test"),
            Tags.profiles("test"));

        Assertions.assertEquals(Tags.of("profile", "test", "profile", "cloud"),
            Tags.profiles("test", "cloud"));
    }

    @Test
    void environment() throws GestaltException {
        Assertions.assertEquals(Tags.of("environment", "dev"), Tags.environment("dev"));
    }

    @Test
    void environments() throws GestaltException {
        Assertions.assertEquals(Tags.of("environment", "dev"),
            Tags.environments("dev"));

        Assertions.assertEquals(Tags.of("environment", "dev", "environment", "stage"),
            Tags.environments("dev", "stage"));
    }
}
