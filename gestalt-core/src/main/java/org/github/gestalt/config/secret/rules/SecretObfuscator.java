package org.github.gestalt.config.secret.rules;

/**
 * Specifies how to obscure a secret. Takes a value and obfuscates it.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface SecretObfuscator {
    /**
     * Takes a value and obfuscates it.
     *
     * @param value the value to obfuscates
     * @return the value to obfuscated
     */
    String obfuscator(String value);
}
