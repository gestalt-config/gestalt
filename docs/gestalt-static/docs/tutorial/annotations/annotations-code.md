---
sidebar_position: 1
---

# Annotations Code
When decoding a Java Bean style class, a record, an interface or a Kotlin Data Class you can provide a custom annotation to override the path for the field as well as provide a default.
The field annotation `@Config` takes priority if both the field and method are annotated.
The class annotation `@ConfigPrefix` allows the user to define the prefix for the config object as part of the class instead of the `getConfig()` call. If you provide both the resulting prefix is first the path in getConfig then the prefix in the `@ConfigPrefix` annotation.
For example using `@ConfigPrefix(prefix = "connection")` with `DBInfo pool = gestalt.getConfig("db", DBInfo.class);` the resulting path would be `db.connection`.

```java
@ConfigPrefix(prefix = "db")
public class DBInfo {
    @Config(path = "channel.port", defaultVal = "1234")
    private int port;

    public int getPort() {
        return port;
    }
}

DBInfo pool = gestalt.getConfig("", DBInfo.class);


public class DBInfo {
    private int port;

    @Config(path = "channel.port", defaultVal = "1234")
    public int getPort() {
        return port;
    }
}  

DBInfo pool = gestalt.getConfig("db.connection", DBInfo.class);
```

The path provided in the annotation is used to find the configuration from the base path provided in the call to Gestalt getConfig.

So if the base path from gestalt.getConfig is `db.connection` and the annotation is `channel.port` the path the configuration will look for is `db.connection.channel.port`

The default accepts a string type and will be decoded into the property type using the gestalt decoders. For example if the property is an Integer and the default is "100" the integer value will be 100.
