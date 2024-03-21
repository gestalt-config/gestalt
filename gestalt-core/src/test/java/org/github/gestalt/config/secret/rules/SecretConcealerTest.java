package org.github.gestalt.config.secret.rules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SecretConcealerTest {
    private SecretConcealer secretConcealer;

    @BeforeEach
    void setUp() {
        Set<String> secretRegex = new HashSet<>();
        secretRegex.add(".*secret.*"); // Sample secret regex
        secretConcealer = new SecretConcealer(secretRegex, "***");
    }

    @Test
    void testGetMask() {
        assertEquals("***", secretConcealer.getMask());
    }

    @Test
    void testAddSecretRule() {
        secretConcealer.addSecretRule(".*password.*");
        assertEquals("***", secretConcealer.getMask()); // Ensure mask remains unchanged
    }

    @Test
    void testConcealSecretMask() {
        String path = "path.to.secret";
        String value = "this is a secret value";
        assertEquals("***", secretConcealer.concealSecret(path, value));
    }

    @Test
    void testConcealSecretNoMask() {
        String path = "path.to.public";
        String value = "this is a public value";
        assertEquals(value, secretConcealer.concealSecret(path, value));
    }

    @Test
    void testConcealSecretWithSecretRule() {
        secretConcealer.addSecretRule("password");
        String path = "path.to.password";
        String value = "this is a password value";
        assertEquals("***", secretConcealer.concealSecret(path, value));
    }

    @Test
    void testConcealSecretInMiddleWithSecretRule() {
        secretConcealer.addSecretRule(".*password.*");
        String path = "path.passwords.database";
        String value = "this is a password value";
        assertEquals("***", secretConcealer.concealSecret(path, value));
    }

    @Test
    void testConcealSecretInMiddlePartialMatchWithSecretRule() {
        secretConcealer.addSecretRule("pass");
        String path = "path.password.database";
        String value = "this is a password value";
        assertEquals("***", secretConcealer.concealSecret(path, value));
    }

    @Test
    void testConcealSecretWithSecretRuleNoMatch() {
        secretConcealer.addSecretRule("password");
        String path = "path.to.username";
        String value = "this is a username value";
        assertEquals(value, secretConcealer.concealSecret(path, value));
    }
}
