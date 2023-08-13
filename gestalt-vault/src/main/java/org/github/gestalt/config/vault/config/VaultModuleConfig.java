package org.github.gestalt.config.vault.config;

import io.github.jopenlibs.vault.Vault;
import org.github.gestalt.config.entity.GestaltModuleConfig;


/**
 * Vault specific configuration.
 * Provides a Vault client to the Vault transformer.
 *
 * @author Colin Redmond (c) 2023.
 */
public class VaultModuleConfig implements GestaltModuleConfig {

    private final Vault vault;

    public VaultModuleConfig(Vault vault) {
        this.vault = vault;
    }

    @Override
    public String name() {
        return "vault";
    }

    public Vault getVault() {
        return vault;
    }
}
