package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.secret.rules.SecretChecker;

import java.util.Set;

/**
 * Configuration for the TemporarySecretConfigNodeProcessor. It allows us to specify the secret and the number of times it is accessible.
 * Once the leaf value has been read the accessCount times, it will release the secret value of the node by setting it to null.
 * Eventually the secret node should be garbage collected. but while waiting for GC it may still be found in memory.
 * These values will not be cached in the Gestalt Cache and should not be cached by the caller
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class EncryptedSecretModule implements GestaltModuleConfig {

    private final SecretChecker encryptedSecret;

    public EncryptedSecretModule(SecretChecker encryptedSecret) {
        this.encryptedSecret = encryptedSecret;
    }

    @Override
    public String name() {
        return "TemporarySecretModule";
    }

    public SecretChecker getSecretChecker() {
        return encryptedSecret;
    }

    public void addEncryptedSecret(Set<String> encryptedSecrets) {
        encryptedSecrets.stream().forEach(encryptedSecret::addSecret);
    }
}
