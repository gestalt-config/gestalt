package org.github.gestalt.config.security.encrypted;

import org.github.gestalt.config.secret.rules.RegexSecretChecker;
import org.github.gestalt.config.secret.rules.SecretChecker;

import java.util.HashSet;
import java.util.Objects;

/**
 * Build a module to manage temporary node access rules. If a path matches the regex, it will be limited to the number of access counts.
 * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
 * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
 * These values will not be cached in the Gestalt Cache and should not be cached by the caller
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class EncryptedSecretModuleBuilder {
    private SecretChecker encryptedSecret = new RegexSecretChecker(new HashSet<>());

    private EncryptedSecretModuleBuilder() {
    }

    /**
     * Static builder.
     *
     * @return new builder
     */
    public static EncryptedSecretModuleBuilder builder() {
        return new EncryptedSecretModuleBuilder();
    }

    /**
     * Set a set of temporary node access rule. If a path matches the regexs, it will be limited to the number of access counts.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param encryptedSecret list of secret SecretChecker with the number of times the temporary node should be accessible
     * @return the builder
     */
    public EncryptedSecretModuleBuilder setEncryptedSecret(SecretChecker encryptedSecret) {
        Objects.requireNonNull(encryptedSecret);

        this.encryptedSecret = encryptedSecret;
        return this;
    }


    /**
     * Set a single temporary node access rule. If a path matches the regex, it will be limited to the 1 access.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param regex If a path matches the regex
     * @return the builder
     */
    public EncryptedSecretModuleBuilder addSecret(String regex) {
        encryptedSecret.addSecret(regex);
        return this;
    }

    public EncryptedSecretModule build() {
        return new EncryptedSecretModule(encryptedSecret);
    }
}
