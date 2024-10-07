---
sidebar_position: 2
---

# Config Tree
The config files are loaded and merged into a config tree. While loading into the config tree all node names and paths are converted to lower case and for environment variables we convert screaming snake case into dot notation. However, we do not convert other cases such as camel case into dot notation. So if your configs use a mix of dot notation and camel case, the nodes will not be merged. You can configure this conversion by providing your own `Sentence Lexer` in the `GestaltBuilder`. The config tree has a structure (sort of like json) where the root has one or more nodes or leafs. A node can have one or more nodes or leafs. A leaf can have a value but no further nodes or leafs. As we traverse the tree each node or leaf has a name and in combination it is called a path. A path can not have two leafs or both a node and a leaf at the same place. If this is detected Gestalt will throw an exception on loading with details on the path.

Valid:
```properties

db.connectionTimeout=6000
db.idleTimeout=600
db.maxLifetime=60000.0

http.pool.maxTotal=1000
http.pool.maxPerRoute=50
```

Invalid:
```properties

db.connectionTimeout=6000
db.idleTimeout=600
db=userTable                #invalid the path db is both a node and a leaf. 

http.pool.maxTotal=1000
http.pool.maxPerRoute=50
HTTP.pool.maxPerRoute=75    #invalid duplicate nodes at the same path.
```

All paths are converted to lower case as different sources have different naming conventions, Env Vars are typically Screaming Snake Case, properties are dot notation, json is camelCase. By normalizing them to lowercase it is easier to merge. However, we do not convert other cases such as camel case into dot notation. It is best to use a consistent case for your configurations.

This is configurable if you desire to keep case or split on different tokens. 
