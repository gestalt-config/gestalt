---
sidebar_position: 12
---

## Git Module

Gestalt provides a Git module for loading configuration from Git repositories. This module uses JGit to clone or pull remote repositories and load configuration files from them.

To use the Git module, add `gestalt-git` to your build configuration.

The module supports:
- Loading configuration from remote Git repositories
- Authentication using SSH keys or tokens
- Automatic syncing of repository changes

---

### Loading Configuration from Git

Use the `GitConfigSource` to load configuration from a Git repository.

### Example

```java
Gestalt gestalt = new GestaltBuilder()
  .addSource(GitConfigSourceBuilder.builder()
    .setRepoURI("https://github.com/myorg/config-repo.git")
    .setConfigFilePath("application.properties")
    .build())
  .build();

gestalt.loadConfigs();
```

### Authentication

The Git module supports various authentication methods:

- **SSH Key Authentication:**
```java
SshSessionFactory sshSessionFactory = new SshdSessionFactoryBuilder()
  .setPreferredAuthentications("publickey")
  .setSshDirectory(new File(System.getProperty("user.home"), ".ssh"))
  .build(null);

GitConfigSourceBuilder.builder()
  .setRepoURI("git@github.com:myorg/config-repo.git")
  .setSshSessionFactory(sshSessionFactory)
  .build()
```

- **Token Authentication:**
```java
CredentialsProvider credentials = new UsernamePasswordCredentialsProvider("token", "your-token");

GitConfigSourceBuilder.builder()
  .setRepoURI("https://github.com/myorg/config-repo.git")
  .setCredentials(credentials)
  .build()
```

### Configuration Options

- `setRepoURI`: The Git repository URL
- `setBranch`: Branch to checkout (default: main/master)
- `setConfigFilePath`: Path to the config file within the repository
- `setLocalRepoDirectory`: Local directory to clone to
- `setCredentials`: CredentialsProvider for authentication
- `setSshSessionFactory`: SshSessionFactory for SSH authentication