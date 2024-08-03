package org.github.gestalt.config.vault;

import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.LogicalResponse;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.processor.config.ConfigNodeProcessorConfig;
import org.github.gestalt.config.vault.config.VaultBuilder;
import org.github.gestalt.config.vault.config.VaultModuleConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("rawtypes")
@Testcontainers
class VaultSecretTransformerTest {

    private static final String VAULT_TOKEN = "my-root-token";

    @Container
    private static final VaultContainer vaultContainer = new VaultContainer("hashicorp/vault:1.13.0").withVaultToken(VAULT_TOKEN);

    private static Vault vault;

    @BeforeAll
    public static void setupVault() throws VaultException {
        vaultContainer.start();

        final VaultConfig config = new VaultConfig()
            .address("http://" + vaultContainer.getHost() + ":" + vaultContainer.getFirstMappedPort())
            .token(VAULT_TOKEN)
            .build();

        vault = Vault.create(config);

        final Map<String, Object> secrets = new HashMap<>();
        secrets.put("value", "hello world");
        secrets.put("other_value", "another world");

        // Write operation
        final LogicalResponse writeResponse = vault.logical().write("secret/hello", secrets);
        Assertions.assertEquals(200, writeResponse.getRestResponse().getStatus());
    }

    @AfterAll
    public static void tearDown() {
        vaultContainer.close();
    }

    @Test
    void processWithSecretClientProvided() throws GestaltConfigurationException {
        VaultModuleConfig vaultModuleConfig = VaultBuilder.builder().setVault(vault).build();

        VaultSecretTransformer transform = new VaultSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(vaultModuleConfig);
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        var results = transform.process("test", "secret/hello:value", "vault:secret/hello:value");

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals("hello world", results.results());
        Assertions.assertEquals("vault", transform.name());

    }

    @Test
    void processInvalidSecretKeyFormat() throws GestaltConfigurationException {
        VaultModuleConfig vaultModuleConfig = VaultBuilder.builder().setVault(vault).build();

        VaultSecretTransformer transform = new VaultSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(vaultModuleConfig);
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        var results = transform.process("test", "secret/hello&value", "vault:secret/hello&value");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Vault Secret must be in the format secretPath:SecretKey received ${vault:secret/hello&value} " +
            "with parts [secret/hello&value], on the path: test", results.getErrors().get(0).description());

    }

    @Test
    void processMissingSecretKey() throws GestaltConfigurationException {
        VaultModuleConfig vaultModuleConfig = VaultBuilder.builder().setVault(vault).build();

        VaultSecretTransformer transform = new VaultSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(vaultModuleConfig);
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        var results = transform.process("test", "secret:value", "vault:secret/hello&value");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Vault secret does not have the key value, on the path: test with substitution " +
            "vault:secret/hello&value", results.getErrors().get(0).description());

    }

    @Test
    void processMissingSecretKey2() throws GestaltConfigurationException {
        VaultModuleConfig vaultModuleConfig = VaultBuilder.builder().setVault(vault).build();

        VaultSecretTransformer transform = new VaultSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(vaultModuleConfig);
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        var results = transform.process("test", "secret/hello:text", "vault:secret/hello&value");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Vault secret/hello does not have the key text, on the path: test with substitution " +
            "vault:secret/hello&value", results.getErrors().get(0).description());

    }

    @Test
    void processNullSecretKey() throws GestaltConfigurationException {
        VaultModuleConfig vaultModuleConfig = VaultBuilder.builder().setVault(vault).build();

        VaultSecretTransformer transform = new VaultSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(vaultModuleConfig);
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        var results = transform.process("test", null, "vault:");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Invalid string: vault:, on path: test in transformer: vault",
            results.getErrors().get(0).description());

    }

    @Test
    void noVaultConfigSet() throws GestaltConfigurationException {

        VaultModuleConfig vaultModuleConfig = VaultBuilder.builder().setVault(vault).build();

        VaultSecretTransformer transform = new VaultSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(vaultModuleConfig);

        var results = transform.process("test", "secret/hello:text", "vault:secret/hello&value");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("VaultModuleConfig has not been registered. Register by creating a VaultBuilder " +
            "then registering the VaultBuilder.build() results with the Gestalt Builder.addModuleConfig(). If you wish to use the " +
            "vault module with string substitution ${vault:secret/hello&value} on the path: test",
            results.getErrors().get(0).description());

    }

    @Test
    void noVaultApplyConfigNoModule() {

        VaultSecretTransformer transform = new VaultSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);

        transform.applyConfig(config);
    }

    @Test
    void noVaultApplyConfigNoVault() {

        VaultModuleConfig vaultModuleConfig = new VaultModuleConfig(null);

        VaultSecretTransformer transform = new VaultSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(vaultModuleConfig);
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);

        transform.applyConfig(config);
    }

    @Test
    void processError() throws GestaltConfigurationException, VaultException {

        final VaultConfig vConfig = new VaultConfig()
            .address("http://localhost:34234324")
            .token("abcdef")
            .build();

        Vault vault = Vault.create(vConfig);
        VaultModuleConfig vaultModuleConfig = VaultBuilder.builder().setVault(vault).build();

        VaultSecretTransformer transform = new VaultSecretTransformer();
        GestaltConfig gestaltConfig = new GestaltConfig();
        gestaltConfig.registerModuleConfig(vaultModuleConfig);
        ConfigNodeProcessorConfig config = new ConfigNodeProcessorConfig(gestaltConfig, null, null, null, null);
        transform.applyConfig(config);

        var results = transform.process("test", "secret:value", "vault:secret/hello&value");

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals(ValidationLevel.ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("Exception thrown while loading Vault secret: vault:secret/hello&value, " +
            "on path: test in transformer: vault, with message: io.github.jopenlibs.vault.rest.RestException: " +
            "java.lang.IllegalArgumentException: port out of range:34234324",
            results.getErrors().get(0).description());

    }

}
