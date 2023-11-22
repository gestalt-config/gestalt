package org.github.gestalt.config.vault.config;

import io.github.jopenlibs.vault.Vault;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import io.github.jopenlibs.vault.VaultConfig;

/**
 * Builder for creating Vault specific configuration.
 * You can either provide the VaultConfig and the builder will create the client,
 * or you can provide a Vault client yourself.
 *
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class VaultBuilder {
    private VaultConfig vaultConfig;

    private Vault vault;

    private VaultBuilder() {

    }


    /**
     * Create a builder to create the Vault config.
     *
     * @return a builder to create the Vault config.
     */
    public static VaultBuilder builder() {
        return new VaultBuilder();
    }

    public VaultConfig getVaultConfig() {
        return vaultConfig;
    }

    public VaultBuilder setVaultConfig(VaultConfig vaultConfig) {
        this.vaultConfig = vaultConfig;
        return this;
    }

    public Vault getVault() {
        return vault;
    }

    public VaultBuilder setVault(Vault vault) {
        this.vault = vault;
        return this;
    }

    public VaultModuleConfig build() throws GestaltConfigurationException {
        if (vaultConfig == null && vault == null) {
            throw new GestaltConfigurationException("VaultModuleConfig was built but one of the vaultConfig " +
                "or the vault client must be provided");
        }

        if (vault == null) {
            vault = Vault.create(vaultConfig);
        }

        return new VaultModuleConfig(vault);
    }
}
