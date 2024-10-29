package org.github.gestalt.config.secret.rules;

import org.github.gestalt.config.metadata.IsSecretMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecretConcealerManagerTest {
    private SecretConcealerManager secretConcealer;

    @BeforeEach
    void setUp() {
        Set<String> secretRegex = new HashSet<>();
        secretRegex.add(".*secret.*"); // Sample secret regex
        secretConcealer = new SecretConcealerManager(secretRegex, it -> "***");
    }

    @Test
    void testGetMask() {
        assertEquals("***", secretConcealer.getMask().obfuscator("test"));
    }

    @Test
    void testAddSecretRule() {
        secretConcealer.addSecretRule(".*password.*");
        assertEquals("***", secretConcealer.getMask().obfuscator("test")); // Ensure mask remains unchanged
    }


    @Test
    void testConcealSecretMask() {
        String path = "path.to.secret";
        String value = "this is a secret value";
        assertEquals("***", secretConcealer.concealSecret(path, value, Map.of()));
    }

    @Test
    void testConcealSecretMaskRegexCheckerConstructor() {

        Set<String> secretRegex = new HashSet<>();
        secretRegex.add(".*secret.*"); // Sample secret regex
        SecretConcealer secretConcealerLocal = new SecretConcealerManager(new RegexSecretChecker(secretRegex), it -> "***");

        String path = "path.to.secret";
        String value = "this is a secret value";
        assertEquals("***", secretConcealerLocal.concealSecret(path, value, Map.of()));
    }

    @Test
    void testConcealSecretNoMask() {
        String path = "path.to.public";
        String value = "this is a public value";
        assertEquals(value, secretConcealer.concealSecret(path, value, Map.of()));
    }

    @Test
    void testConcealSecretWithSecretRule() {
        secretConcealer.addSecretRule("password");
        String path = "path.to.password";
        String value = "this is a password value";
        assertEquals("***", secretConcealer.concealSecret(path, value, Map.of()));
    }

    @Test
    void testConcealSecretInMiddleWithSecretRule() {
        secretConcealer.addSecretRule(".*password.*");
        String path = "path.passwords.database";
        String value = "this is a password value";
        assertEquals("***", secretConcealer.concealSecret(path, value, Map.of()));
    }

    @Test
    void testConcealSecretInMiddlePartialMatchWithSecretRule() {
        secretConcealer.addSecretRule("pass");
        String path = "path.password.database";
        String value = "this is a password value";
        assertEquals("***", secretConcealer.concealSecret(path, value, Map.of()));
    }

    @Test
    void testConcealSecretWithSecretRuleNoMatch() {
        secretConcealer.addSecretRule("password");
        String path = "path.to.username";
        String value = "this is a username value";
        assertEquals(value, secretConcealer.concealSecret(path, value, Map.of()));
    }

    @Test
    void testConcealSecretMaskWithMetadata() {
        String path = "path.to.data";
        String value = "this is a secret value";
        assertEquals("***", secretConcealer.concealSecret(path, value,
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(true)))));
    }

    @Test
    void testConcealSecretMaskWithMetadataNotMatch() {
        String path = "path.to.data";
        String value = "this is a secret value";
        assertEquals("this is a secret value", secretConcealer.concealSecret(path, value,
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false)))));
    }

    @Test
    void testConcealSecretMaskWithMetadataMultiMatch() {
        String path = "path.to.data";
        String value = "this is a secret value";
        assertEquals("***", secretConcealer.concealSecret(path, value,
            Map.of(IsSecretMetadata.SECRET, List.of(new IsSecretMetadata(false), new IsSecretMetadata(true)))));
    }
}
