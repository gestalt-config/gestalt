package org.github.gestalt.config.secret.rules;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class RegexSecretCheckerTest {

    @Test
    public void checkIsSecret() {
        Set<String> secretRegex = new HashSet<>();
        secretRegex.add(".*secret.*"); // Sample secret regex
        SecretChecker secretChecker = new RegexSecretChecker(secretRegex);

        Assertions.assertTrue(secretChecker.isSecret("my.secret"));
        Assertions.assertFalse(secretChecker.isSecret("password"));

        secretChecker.addSecret(".*password.*");
        Assertions.assertTrue(secretChecker.isSecret("my.secret"));
        Assertions.assertTrue(secretChecker.isSecret("password"));
    }
}
