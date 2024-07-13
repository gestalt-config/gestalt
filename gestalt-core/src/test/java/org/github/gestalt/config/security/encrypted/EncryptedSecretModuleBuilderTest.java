package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.secret.rules.RegexSecretChecker;
import org.github.gestalt.config.secret.rules.SecretChecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EncryptedSecretModuleBuilderTest {

    private SecretChecker secretChecker;

    @BeforeEach
    void setUp() {
        secretChecker = new RegexSecretChecker(Set.of());
    }

    @Test
    void testBuilderCreatesInstance() {
        EncryptedSecretModuleBuilder builder = EncryptedSecretModuleBuilder.builder();
        assertNotNull(builder);
    }

    @Test
    void testSetEncryptedSecret() {
        EncryptedSecretModuleBuilder builder = EncryptedSecretModuleBuilder.builder();
        builder.setEncryptedSecret(secretChecker);
        EncryptedSecretModule module = builder.build();

        assertEquals(secretChecker, module.getSecretChecker());
    }

    @Test
    void testAddSecret() {
        String regex = ".*secret.*";
        EncryptedSecretModuleBuilder builder = EncryptedSecretModuleBuilder.builder();
        SecretChecker regexSecretChecker = new RegexSecretChecker(new HashSet<>());
        builder.setEncryptedSecret(regexSecretChecker);
        builder.addSecret(regex);
        EncryptedSecretModule module = builder.build();

        assertEquals(regexSecretChecker, module.getSecretChecker());
        Assertions.assertTrue(regexSecretChecker.isSecret("secret"));
        Assertions.assertFalse(regexSecretChecker.isSecret("blue"));
    }

    @Test
    void testBuild() {
        EncryptedSecretModuleBuilder builder = EncryptedSecretModuleBuilder.builder();
        builder.setEncryptedSecret(secretChecker);
        EncryptedSecretModule module = builder.build();

        assertNotNull(module);
        assertEquals(secretChecker, module.getSecretChecker());
    }
}
