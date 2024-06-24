package org.github.gestalt.config.security.temporary;

import org.github.gestalt.config.secret.rules.RegexSecretChecker;
import org.github.gestalt.config.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class TemporarySecretModuleBuilderTest {

    @Test
    public void testBuilder() {
        TemporarySecretModule tempSecrets = TemporarySecretModuleBuilder.builder()
            .addSecretWithCount("secret", 1)
            .addSecretWithCount(new RegexSecretChecker("password"), 2)
            .addSecretCounts(List.of(new Pair<>(new RegexSecretChecker("cert"), 1)))
            .build();

        Assertions.assertEquals("TemporarySecretModule", tempSecrets.name());

        Assertions.assertEquals(3, tempSecrets.getSecretCounts().size());
        Assertions.assertTrue(tempSecrets.getSecretCounts().get(0).getFirst().isSecret("secret"));
        Assertions.assertEquals(1, tempSecrets.getSecretCounts().get(0).getSecond());

        Assertions.assertTrue(tempSecrets.getSecretCounts().get(1).getFirst().isSecret("password"));
        Assertions.assertEquals(2, tempSecrets.getSecretCounts().get(1).getSecond());

        Assertions.assertTrue(tempSecrets.getSecretCounts().get(2).getFirst().isSecret("cert"));
        Assertions.assertEquals(1, tempSecrets.getSecretCounts().get(2).getSecond());
    }

    @Test
    public void testBuilderSet() {
        TemporarySecretModule tempSecrets = TemporarySecretModuleBuilder.builder()
            .addSecretWithCount("secret", 1)
            .addSecretWithCount(new RegexSecretChecker("password"), 2)
            .setSecretCounts(List.of(new Pair<>(new RegexSecretChecker("cert"), 1)))
            .build();

        Assertions.assertEquals(1, tempSecrets.getSecretCounts().size());

        Assertions.assertTrue(tempSecrets.getSecretCounts().get(0).getFirst().isSecret("cert"));
        Assertions.assertEquals(1, tempSecrets.getSecretCounts().get(0).getSecond());
    }

    @Test
    public void testBuilderStrings() {
        TemporarySecretModule tempSecrets = TemporarySecretModuleBuilder.builder()
            .addSecret("secret")
            .build();

        Assertions.assertEquals("TemporarySecretModule", tempSecrets.name());

        Assertions.assertEquals(1, tempSecrets.getSecretCounts().size());
        Assertions.assertTrue(tempSecrets.getSecretCounts().get(0).getFirst().isSecret("secret"));
        Assertions.assertEquals(1, tempSecrets.getSecretCounts().get(0).getSecond());
    }
}
