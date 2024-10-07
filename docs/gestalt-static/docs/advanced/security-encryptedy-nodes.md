---
sidebar_position: 4
---

# Security - Encrypted Nodes
Another layer of security used by Gestalt is to encrypt your secrets at rest in memory.

The Encrypted Secrets feature allows us to specify the which secrets to encrypt using a regex. During initialization the secret is encrypted and the source value is released to be GC'ed. The source value may still be accessible until it has been GC'ed.
The configuration will be read from its source, and an encryption cipher with iv will be generated for each secret. The cipher and iv is used to encrypt the secret and the decryption cipher is passed into the node for decryption when needed.

These values will not be cached in the Gestalt Cache and should not be cached by the caller. Since they are not cached there a performance cost since each request has to be decrypted.
This makes it harder to access your secrets, but a persist attacker will be able to find the cipher, and iv in memory can decrypt the value.

To encrypt values you can either use the `addEncryptedSecret` methods in the `GestaltBuilder` or register a `EncryptedSecretModule` by using the `EncryptedSecretModuleBuilder`.

```java
Map<String, String> configs = new HashMap<>();
configs.put("my.password", "abcdef");

GestaltBuilder builder = new GestaltBuilder();
Gestalt gestalt = builder
  .addSource(MapConfigSourceBuilder.builder()
    .setCustomConfig(configs)
    .build())
  .addEncryptedSecret("password")
  .build();

gestalt.loadConfigs();

// the call will get the encrypted node but will return the decrypted results. 
Assertions.assertEquals("abcdef", gestalt.getConfig("my.password", String.class));
```
