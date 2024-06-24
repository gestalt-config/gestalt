package org.github.gestalt.config.secret.rules;

import java.util.Set;

/**
 * Contains all the rules on how to conceal a secret, then apply them to a value.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class SecretConcealerManager implements SecretConcealer {
    private final SecretObfuscator obfuscator;

    private final SecretChecker secretChecker;

    public SecretConcealerManager(Set<String> secretRegex, SecretObfuscator obfuscator) {
        this.secretChecker = new RegexSecretChecker(secretRegex);
        this.obfuscator = obfuscator;
    }

    public SecretConcealerManager(SecretChecker secretChecker, SecretObfuscator obfuscator) {
        this.secretChecker = secretChecker;
        this.obfuscator = obfuscator;
    }

    public SecretObfuscator getMask() {
        return obfuscator;
    }

    public void addSecretRule(String rule) {
        secretChecker.addSecret(rule);
    }

    /**
     * returns the value that is concealed if it is a secret. Otherwise, returns the value.
     *
     * @param path path of the value
     * @param value value we are checking if we need to conceal.
     * @return the value that is concealed if it is a secret.
     */
    @Override
    public String concealSecret(String path, String value) {
        if (secretChecker.isSecret(path)) {
            return obfuscator.obfuscator(value);
        } else {
            return value;
        }
    }
}
