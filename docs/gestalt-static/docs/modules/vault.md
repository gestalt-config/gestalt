---
sidebar_position: 10
---

## Vault Module

Gestalt provides a Vault module for integrating with HashiCorp Vault to securely retrieve secrets and inject them into configuration values through string substitution.

To use the Vault module, add `gestalt-vault` to your build configuration.

The module supports:
- Retrieving secrets from HashiCorp Vault
- String substitution with Vault secrets
- Integration with the vault-java-driver library

---

### Configuring Vault Integration

To use Vault secrets in string substitution, configure a `VaultModuleConfig` with your Vault client.

### Example

```java
import io.github.jopenlibs.vault.Vault;

// Configure Vault client
Vault vault = new Vault.Builder()
  .withAddress("http://localhost:8200")
  .withToken("your-vault-token")
  .build();

// Configure Gestalt with Vault module
Gestalt gestalt = new GestaltBuilder()
  .addSource(ClassPathConfigSourceBuilder.builder().setResource("/config.properties").build())
  .addModuleConfig(VaultBuilder.builder().setVault(vault).build())
  .build();

gestalt.loadConfigs();
```

### Using Vault Secrets in Configuration

In your configuration files, use the `vault:` prefix for string substitution:

```properties
db.password=${vault:secret/database,password}
api.key=${vault:secret/api,key}
```

The Vault module will automatically resolve these placeholders by fetching the corresponding secrets from Vault at runtime. The path after `vault:` should be the Vault secret path and key, separated by a comma. 

For example, `${vault:secret/database,password}` will:
1. Read the secret at path `secret/database` from Vault
2. Extract the `password` field from that secret
3. Replace the placeholder with the actual password value