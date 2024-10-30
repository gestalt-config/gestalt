package org.github.gestalt.config.security.temporary;

import org.github.gestalt.config.entity.GestaltModuleConfig;
import org.github.gestalt.config.secret.rules.SecretChecker;
import org.github.gestalt.config.utils.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the TemporarySecretConfigNodeProcessor. It allows us to specify the secret and the number of times it is accessible.
 * Once the leaf value has been read the accessCount times, it will release the secret value of the node by setting it to null.
 * Eventually the secret node should be garbage collected. but while waiting for GC it may still be found in memory.
 * These values will not be cached in the Gestalt Cache and should not be cached by the caller
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class TemporarySecretModule implements GestaltModuleConfig {

    private final List<Pair<SecretChecker, Integer>> secretCounts = new ArrayList<>();

    public TemporarySecretModule(List<Pair<SecretChecker, Integer>> secretCounts) {
        this.secretCounts.addAll(secretCounts);
    }

    @Override
    public String name() {
        return "TemporarySecretModule";
    }

    public List<Pair<SecretChecker, Integer>> getSecretCounts() {
        return secretCounts;
    }

    public void addSecretCounts(List<Pair<SecretChecker, Integer>> addSecretCounts) {
        secretCounts.addAll(addSecretCounts);
    }
}
