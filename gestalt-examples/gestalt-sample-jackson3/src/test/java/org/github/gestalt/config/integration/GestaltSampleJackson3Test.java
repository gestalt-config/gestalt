package org.github.gestalt.config.integration;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.annotations.ConfigPrefix;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.source.ClassPathConfigSourceBuilder;
import org.github.gestalt.config.source.FileConfigSourceBuilder;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.LogManager;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GestaltSampleJackson3Test {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = GestaltSampleJackson3Test.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // This example shows a how to load a Json source.
    @Test
    public void integrationTestJson() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL defaultFileURL = GestaltSampleJackson3Test.class.getClassLoader().getResource("default.json");
        File defaultFile = new File(defaultFileURL.getFile());

        URL devFileURL = GestaltSampleJackson3Test.class.getClassLoader().getResource("dev.json");
        File devFile = new File(devFileURL.getFile());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(FileConfigSourceBuilder.builder().setFile(defaultFile).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    // This example shows a how to load a Yaml source.
    @Test
    public void integrationTestYaml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL devFileURL = GestaltSampleJackson3Test.class.getClassLoader().getResource("dev.yml");
        File devFile = new File(devFileURL.getFile());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.yml").build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    // This example shows a how to load a json and Yaml source and
    // layer them on top of each other in the same config tree.
    @Test
    public void integrationTestJsonAndYaml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.json").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.yml").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    // This example shows a how to load a TOML source.
    @Test
    public void integrationTestToml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL defaultFileURL = GestaltSampleJackson3Test.class.getClassLoader().getResource("default.conf");
        File defaultFile = new File(defaultFileURL.getFile());

        URL devFileURL = GestaltSampleJackson3Test.class.getClassLoader().getResource("dev.toml");
        File devFile = new File(devFileURL.getFile());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(FileConfigSourceBuilder.builder().setFile(defaultFile).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    private void validateResults(Gestalt gestalt) throws GestaltException {
        HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);

        Assertions.assertEquals(1000, pool.maxTotal);
        Assertions.assertEquals((short) 1000, gestalt.getConfig("http.pool.maxTotal", Short.class));

        //Check with the wrong case
        Assertions.assertEquals((short) 1000, gestalt.getConfig("HTTP.pool.MAXTotal", Short.class));

        Assertions.assertEquals(50L, pool.maxPerRoute);
        Assertions.assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute", Long.class));
        Assertions.assertEquals(6000, pool.validateAfterInactivity);
        Assertions.assertEquals(60000D, pool.keepAliveTimeoutMs);
        Assertions.assertEquals(25, pool.idleTimeoutSec);
        Assertions.assertEquals(33.0F, pool.defaultWait);

        MaxTotal maxTotal = gestalt.getConfig("http.pool.maxTotal", MaxTotal.class);
        Assertions.assertEquals(1000, maxTotal.maxTotal);

        Map<String, Integer> httpPoolMap = gestalt.getConfig("http.pool", new TypeCapture<>() {
        });

        Assertions.assertEquals(50, httpPoolMap.get("maxperroute"));
        Assertions.assertEquals(6000, httpPoolMap.get("validateafterinactivity"));
        Assertions.assertEquals(60000, httpPoolMap.get("keepalivetimeoutms"));
        Assertions.assertEquals(25, httpPoolMap.get("idletimeoutsec"));

        Map<String, Integer> poolMap = gestalt.getConfig("http", new TypeCapture<>() {
        });

        Assertions.assertEquals(50, poolMap.get("pool.maxperroute"));
        Assertions.assertEquals(6000, poolMap.get("pool.validateafterinactivity"));
        Assertions.assertEquals(60000, poolMap.get("pool.keepalivetimeoutms"));
        Assertions.assertEquals(25, poolMap.get("pool.idletimeoutsec"));


        long startTime = System.nanoTime();
        gestalt.getConfig("db", DataBase.class);
        long timeTaken = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        DataBase db = gestalt.getConfig("db", DataBase.class);
        long cacheTimeTaken = System.nanoTime() - startTime;

        // not really a great test for ensuring we are hitting a cache
        //Assertions.assertTrue(timeTaken > cacheTimeTaken);

        Assertions.assertEquals(600, db.connectionTimeout);
        Assertions.assertEquals(600, gestalt.getConfig("DB.connectionTimeout", Integer.class));
        Assertions.assertEquals(123, db.idleTimeout);
        Assertions.assertEquals(60000.0F, db.maxLifetime);
        Assertions.assertTrue(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("db.doesNotExist", true, Boolean.class));

        // Test optional values.
        Assertions.assertEquals(600, gestalt.getConfigOptional("db.connectionTimeout", Integer.class).get());
        Assertions.assertEquals(600, gestalt.getConfig("db.connectionTimeout", OptionalInt.class).getAsInt());
        Assertions.assertEquals(600L, gestalt.getConfig("db.connectionTimeout", OptionalLong.class).getAsLong());
        Assertions.assertEquals(600D, gestalt.getConfig("db.connectionTimeout", OptionalDouble.class).getAsDouble());
        Assertions.assertEquals(600, gestalt.getConfig("db.connectionTimeout", new TypeCapture<Optional<Integer>>() {
        }).get());

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.get(0).getUser());
        // index into the path of an array.
        Assertions.assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test", String.class));
        Assertions.assertEquals("1234", db.hosts.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", db.hosts.get(0).url);
        Assertions.assertEquals("credmond", db.hosts.get(1).getUser());
        Assertions.assertEquals("5678", db.hosts.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", db.hosts.get(1).url);
        Assertions.assertEquals("credmond", db.hosts.get(2).getUser());
        Assertions.assertEquals("9012", db.hosts.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", db.hosts.get(2).url);

        Assertions.assertEquals("test", gestalt.getConfig("db.does.not.exist", "test", String.class));

        //  result prefix annotation
        DataBasePrefix dbPrefix = gestalt.getConfig("", DataBasePrefix.class);
        // not really a great test for ensuring we are hitting a cache
        Assertions.assertEquals(600, dbPrefix.connectionTimeout);
        Assertions.assertEquals(123, dbPrefix.idleTimeout);
        Assertions.assertEquals(60000.0F, dbPrefix.maxLifetime);
        Assertions.assertTrue(dbPrefix.isEnabled);

        Assertions.assertEquals(3, dbPrefix.hosts.size());
        Assertions.assertEquals("credmond", dbPrefix.hosts.get(0).getUser());
        // index into the path of an array.
        Assertions.assertEquals("1234", dbPrefix.hosts.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", dbPrefix.hosts.get(0).url);
        Assertions.assertEquals("credmond", dbPrefix.hosts.get(1).getUser());
        Assertions.assertEquals("5678", dbPrefix.hosts.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", dbPrefix.hosts.get(1).url);
        Assertions.assertEquals("credmond", dbPrefix.hosts.get(2).getUser());
        Assertions.assertEquals("9012", dbPrefix.hosts.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", dbPrefix.hosts.get(2).url);

        List<Host> hosts = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(3, hosts.size());
        Assertions.assertEquals("credmond", hosts.get(0).getUser());
        Assertions.assertEquals("1234", hosts.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hosts.get(0).url);
        Assertions.assertEquals("credmond", hosts.get(1).getUser());
        Assertions.assertEquals("5678", hosts.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hosts.get(1).url);
        Assertions.assertEquals("credmond", hosts.get(2).getUser());
        Assertions.assertEquals("9012", hosts.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hosts.get(2).url);

        List<IHost> ihosts = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(3, ihosts.size());
        Assertions.assertEquals("credmond", ihosts.get(0).getUser());
        Assertions.assertEquals("1234", ihosts.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", ihosts.get(0).getUrl());
        Assertions.assertEquals("credmond", ihosts.get(1).getUser());
        Assertions.assertEquals("5678", ihosts.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", ihosts.get(1).getUrl());
        Assertions.assertEquals("credmond", ihosts.get(2).getUser());
        Assertions.assertEquals("9012", ihosts.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", ihosts.get(2).getUrl());

        List<IHostDefault> ihostsDefault = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(3, ihostsDefault.size());
        Assertions.assertEquals("credmond", ihostsDefault.get(0).getUser());
        Assertions.assertEquals("1234", ihostsDefault.get(0).getPassword());
        Assertions.assertEquals(10, ihostsDefault.get(0).getPort());

        List<IHostAnnotations> iHostAnnotations = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(3, iHostAnnotations.size());
        Assertions.assertEquals("credmond", iHostAnnotations.get(0).getUser());
        Assertions.assertEquals("1234", iHostAnnotations.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", iHostAnnotations.get(0).getUrl());
        Assertions.assertEquals("customers", iHostAnnotations.get(0).getTable());
        Assertions.assertEquals("credmond", iHostAnnotations.get(1).getUser());
        Assertions.assertEquals("5678", iHostAnnotations.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", iHostAnnotations.get(1).getUrl());
        Assertions.assertEquals("customers", iHostAnnotations.get(1).getTable());
        Assertions.assertEquals("credmond", iHostAnnotations.get(2).getUser());
        Assertions.assertEquals("9012", iHostAnnotations.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", iHostAnnotations.get(2).getUrl());
        Assertions.assertEquals("customers", iHostAnnotations.get(2).getTable());

        List<HostAnnotations> hostsAnnotations = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(3, hostsAnnotations.size());
        Assertions.assertEquals("credmond", hostsAnnotations.get(0).getUser());
        Assertions.assertEquals("1234", hostsAnnotations.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hostsAnnotations.get(0).getUrl());
        Assertions.assertEquals("customers", hostsAnnotations.get(0).getTable());
        Assertions.assertEquals("credmond", hostsAnnotations.get(1).getUser());
        Assertions.assertEquals("5678", hostsAnnotations.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hostsAnnotations.get(1).getUrl());
        Assertions.assertEquals("customers", hostsAnnotations.get(1).getTable());
        Assertions.assertEquals("credmond", hostsAnnotations.get(2).getUser());
        Assertions.assertEquals("9012", hostsAnnotations.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hostsAnnotations.get(2).getUrl());
        Assertions.assertEquals("customers", hostsAnnotations.get(2).getTable());

        List<HostMethodAnnotations> hostsMethodAnnotations = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(3, hostsMethodAnnotations.size());
        Assertions.assertEquals("credmond", hostsMethodAnnotations.get(0).getUser());
        Assertions.assertEquals("1234", hostsMethodAnnotations.get(0).getSecret());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hostsMethodAnnotations.get(0).getUrl());
        Assertions.assertEquals("customers", hostsMethodAnnotations.get(0).getTable());
        Assertions.assertEquals("credmond", hostsMethodAnnotations.get(1).getUser());
        Assertions.assertEquals("5678", hostsMethodAnnotations.get(1).getSecret());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hostsMethodAnnotations.get(1).getUrl());
        Assertions.assertEquals("customers", hostsMethodAnnotations.get(1).getTable());
        Assertions.assertEquals("credmond", hostsMethodAnnotations.get(2).getUser());
        Assertions.assertEquals("9012", hostsMethodAnnotations.get(2).getSecret());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hostsMethodAnnotations.get(2).getUrl());
        Assertions.assertEquals("customers", hostsMethodAnnotations.get(2).getTable());

        List<Host> noHosts = gestalt.getConfig("db.not.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(0, noHosts.size());

        List<HostOpt> hostsOpt = gestalt.getConfig("db.hosts", Collections.emptyList(),
            new TypeCapture<>() {
            });
        Assertions.assertEquals(3, hostsOpt.size());
        Assertions.assertEquals("credmond", hostsOpt.get(0).getUser().get());
        Assertions.assertEquals("1234", hostsOpt.get(0).getPassword().get());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hostsOpt.get(0).getUrl().get());
        Assertions.assertFalse(hostsOpt.get(0).getPort().isPresent());
        Assertions.assertEquals("credmond", hostsOpt.get(1).getUser().get());
        Assertions.assertEquals("5678", hostsOpt.get(1).getPassword().get());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hostsOpt.get(1).getUrl().get());
        Assertions.assertFalse(hostsOpt.get(1).getPort().isPresent());
        Assertions.assertEquals("credmond", hostsOpt.get(2).getUser().get());
        Assertions.assertEquals("9012", hostsOpt.get(2).getPassword().get());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hostsOpt.get(2).getUrl().get());
        Assertions.assertFalse(hostsOpt.get(2).getPort().isPresent());

        List<HostOptionalInt> hostOptionalInt = gestalt.getConfig("db.hosts", Collections.emptyList(),
            new TypeCapture<>() {
            });
        Assertions.assertEquals(3, hostOptionalInt.size());
        Assertions.assertEquals("credmond", hostOptionalInt.get(0).getUser().get());
        Assertions.assertEquals(1234, hostOptionalInt.get(0).getPassword().getAsInt());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hostOptionalInt.get(0).getUrl().get());
        Assertions.assertFalse(hostOptionalInt.get(0).getPort().isPresent());
        Assertions.assertEquals("credmond", hostOptionalInt.get(1).getUser().get());
        Assertions.assertEquals(5678, hostOptionalInt.get(1).getPassword().getAsInt());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hostOptionalInt.get(1).getUrl().get());
        Assertions.assertFalse(hostOptionalInt.get(1).getPort().isPresent());
        Assertions.assertEquals("credmond", hostOptionalInt.get(2).getUser().get());
        Assertions.assertEquals(9012, hostOptionalInt.get(2).getPassword().getAsInt());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hostOptionalInt.get(2).getUrl().get());
        Assertions.assertFalse(hostOptionalInt.get(2).getPort().isPresent());

        User admin = gestalt.getConfig("admin", new TypeCapture<>() {
        });
        Assertions.assertEquals(3, admin.user.length);
        Assertions.assertEquals("Peter", admin.user[0]);
        Assertions.assertEquals("Kim", admin.user[1]);
        Assertions.assertEquals("Steve", admin.user[2]);
        Assertions.assertEquals(Role.LEVEL0, admin.accessRole);
        Assertions.assertTrue(admin.overrideEnabled);

        User user = gestalt.getConfig("employee", new TypeCapture<>() {
        });
        Assertions.assertEquals(1, user.user.length);
        Assertions.assertEquals("Janice", user.user[0]);
        Assertions.assertEquals(Role.LEVEL1, user.accessRole);
        Assertions.assertFalse(user.overrideEnabled);

        Assertions.assertEquals("active", gestalt.getConfig("serviceMode", TypeCapture.of(String.class)));
        Assertions.assertEquals('a', gestalt.getConfig("serviceMode", TypeCapture.of(Character.class)));

        Assertions.assertEquals(600, db.connectionTimeout);
        Assertions.assertEquals(600, gestalt.getConfig("DB.connectionTimeout", Integer.class));
        Assertions.assertEquals(123, db.idleTimeout);
        Assertions.assertEquals(60000.0F, db.maxLifetime);
        Assertions.assertTrue(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("db.unkown", true, Boolean.class));

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.get(0).getUser());
        // index into the path of an array.
        Assertions.assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test", String.class));
        Assertions.assertEquals("1234", db.hosts.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", db.hosts.get(0).url);
        Assertions.assertEquals("credmond", db.hosts.get(1).getUser());
        Assertions.assertEquals("5678", db.hosts.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", db.hosts.get(1).url);
        Assertions.assertEquals("credmond", db.hosts.get(2).getUser());
        Assertions.assertEquals("9012", db.hosts.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", db.hosts.get(2).url);

        Assertions.assertEquals("test", gestalt.getConfig("db.does.not.exist", "test", String.class));

    }

    public enum Role {
        LEVEL0, LEVEL1
    }

    public interface IHostDefault {
        String getUser();

        String getUrl();

        String getPassword();

        default int getPort() {
            return 10;
        }
    }

    public interface IHost {
        String getUser();

        String getUrl();

        String getPassword();
    }

    public interface IHostAnnotations {
        @Config(path = "user")
        String getUser();

        String getUrl();

        String getPassword();

        @Config(defaultVal = "customers")
        String getTable();
    }

    public static class HttpPool {
        public short maxTotal;
        public long maxPerRoute;
        public int validateAfterInactivity;
        public double keepAliveTimeoutMs = 6000;
        public int idleTimeoutSec = 10;
        public float defaultWait = 33.0F;

        public HttpPool() {

        }
    }

    public static class HostAnnotations implements IHost {
        private String user;
        private String url;

        @Config(path = "password")
        private String secret;

        @Config(defaultVal = "customers")
        private String table;

        public HostAnnotations() {
        }

        @Override
        public String getUser() {
            return user;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getPassword() {
            return secret;
        }

        public String getTable() {
            return table;
        }
    }

    public static class HostMethodAnnotations {
        private String user;
        private String url;
        private String secret;
        private String table;

        public String getUser() {
            return user;
        }

        public String getUrl() {
            return url;
        }

        @Config(path = "password")
        public String getSecret() {
            return secret;
        }

        @Config(defaultVal = "customers")
        public String getTable() {
            return table;
        }
    }

    public static class HostOpt {
        private Optional<String> user;
        private Optional<String> url;
        private Optional<String> password;

        private Optional<Integer> port;

        public HostOpt() {
        }

        public Optional<String> getUser() {
            return user;
        }

        public Optional<String> getUrl() {
            return url;
        }

        public Optional<String> getPassword() {
            return password;
        }

        public Optional<Integer> getPort() {
            return port;
        }
    }

    public static class HostOptionalInt {
        private Optional<String> user;
        private Optional<String> url;
        private OptionalInt password;

        private OptionalInt port;

        public HostOptionalInt() {
        }

        public Optional<String> getUser() {
            return user;
        }

        public Optional<String> getUrl() {
            return url;
        }

        public OptionalInt getPassword() {
            return password;
        }

        public OptionalInt getPort() {
            return port;
        }
    }

    public static class Host implements IHost {
        private String user;
        private String url;
        private String password;

        public Host() {
        }

        @Override
        public String getUser() {
            return user;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getPassword() {
            return password;
        }
    }

    public static class DataBase {
        public List<Host> hosts;
        public int connectionTimeout;
        public Integer idleTimeout;
        public float maxLifetime;
        public Boolean isEnabled = true;


        public DataBase() {
        }
    }

    @ConfigPrefix(prefix = "db")
    public static class DataBasePrefix {
        public List<Host> hosts;
        public int connectionTimeout;
        public Integer idleTimeout;
        public float maxLifetime;
        public Boolean isEnabled = true;

        public DataBasePrefix() {
        }
    }

    public static class User {
        public String[] user;
        public Boolean overrideEnabled = false;
        public Role accessRole;
    }

    public static class Connection {
        private String host;
        private int port;
        private String path;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }


    public static class MaxTotal {

        private Integer maxTotal;

        public MaxTotal(String maxTotal) {
            this.maxTotal = Integer.parseInt(maxTotal);
        }

        public Integer getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(Integer port) {
            this.maxTotal = port;
        }
    }

}
