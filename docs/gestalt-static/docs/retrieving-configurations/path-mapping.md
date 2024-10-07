---
sidebar_position: 5
---

# Path Mapping

## Searching for path while Decoding Objects
When decoding a class, we need to know what configuration to lookup for each field. To generate the name of the configuration to lookup, we first find the path as defined in the call to `gestalt.getConfig("book.service", HttpPool.class)` where the path is `book.service`. We do not apply the path mappers to the path, only the config tree notes from the path. Once at the path we check class for any annotations. If there are no annotations, then we search for the fields by exact match. So we look for a config value with the same name as the field. If it is unable to find the exact match, it will attempt to map it to a path based on camel case. Where the camel case words will be separated and converted to Kebab case, Snake case and Dot Notation, then used to search for the configuration.
The order is descending based on the priority of the mapper.

| Casing                   | Priority | Class Name            |
|--------------------------|----------|-----------------------|
| Camel Case (exact match) | 1000     | StandardPathMapper    |
| Kebab Case               | 600      | KebabCasePathMapper   |
| Snake Case               | 550      | SnakeCasePathMapper   |
| Dot Notation             | 500      | DotNotationPathMapper |

Given the following class lets see how it is translated to the different casings:
```java
// With a class of 
public static class DBConnection {
    @Config(path = "host")
    private String uri;
    private int dbPort;
    private String dbPath;
}
```

#### Kebab Case:
All objects in Java use the standard Camel case, however in the config files you can use Kebab case, and if an exact match isnt found it will search for a config variable converting Camel case into Kebab case.
Kebab case or an exact match are preferred as using dot notation could potentially cause some issues as it is parsed to a config tree. Using dot notation you would need to ensure that none of values break the tree rules.

```java
// Given a config of
"users.host" => "myHost"
"users.uri" => "myHost"
"users.dbPort" => "1234"
"users.db-path" => "usersTable"
  
// the uri will map to host
// the dbPort will map to dbPort using Camel case using exact match.
// the dbPath will automatically map to db.path using Kebab case.     
DBConnection connection = gestalt.getConfig("users", TypeCapture.of(DBConnection.class));
```

#### Snake Case:
All objects in Java use the standard Camel case, however in the config files you can use Snake case, and if an exact match isnt found it will search for a config variable converting Camel case into snake case.

```java
// Given a config of
"users.host" => "myHost"
"users.uri" => "myHost"
"users.dbPort" => "1234"
"users.db_path" => "usersTable"
  
// the uri will map to host
// the dbPort will map to dbPort using Camel case using exact match.
// the dbPath will automatically map to db_path using snake case     
DBConnection connection = gestalt.getConfig("users", TypeCapture.of(DBConnection.class));
```

#### Dot Notation:
All objects in Java use the standard Camel case, however in the config files you can use Dot Notation, and if an exact match isnt found it will search for a config variable converting Camel case into Dot Notation.
Kebab case or an exact match are preferred as using dot notation could potentially cause some issues as it is parsed to a config tree. Using dot notation you would need to ensure that none of values break the tree rules.

```java
// Given a config of
"users.host" => "myHost"
"users.uri" => "myHost"
"users.dbPort" => "1234"
"users.db.path" => "usersTable"
  
// the uri will map to host
// the dbPort will map to dbPort using Camel case using exact match.
// the dbPath will automatically map to db.path using dot notation.     
DBConnection connection = gestalt.getConfig("users", TypeCapture.of(DBConnection.class));
```
