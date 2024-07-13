package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.secret.rules.RegexSecretChecker;
import org.github.gestalt.config.secret.rules.SecretChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EncryptedSecretModuleTest {

    private SecretChecker secretChecker;
    private EncryptedSecretModule encryptedSecretModule;

    @BeforeEach
    void setUp() {
        secretChecker = new RegexSecretChecker(Set.of());
        encryptedSecretModule = new EncryptedSecretModule(secretChecker);
    }

    @Test
    void testName() {
        assertEquals("TemporarySecretModule", encryptedSecretModule.name());
    }

    @Test
    void testGetSecretChecker() {
        assertEquals(secretChecker, encryptedSecretModule.getSecretChecker());
    }
}
