package org.github.gestalt.config.vault.config;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VaultBuilderTest {

    @Test
    public void vaultConfig() throws VaultException, GestaltConfigurationException {
        final VaultConfig config = new VaultConfig()
            .address("http://127.0.0.1:1235")
            .token("abcdef")
            .build();

        VaultBuilder vaultBuilder = VaultBuilder.builder().setVaultConfig(config);
        VaultModuleConfig vaultModuleConfig = vaultBuilder.build();

        Assertions.assertNotNull(vaultBuilder.getVault());
        Assertions.assertNotNull(vaultBuilder.getVaultConfig());
        Assertions.assertNotNull(vaultModuleConfig.getVault());
        Assertions.assertEquals("vault", vaultModuleConfig.name());
    }

    @Test
    public void vault() throws VaultException, GestaltConfigurationException {
        final VaultConfig config = new VaultConfig()
            .address("http://127.0.0.1:1235")
            .token("abcdef")
            .build();

        Vault vault = Vault.create(config);

        VaultBuilder vaultBuilder = VaultBuilder.builder().setVault(vault);
        VaultModuleConfig vaultModuleConfig = vaultBuilder.build();
        Assertions.assertNotNull(vaultModuleConfig.getVault());
    }

    @Test
    public void vaultNull() {
        Assertions.assertThrows(GestaltConfigurationException.class, () -> VaultBuilder.builder().build());
    }
}
