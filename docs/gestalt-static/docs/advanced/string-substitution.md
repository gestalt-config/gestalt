---
sidebar_position: 3
---

# String Substitution
Gestalt supports string substitutions using `${}` to evaluate at load time on configuration properties to dynamically modify configurations.

For example if we have a properties file with a Database connection you don't want to save your usernames and passwords in the properties files. Instead, you want to inject the username and passwords as Environment Variables.

```properties
db.user=${DB_USER}
db.password=${DB_PASSWORD}
```

You can use multiple string replacements within a single string to build a configuration property.

```properties
db.uri=jdbc:mysql://${DB_HOST}:${DB_PORT}/${environment}
```

### Load time vs run time
Load time `${}` substitutions are evaluated when we load the configurations and build the config tree. This is done once on `gestalt.load()` then all results are cached in the config tree and returned.
Run time `#{}` substitutions are evaluated at runtime when you call `gestalt.getConfig(...);`, the results are not cached and each time you call `gestalt.getConfig(...);` you will re-evaluate the value.

It is recommended to use Load time `${}` substitutions in the vast majority of cases as it is more performant. The main use case for run time `#{}` substitutions is for values you expect to change from one call to the next, such as wanting a different random number each time you call `gestalt.getConfig(...);`.

Aside from evaluated time, the syntax and use of both `${}` and `#{}` are otherwise identical, you can mix and match them as needed.

### Specifying the Transformer
You can specify the substitution in the format `${transform:key}` or `${key}`. If you provide a transform name it will only check that one transform. Otherwise, it will check all the Transformer annotated with a `@ConfigPriority` in descending order and will return the first matching value.
Unlike the rest of Gestalt, this is case-sensitive, and it does not tokenize the string (except the node transform). The key expects an exact match, so if the Environment Variable name is DB_USER you need to use the key DB_USER. Using db.user or db_user will not match.

```properties
db.uri=jdbc:mysql://${DB_HOST}:${map:DB_PORT}/${sys:environment}
```

### Defaults for a Substitution
You can provide a default for the substitution in the format `${transform:key:=default}` or `${key:=default}`. If you provide a default it will use the default value in the event that the key provided cant be found

```properties
db.uri=jdbc:mysql://${DB_HOST}:${map:DB_PORT:=3306}/${environment:=dev}
```

Using nested substitution, you can have a chain of defaults. Where you can fall back from one source to another.

```properties
test.duration=${sys:duration:=${env:TEST_DURATION:=120}}
```
In this example, it will first try the system variable `duration`, then the Environment Variable `TEST_DURATION` and finally if none of those are found, it will use the default `120`

### Escaping a Substitution
You can escape the value with '\' like `\${my text}` to prevent the substitution. In Java you need to write `\\` to escape the character in a normal string but not in a Text block
In nested substitutions you should escape both the opening token `\${` and the closing token `\}` to be clear what is escaped, otherwise you may get undetermined results.

```properties
user.block.message=You are blocked because \\${reason\\}
```


### Nested Substitutions
Gestalt supports nested and recursive substitutions. Where a substitution can happen inside another substitution and the results could trigger another substitution.
Please use nested substitution sparingly, it can get very complex and confusing quickly.
Using these variables:

Environment Variables:
```properties
DB_HOST=cloudHost
environment=dev
```

System Variables:
```properties
DB_HOST=localHost
environment=test
```

Map Variable:
```properties
DB_TRANSFORM=sys
DB_PORT=13306
```
config source:
```properties
db.uri=jdbc:mysql://${${DB_TRANSFORM}:DB_HOST}:${map:DB_PORT}/${sys:environment}
```

This will resolve `${DB_TRANSFORM}` => `sys`
then resolve `${sys:DB_HOST}` => `localHost`
For a configuration value of `db.uri=jdbc:mysql://localHost:13306/test`


Nested substitution resolving to a nested substitution.
Given properties:
```properties
this.path = greeting
your.path = ${this.path}
my.path.greeting = good day
```

And a string to Substitute:
`"${my.path.${your.path}}"`

the result is `good day`

`${your.path}` resolves to `${this.path}`
`${this.path}` is then resolved to `greeting`
And finally the path `my.path.greeting` is resolved to `good day`

### Provided Transformers
| keyword      | priority | source                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|--------------|----------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| env          | 100      | Environment Variables                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| envVar       | 100      | **Deprecated** Environment Variables                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| sys          | 200      | Java System Properties                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| map          | 400      | A custom map provided to the constructor                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| node         | 300      | map to another leaf node in the configuration tree                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| random       | n/a      | provides a random value                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| base64Decode | n/a      | decode a base 64 encoded string                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| base64Encode | n/a      | encode a base 64 encoded string                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| classpath    | n/a      | load the contents of a file on the classpath into a string substitution.                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| dist100      | n/a      | Use a comma-separated list, where each element is a colon-separated pair of a threshold and its corresponding value. If an element has no threshold, it is treated as the default. For example, the format "10:red,30:green,blue" defines ranges and outcomes for random distributions: numbers from 1 to 10 correspond to red, 11 to 30 correspond to green, and all numbers above 30 default to blue. This is best used with runtime evaluation using `#{dist100:10:red,30:green,blue}` so each call will evaluate to a new value |
| file         | n/a      | load the contents of a file into a string substitution                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| urlDecode    | n/a      | URL decode a string                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| urlEncode    | n/a      | URL encode a string                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| awsSecret    | n/a/     | An AWS Secret is injected for the secret name and key. Configure the AWS Secret by registering a AWSModuleConfig using the AWSBuilder.   ```Gestalt gestalt = builder.addModuleConfig(AWSBuilder.builder().setRegion("us-east-1").build()).build();```                                                                                                                                                                                                                                                                              |
| azureSecret  | n/a/     | An Azure Secret is injected for the secret name and key. Configure the Azure Secret by registering a AzureModuleConfig using the AzureModuleBuilder.   ```Gestalt gestalt = builder.addModuleConfig(AzureModuleBuilder.builder().setKeyVaultUri("test").setCredential(tokenCredential)).build();```                                                                                                                                                                                                                                 |
| gcpSecret    | n/a      | A Google Cloud Secret given the key provided. Optionally configure the GCP Secret by registering an GoogleModuleConfig using the GoogleBuilder, or let google use the defaults.  ``` Gestalt gestalt = builder.addModuleConfig(GoogleBuilder.builder().setProjectId("myProject").build()).build()```                                                                                                                                                                                                                                |
| vault        | n/a      | A vault Secret given the key provided. Configure the Vault Secret by registering an VaultModuleConfig using the VaultBuilder.  ``` Gestalt gestalt = builder.addModuleConfig(VaultBuilder.builder().setVault(vault).build()).build()```. Uses the io.github.jopenlibs:vault-java-driver project to communicate with vault                                                                                                                                                                                                           |


### Random String Substitution
To inject a random variable during config node processing you can use the format `${random:type(origin, bound)}`
The random value is generated while loading the config, so you will always get the same random value when asking gestalt.

```properties
db.userId=dbUser-${random:int(5, 25)}
app.uuid=${random:uuid}
```

#### Random Options supported:
| data type | format                | notes                                                |
|-----------|-----------------------|------------------------------------------------------|
| byte      | byte                  | a random byte of data base 64 encoded                |
| byte      | byte(length)          | random bytes of provided length base 64 encoded      |
| int       | int                   | a random int of all possible int values              |
| int       | int(max)              | a random int from 0 to the max value provided        |
| int       | int(origin, bound)    | a random int between origin and bound                |
| long      | long                  | a random long of all possible long values            |
| long      | long(max)             | a random long from 0 to the max value provided       |
| long      | long(origin, bound)   | a random long between origin and bound               |
| float     | float                 | a random float between 0 and 1                       |
| float     | float(max)            | a random float from 0 to the max value provided      |
| float     | float(origin, bound)  | a random float between origin and bound              |
| double    | double                | a random double of all possible long values          |
| double    | double(max)           | a random double from 0 to the max value provided     |
| double    | double(origin, bound) | a random double between origin and bound             |
| boolean   | boolean               | a random boolean                                     |
| string    | string                | a random string of characters a-z of length 1        |
| string    | string(length)        | a random string of characters a-z of length provided |
| char      | char                  | a random char of characters a-z                      |
| uuid      | uuid                  | a random uuid                                        |
* Note: The formats in the table would need to be embedded inside of `${random:format}` so byte(length) would be `${random:byte(10)}`
