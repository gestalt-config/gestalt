package org.github.gestalt.config.security.temporary;

import org.github.gestalt.config.secret.rules.RegexSecretChecker;
import org.github.gestalt.config.secret.rules.SecretChecker;
import org.github.gestalt.config.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Build a module to manage temporary node access rules. If a path matches the regex, it will be limited to the number of access counts.
 * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
 * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
 * These values will not be cached in the Gestalt Cache and should not be cached by the caller
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class TemporarySecretModuleBuilder {
    private List<Pair<SecretChecker, Integer>> secretCounts = new ArrayList<>();

    private TemporarySecretModuleBuilder() {
    }

    /**
     * Static builder.
     *
     * @return new builder
     */
    public static TemporarySecretModuleBuilder builder() {
        return new TemporarySecretModuleBuilder();
    }

    /**
     * Set a set of temporary node access rule. If a path matches the regexs, it will be limited to the number of access counts.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param secretCounts list of secret SecretChecker with the number of times the temporary node should be accessible
     * @return the builder
     */
    public TemporarySecretModuleBuilder setSecretCounts(List<Pair<SecretChecker, Integer>> secretCounts) {
        Objects.requireNonNull(secretCounts);

        this.secretCounts = secretCounts;
        return this;
    }

    /**
     * add a set of temporary node access rule. If a path matches the regexs, it will be limited to the number of access counts.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param secretCounts list of secret SecretChecker with the number of times the temporary node should be accessible
     * @return the builder
     */
    public TemporarySecretModuleBuilder addSecretCounts(List<Pair<SecretChecker, Integer>> secretCounts) {
        this.secretCounts.addAll(secretCounts);
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
    public TemporarySecretModuleBuilder addSecret(String regex) {
        secretCounts.add(new Pair<>(new RegexSecretChecker(Set.of(regex)), 1));
        return this;
    }

    /**
     * Set a single temporary node access rule. If a path matches the regex, it will be limited to the number of access counts.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param secretRegex If a path matches the regex
     * @param accessCount After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * @return the builder
     */
    public TemporarySecretModuleBuilder addSecretWithCount(String secretRegex, Integer accessCount) {
        secretCounts.add(new Pair<>(new RegexSecretChecker(Set.of(secretRegex)), accessCount));
        return this;
    }

    /**
     * Set a single temporary node access rule. If a path matches the regex, it will be limited to the number of access counts.
     * After the value has been retrieved more than accessCount the original value will be released and GC'ed.
     * It may be a while till the secret is GC'ed and during that time it will still be retained in memory.
     * These values will not be cached in the Gestalt Cache and should not be cached by the caller
     *
     * @param secretChecker the secretChecker used to identify the secret, this is the path of the secret.
     * @param count the number of times the secret matching the regex can be accessed
     * @return the builder
     */
    public TemporarySecretModuleBuilder addSecretWithCount(SecretChecker secretChecker, Integer count) {
        secretCounts.add(new Pair<>(secretChecker, count));
        return this;
    }

    public TemporarySecretModule build() {
        return new TemporarySecretModule(secretCounts);
    }
}
