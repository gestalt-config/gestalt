package org.github.gestalt.config.secret.rules;

public interface SecretChecker {

    /**
     * Checks if the given value matches any of the secret patterns.
     *
     * @param value the value to be checked
     * @return true if the value matches any secret pattern, false otherwise
     */
    boolean isSecret(String value);

    /**
     * Adds a new Secret to the checker.
     *
     * @param rule the new secret to search for
     */
    void addSecret(String rule);
}
