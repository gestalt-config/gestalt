package org.github.gestalt.config.integration;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.GestaltImportProcessorTest;
import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.annotations.ConfigPrefix;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.decoder.*;
import org.github.gestalt.config.entity.ConfigContainer;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.loader.ConfigLoaderRegistry;
import org.github.gestalt.config.loader.MapConfigLoader;
import org.github.gestalt.config.node.SubsetTagsWithDefaultTagResolutionStrategy;
import org.github.gestalt.config.processor.config.transform.LoadtimeStringSubstitutionConfigNodeProcessor;
import org.github.gestalt.config.processor.config.transform.SystemPropertiesTransformer;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.FileChangeReloadStrategy;
import org.github.gestalt.config.reload.ManualConfigReloadStrategy;
import org.github.gestalt.config.source.*;
import org.github.gestalt.config.node.factory.MapNodeImportFactory;
import org.github.gestalt.config.tag.Tag;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.test.classes.DBInfoNoConstructor;
import org.github.gestalt.config.test.classes.DBInfoOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.LogManager;

public class GestaltIntegrationTests {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = GestaltIntegrationTests.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    public void integrationTest() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        String fileURL = "https://raw.githubusercontent.com/gestalt-config/gestalt/main/gestalt-core/src/test/resources/default.properties";

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(URLConfigSourceBuilder.builder().setSourceURL(fileURL).build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(StringConfigSourceBuilder.builder().setConfig("db.idleTimeout=123").setFormat("properties").build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Test
    public void integrationTestProxyPassThrough() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        String fileURL = "https://raw.githubusercontent.com/gestalt-config/gestalt/main/gestalt-core/src/test/resources/default.properties";

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(URLConfigSourceBuilder.builder().setSourceURL(fileURL).build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(StringConfigSourceBuilder.builder().setConfig("db.idleTimeout=123").setFormat("properties").build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Test
    public void testGettingEmptyPath() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("password", "test");
        configs.put("uri", "somedatabase");
        configs.put("port", "3306");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        DBInfo dbInfo = gestalt.getConfig("", DBInfo.class);

        Assertions.assertEquals("test", dbInfo.getPassword());
        Assertions.assertEquals("somedatabase", dbInfo.getUri());
        Assertions.assertEquals(3306, dbInfo.getPort());

        DBInfo dbInfoDef = gestalt.getConfig("", new DBInfo(), DBInfo.class);

        Assertions.assertEquals("test", dbInfoDef.getPassword());
        Assertions.assertEquals("somedatabase", dbInfoDef.getUri());
        Assertions.assertEquals(3306, dbInfoDef.getPort());

        Optional<DBInfo> dbInfoOpt = gestalt.getConfigOptional("", DBInfo.class);

        Assertions.assertEquals("test", dbInfoOpt.get().getPassword());
        Assertions.assertEquals("somedatabase", dbInfoOpt.get().getUri());
        Assertions.assertEquals(3306, dbInfoOpt.get().getPort());
    }

    //to run this test it must be run as an administrator.
    @Test
    //@Disabled
    public void integrationTestReloadFile() throws GestaltException, IOException, InterruptedException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL devFileURL = GestaltIntegrationTests.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        Path tempFile = Files.createTempFile("gestalt", "dev.properties");
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, Files.readAllBytes(devFile.toPath()));

        devFile = tempFile.toFile();

        TestReloadListener reloadListener = new TestReloadListener();
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())
            .addSource(FileConfigSourceBuilder.builder()
                .setFile(devFile)
                .addConfigReloadStrategy(new FileChangeReloadStrategy())
                .build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addCoreReloadListener(reloadListener)
            .build();

        gestalt.loadConfigs();

        HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);

        Assertions.assertEquals(1000, pool.maxTotal);
        Assertions.assertEquals((short) 1000, gestalt.getConfig("http.pool.maxTotal", Short.class));
        Assertions.assertEquals(50L, pool.maxPerRoute);
        Assertions.assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute", Long.class));
        Assertions.assertEquals(6000, pool.validateAfterInactivity);
        Assertions.assertEquals(60000D, pool.keepAliveTimeoutMs);
        Assertions.assertEquals(25, pool.idleTimeoutSec);
        Assertions.assertEquals(33.0F, pool.defaultWait);

        long startTime = System.nanoTime();
        gestalt.getConfig("db", DataBase.class);
        long timeTaken = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        DataBase db = gestalt.getConfig("db", DataBase.class);
        long cacheTimeTaken = System.nanoTime() - startTime;

        // not really a great test for ensuring we are hitting a cache
        Assertions.assertTrue(timeTaken > cacheTimeTaken);

        Assertions.assertEquals(600, db.connectionTimeout);
        Assertions.assertEquals(600, gestalt.getConfig("db.connectionTimeout", Integer.class));
        Assertions.assertEquals(123, db.idleTimeout);
        Assertions.assertEquals(60000.0F, db.maxLifetime);
        Assertions.assertTrue(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("DB.isEnabled", true, Boolean.class));

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.get(0).getUser());
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

        List<Host> noHosts = gestalt.getConfig("db.not.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(0, noHosts.size());

        String config = "db.hosts[0].url=jdbc:postgresql://dev.host.name1:5432/mydb2\n" +
            "db.hosts[1].url=jdbc:postgresql://dev.host.name2:5432/mydb2\n" +
            "db.hosts[2].url=jdbc:postgresql://dev.host.name3:5432/mydb2\n" +
            "db.connectionTimeout=2222\n" +
            "\n" +
            "http.pool.maxTotal=222\n" +
            "http.pool.maxPerRoute=22\n" +
            "\n" +
            "admin.user=Peter, Kim, Steve\n" +
            "admin.overrideEnabled=true\n";

        // Update the config file so we cause a reload to happen.
        Files.writeString(devFile.toPath(), config);

        // Reloads sometimes take a little bit of time, so wait till the update has been triggered.
        for (int i = 0; i < 10; i++) {
            if (reloadListener.count > 1) {
                break;
            } else {
                Thread.sleep(100);
            }
        }
        db = gestalt.getConfig("DB", DataBase.class);
        Assertions.assertTrue(reloadListener.count >= 1);

        Assertions.assertEquals(2222, db.connectionTimeout);
        Assertions.assertEquals(2222, gestalt.getConfig("DB.connectionTimeout", Integer.class));
        Assertions.assertEquals(123, db.idleTimeout);
        Assertions.assertEquals(60000.0F, db.maxLifetime);
        Assertions.assertTrue(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("db.isEnabled", true, Boolean.class));

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.get(0).getUser());
        Assertions.assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test", String.class));
        Assertions.assertEquals("1234", db.hosts.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb2", db.hosts.get(0).url);
        Assertions.assertEquals("credmond", db.hosts.get(1).getUser());
        Assertions.assertEquals("5678", db.hosts.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb2", db.hosts.get(1).url);
        Assertions.assertEquals("credmond", db.hosts.get(2).getUser());
        Assertions.assertEquals("9012", db.hosts.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb2", db.hosts.get(2).url);

        Assertions.assertEquals("test", gestalt.getConfig("db.does.not.exist", "test", String.class));

        hosts = gestalt.getConfig("db.Hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(3, hosts.size());
        Assertions.assertEquals("credmond", hosts.get(0).getUser());
        Assertions.assertEquals("1234", hosts.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb2", hosts.get(0).url);
        Assertions.assertEquals("credmond", hosts.get(1).getUser());
        Assertions.assertEquals("5678", hosts.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb2", hosts.get(1).url);
        Assertions.assertEquals("credmond", hosts.get(2).getUser());
        Assertions.assertEquals("9012", hosts.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb2", hosts.get(2).url);
    }

    @Test
    public void integrationTestManualReload() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("some.value", "value1");

        var manualReload = new ManualConfigReloadStrategy();

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(manualReload)
                .build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("value1", gestalt.getConfig("some.value", String.class));

        configs.put("some.value", "value2");

        manualReload.reload();

        Assertions.assertEquals("value2", gestalt.getConfig("some.value", String.class));
    }

    @Test
    public void integrationTestManualReloadWithConfigContainer() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("some.value", "value1");

        var manualReload = new ManualConfigReloadStrategy();

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .addConfigReloadStrategy(manualReload)
                .build())
            .build();

        gestalt.loadConfigs();

        var configContainer = gestalt.getConfig("some.value", new TypeCapture<ConfigContainer<String>>() {});

        Assertions.assertEquals("value1", configContainer.orElseThrow());

        // Change the values in the config map
        configs.put("some.value", "value2");

        // let gestalt know the values have changed so it can update the config tree.
        manualReload.reload();

        // The config container is automatically updated.
        Assertions.assertEquals("value2", configContainer.orElseThrow());
    }

    @Test
    public void integrationTestAddSource() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("some.value", "value1");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("some.value", "value2");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .build())
            .build();

        gestalt.loadConfigs();

        var value = gestalt.getConfig("some.value", String.class);

        Assertions.assertEquals("value1", value);

        gestalt.addConfigSourcePackage(MapConfigSourceBuilder.builder()
            .setCustomConfig(configs2)
            .build());

        value = gestalt.getConfig("some.value", String.class);

        // The value has changed
        Assertions.assertEquals("value2", value);
    }

    @Test
    public void integrationTestAddSourceWithConfigContainer() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("some.value", "value1");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("some.value", "value2");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs)
                .build())
            .build();

        gestalt.loadConfigs();

        var configContainer = gestalt.getConfig("some.value", new TypeCapture<ConfigContainer<String>>() {});

        Assertions.assertEquals("value1", configContainer.orElseThrow());

        gestalt.addConfigSourcePackage(MapConfigSourceBuilder.builder()
            .setCustomConfig(configs2)
            .build());

        // The config container is automatically updated.
        Assertions.assertEquals("value2", configContainer.orElseThrow());
    }

    @Test
    public void integrationTestEmpty() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        String fileURL = "https://raw.githubusercontent.com/gestalt-config/gestalt/main/gestalt-core/src/test/resources/default.properties";

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(URLConfigSourceBuilder.builder().setSourceURL(fileURL).build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("empty.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(StringConfigSourceBuilder.builder().setConfig("db.idleTimeout=123").setFormat("properties").build())
            .addSource(StringConfigSourceBuilder.builder().setConfig("").setFormat("properties").build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Test
    public void integrationTestEmptyReload() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        String fileURL = "https://raw.githubusercontent.com/gestalt-config/gestalt/main/gestalt-core/src/test/resources/default.properties";

        var reloadStrategy = new ManualConfigReloadStrategy();

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(URLConfigSourceBuilder.builder().setSourceURL(fileURL).build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("empty.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(StringConfigSourceBuilder.builder().setConfig("db.idleTimeout=123").setFormat("properties").build())
            .addSource(StringConfigSourceBuilder.builder()
                .setConfig("")
                .setFormat("properties")
                .addConfigReloadStrategy(reloadStrategy)
                .build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        reloadStrategy.reload();

        validateResults(gestalt);
    }

    @Test
    public void integrationTestEnvVars() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        /*
        Expects the following environment variables
            DB_IDLETIMEOUT: 123
            SUBSERVICE_BOOKING_ISENABLED: true
            SUBSERVICE_BOOKING_SERVICE_HOST: https://dev.booking.host.name
            SUBSERVICE_BOOKING_SERVICE_PORT: 443
         */

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/dev.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(EnvironmentConfigSourceBuilder.builder().setFailOnErrors(false).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
    }

    @Test
    public void integrationTestPostProcessorEnvironment() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        /*
        Expects the following environment variables
            DB_IDLETIMEOUT: 123
            SUBSERVICE_BOOKING_ISENABLED: true
            SUBSERVICE_BOOKING_SERVICE_HOST: https://dev.booking.host.name
            SUBSERVICE_BOOKING_SERVICE_PORT: 443
         */

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("defaultPPEnv.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/integration.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addDefaultPostProcessors()
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
    }

    @Test
    public void integrationTestPostProcessorSystem() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        /*
        Expects the following system properties variables
            DB_IDLETIMEOUT: 123
            SUBSERVICE_BOOKING_ISENABLED: true
            SUBSERVICE_BOOKING_SERVICE_HOST: https://dev.booking.host.name
            SUBSERVICE_BOOKING_SERVICE_PORT: 443
         */

        System.getProperties().put("DB_IDLETIMEOUT", "123");
        System.getProperties().put("SUBSERVICE_BOOKING_ISENABLED", "true");
        System.getProperties().put("SUBSERVICE_BOOKING_SERVICE_HOST", "https://dev.booking.host.name");
        System.getProperties().put("SUBSERVICE_BOOKING_SERVICE_PORT", "443");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/defaultPPSys.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("integration.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addConfigNodeProcessor(
                new LoadtimeStringSubstitutionConfigNodeProcessor(Collections.singletonList(new SystemPropertiesTransformer())))
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
    }

    @Test
    public void integrationTestPostProcessorNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/defaultPPNode.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("integration.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
    }

    @Test
    public void integrationTestPostProcessorMulti() throws GestaltException {

        URL employeeURL = GestaltIntegrationTests.class.getClassLoader().getResource("employee");
        File testFile = new File(employeeURL.getFile());

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        configs.put("employee.user", "${file:" + testFile.getAbsolutePath() + "}");

        /*
        Expects the following system properties variables
            DB_IDLETIMEOUT: 123
            SUBSERVICE_BOOKING_ISENABLED: true
            SUBSERVICE_BOOKING_SERVICE_HOST: https://dev.booking.host.name
            SUBSERVICE_BOOKING_SERVICE_PORT: 443
         */

        System.getProperties().put("DB_IDLETIMEOUT", "123");
        System.getProperties().put("SUBSERVICE_BOOKING_ISENABLED", "true");
        System.getProperties().put("SUBSERVICE_BOOKING_SERVICE_HOST", "https://dev.booking.host.name");
        System.getProperties().put("SUBSERVICE_BOOKING_SERVICE_PORT", "443");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/defaultMulti.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("integration.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
    }

    @SuppressWarnings("JdkObsolete")
    private void validateResults(Gestalt gestalt) throws GestaltException {
        HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);

        Assertions.assertEquals(1000, pool.maxTotal);
        Assertions.assertEquals((short) 1000, gestalt.getConfig("http.pool.maxTotal", Short.class));
        Assertions.assertEquals(50L, pool.maxPerRoute);
        Assertions.assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute", Long.class));
        Assertions.assertEquals(6000, pool.validateAfterInactivity);
        Assertions.assertEquals(60000D, pool.keepAliveTimeoutMs);
        Assertions.assertEquals(25, pool.idleTimeoutSec);
        Assertions.assertEquals(33.0F, pool.defaultWait);

        MaxTotal maxTotal = gestalt.getConfig("http.pool.maxTotal", MaxTotal.class);
        Assertions.assertEquals(1000, maxTotal.maxTotal);

        long startTime = System.nanoTime();
        gestalt.getConfig("db", DataBase.class);
        long timeTaken = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        DataBase db = gestalt.getConfig("db", DataBase.class);
        long cacheTimeTaken = System.nanoTime() - startTime;

        // not really a great test for ensuring we are hitting a cache
        Assertions.assertTrue(timeTaken > cacheTimeTaken);

        Assertions.assertEquals(600, db.connectionTimeout);
        Assertions.assertEquals(600, gestalt.getConfig("db.connectionTimeout", Integer.class));

        Assertions.assertEquals(600, gestalt.getConfig("db.connectionTimeout", OptionalInt.class).getAsInt());
        Assertions.assertEquals(600L, gestalt.getConfig("db.connectionTimeout", OptionalLong.class).getAsLong());
        Assertions.assertEquals(600D, gestalt.getConfig("db.connectionTimeout", OptionalDouble.class).getAsDouble());
        Assertions.assertEquals(600, gestalt.getConfig("db.connectionTimeout", new TypeCapture<Optional<Integer>>() {
        }).get());
        Assertions.assertEquals(123, db.idleTimeout);
        Assertions.assertEquals(60000.0F, db.maxLifetime);
        Assertions.assertTrue(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("db.does.not.exist", true, Boolean.class));

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.get(0).getUser());
        // index into the path of an array.
        Assertions.assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test", String.class));
        Optional<String> optUser = gestalt.getConfig("db.hosts[0].user", new TypeCapture<>() {
        });
        Assertions.assertTrue(optUser.isPresent());
        Assertions.assertEquals("credmond", optUser.get());
        Assertions.assertEquals("1234", db.hosts.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", db.hosts.get(0).url);
        Assertions.assertEquals("credmond", db.hosts.get(1).getUser());
        Assertions.assertEquals("5678", db.hosts.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", db.hosts.get(1).url);
        Assertions.assertEquals("credmond", db.hosts.get(2).getUser());
        Assertions.assertEquals("9012", db.hosts.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", db.hosts.get(2).url);

        Assertions.assertEquals("test", gestalt.getConfig("db.does.not.exist", "test", String.class));

        //  annotation
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
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", ihostsDefault.get(0).getUrl());
        Assertions.assertEquals("credmond", ihostsDefault.get(1).getUser());
        Assertions.assertEquals("5678", ihostsDefault.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", ihostsDefault.get(1).getUrl());
        Assertions.assertEquals("credmond", ihostsDefault.get(2).getUser());
        Assertions.assertEquals("9012", ihostsDefault.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", ihostsDefault.get(2).getUrl());
        Assertions.assertEquals(10, ihostsDefault.get(2).getPort());

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

        List<IHostAnnotations> ihostAnnotations = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(3, ihostAnnotations.size());
        Assertions.assertEquals("credmond", ihostAnnotations.get(0).getUser());
        Assertions.assertEquals("1234", ihostAnnotations.get(0).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", ihostAnnotations.get(0).getUrl());
        Assertions.assertEquals("customers", ihostAnnotations.get(0).getTable());
        Assertions.assertEquals("credmond", ihostAnnotations.get(1).getUser());
        Assertions.assertEquals("5678", ihostAnnotations.get(1).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", ihostAnnotations.get(1).getUrl());
        Assertions.assertEquals("customers", ihostAnnotations.get(1).getTable());
        Assertions.assertEquals("credmond", ihostAnnotations.get(2).getUser());
        Assertions.assertEquals("9012", ihostAnnotations.get(2).getPassword());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", ihostAnnotations.get(2).getUrl());
        Assertions.assertEquals("customers", ihostAnnotations.get(2).getTable());

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

        List<HostMethodAnnotations> hostsMethodAnnotations = gestalt.getConfig("db.hosts", Collections.emptyList(),
            new TypeCapture<>() {
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

        LinkedList<HostOptionalInt> hostOptionalInt = gestalt.getConfig("db.hosts", new LinkedList<>(),
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


        ArrayList<Host> noHosts = gestalt.getConfig("db.not.hosts", new ArrayList<>(), new TypeCapture<>() {
        });
        Assertions.assertEquals(0, noHosts.size());

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
    }

    @Test
    public void integrationTestTags() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        String fileURL = "https://raw.githubusercontent.com/gestalt-config/gestalt/main/gestalt-core/src/test/resources/default.properties";

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(URLConfigSourceBuilder.builder().setSourceURL(fileURL).build())
            .addSource(ClassPathConfigSourceBuilder.builder()
                .setResource("/dev.properties")
                .setTags(Tags.of("toy", "ball")).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(StringConfigSourceBuilder.builder().setConfig("db.idleTimeout=123").setFormat("properties").build())
            .build();

        gestalt.loadConfigs();

        HttpPool pool = gestalt.getConfig("http.pool", HttpPool.class);

        Assertions.assertEquals(100, pool.maxTotal);
        Assertions.assertEquals((short) 100, gestalt.getConfig("http.pool.maxTotal", Short.class));
        Assertions.assertEquals(10L, pool.maxPerRoute);
        Assertions.assertEquals(10L, gestalt.getConfig("http.pool.maxPerRoute", Long.class));
        Assertions.assertEquals(6000, pool.validateAfterInactivity);
        Assertions.assertEquals(60000D, pool.keepAliveTimeoutMs);
        Assertions.assertEquals(25, pool.idleTimeoutSec);
        Assertions.assertEquals(33.0F, pool.defaultWait);

        HttpPool poolTags = gestalt.getConfig("http.pool", HttpPool.class, Tags.of("toy", "ball"));

        Assertions.assertEquals(1000, poolTags.maxTotal);
        Assertions.assertEquals((short) 1000, gestalt.getConfig("http.pool.maxTotal", Short.class, Tags.of("toy", "ball")));
        Assertions.assertEquals(50L, poolTags.maxPerRoute);
        Assertions.assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute", Long.class, Tags.of("toy", "ball")));
        Assertions.assertEquals(6000, poolTags.validateAfterInactivity);
        Assertions.assertEquals(60000D, poolTags.keepAliveTimeoutMs);
        Assertions.assertEquals(25, poolTags.idleTimeoutSec);
        Assertions.assertEquals(33.0F, poolTags.defaultWait);

        HttpPool poolTags2 = gestalt.getConfig("http.pool", HttpPool.class, Tags.of("toy", "car"));

        Assertions.assertEquals(100, poolTags2.maxTotal);
        Assertions.assertEquals((short) 100, gestalt.getConfig("http.pool.maxTotal", Short.class, Tags.of("toy", "car")));
        Assertions.assertEquals(10L, poolTags2.maxPerRoute);
        Assertions.assertEquals(10L, gestalt.getConfig("http.pool.maxPerRoute", Long.class, Tags.of("toy", "car")));
        Assertions.assertEquals(6000, poolTags2.validateAfterInactivity);
        Assertions.assertEquals(60000D, poolTags2.keepAliveTimeoutMs);
        Assertions.assertEquals(25, poolTags2.idleTimeoutSec);
        Assertions.assertEquals(33.0F, poolTags2.defaultWait);
    }

    @Test
    public void integrationTestCamelCase() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("users.host", "myHost");
        configs.put("users.uri", "different host");
        configs.put("users.db.port", "1234");
        configs.put("users.db.path", "usersTable");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        DBConnection connection = gestalt.getConfig("users", TypeCapture.of(DBConnection.class));
        Assertions.assertEquals("myHost", connection.getUri());
        Assertions.assertEquals(1234, connection.getDbPort());
        Assertions.assertEquals("usersTable", connection.getDbPath());
    }

    @Test
    public void testMapTypes() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "abcdef");
        configs.put("db.uri", "my.sql.com");
        configs.put("db.salt", "pepper");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        Map<String, String> dbMap = gestalt.getConfig("db", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbMap)
            .isInstanceOf(HashMap.class)
            .contains(Map.entry("password", "test"))
            .contains(Map.entry("port", "abcdef"))
            .contains(Map.entry("uri", "my.sql.com"))
            .contains(Map.entry("salt", "pepper"));

        HashMap<String, String> dbHashMap = gestalt.getConfig("db", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbHashMap)
            .isInstanceOf(HashMap.class)
            .contains(Map.entry("password", "test"))
            .contains(Map.entry("port", "abcdef"))
            .contains(Map.entry("uri", "my.sql.com"))
            .contains(Map.entry("salt", "pepper"));

        TreeMap<String, String> dbTreeMap = gestalt.getConfig("db", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbTreeMap)
            .isInstanceOf(TreeMap.class)
            .contains(Map.entry("password", "test"))
            .contains(Map.entry("port", "abcdef"))
            .contains(Map.entry("uri", "my.sql.com"))
            .contains(Map.entry("salt", "pepper"));

        LinkedHashMap<String, String> dbLinkedHashMap = gestalt.getConfig("db", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbLinkedHashMap)
            .isInstanceOf(LinkedHashMap.class)
            .contains(Map.entry("password", "test"))
            .contains(Map.entry("port", "abcdef"))
            .contains(Map.entry("uri", "my.sql.com"))
            .contains(Map.entry("salt", "pepper"));
    }

    @Test
    public void testSetTypes() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        Set<String> dbSet = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbSet)
            .isInstanceOf(HashSet.class)
            .contains("John")
            .contains("Steve");

        HashSet<String> dbHashSet = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbHashSet)
            .isInstanceOf(HashSet.class)
            .contains("John")
            .contains("Steve");

        TreeSet<String> dbTreeSet = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbTreeSet)
            .isInstanceOf(TreeSet.class)
            .contains("John")
            .contains("Steve");

        LinkedHashSet<String> dbLinkedHashSet = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbLinkedHashSet)
            .isInstanceOf(LinkedHashSet.class)
            .contains("John")
            .contains("Steve");
    }

    @Test
    public void testListTypes() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        List<String> dbList = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbList)
            .isInstanceOf(List.class)
            .contains("John")
            .contains("Steve");

        AbstractList<String> dbAbstractList = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbAbstractList)
            .isInstanceOf(AbstractList.class)
            .contains("John")
            .contains("Steve");

        CopyOnWriteArrayList<String> dbCopyOnWriteArrayList = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbCopyOnWriteArrayList)
            .isInstanceOf(CopyOnWriteArrayList.class)
            .contains("John")
            .contains("Steve");

        ArrayList<String> dbArrayList = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbArrayList)
            .isInstanceOf(ArrayList.class)
            .contains("John")
            .contains("Steve");

        LinkedList<String> dbLinkedList = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbLinkedList)
            .isInstanceOf(LinkedList.class)
            .contains("John")
            .contains("Steve");

        Stack<String> dbStack = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbStack)
            .isInstanceOf(Stack.class)
            .contains("John")
            .contains("Steve");

        Vector<String> dbVector = gestalt.getConfig("admin", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbVector)
            .isInstanceOf(Vector.class)
            .contains("John")
            .contains("Steve");
    }

    @Test
    public void testSubsetTagsWithDefaultTagResolutionStrategy() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "abcdef");
        configs.put("db.uri", "my.sql.com");
        configs.put("db.salt", "pepper");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.port", "abcdef2");

        Map<String, String> configs3 = new HashMap<>();
        configs3.put("db.uri", "my.sql.com3");
        configs3.put("db.salt", "pepper3");

        Map<String, String> configs4 = new HashMap<>();
        configs4.put("db.salt", "pepper4");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs2)
                .addTag(Tag.profile("orange"))
                .addTag(Tag.profile("flower"))
                .build())
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs3)
                .addTag(Tag.profile("blue"))
                .build())
            .addSource(MapConfigSourceBuilder.builder()
                .setCustomConfig(configs4)
                .addTag(Tag.profile("orange"))
                .build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .useCacheDecorator(false)
            .setConfigNodeTagResolutionStrategy(new SubsetTagsWithDefaultTagResolutionStrategy())
            .build();

        gestalt.loadConfigs();

        Map<String, String> dbMap = gestalt.getConfig("db", new TypeCapture<>(){});

        org.assertj.core.api.Assertions.assertThat(dbMap)
            .isInstanceOf(HashMap.class)
            .contains(Map.entry("password", "test"))
            .contains(Map.entry("port", "abcdef"))
            .contains(Map.entry("uri", "my.sql.com"))
            .contains(Map.entry("salt", "pepper"));

        dbMap = gestalt.getConfig("db", new TypeCapture<>(){}, Tags.profile("orange"));

        org.assertj.core.api.Assertions.assertThat(dbMap)
            .isInstanceOf(HashMap.class)
            .contains(Map.entry("password", "test"))
            .contains(Map.entry("port", "abcdef"))
            .contains(Map.entry("uri", "my.sql.com"))
            .contains(Map.entry("salt", "pepper4"));

        dbMap = gestalt.getConfig("db", new TypeCapture<>(){}, Tags.of(Tag.profile("orange"), Tag.profile("blue")));

        org.assertj.core.api.Assertions.assertThat(dbMap)
            .isInstanceOf(HashMap.class)
            .contains(Map.entry("password", "test"))
            .contains(Map.entry("port", "abcdef"))
            .contains(Map.entry("uri", "my.sql.com3"))
            .contains(Map.entry("salt", "pepper4"));

        dbMap = gestalt.getConfig("db", new TypeCapture<>(){}, Tags.of(Tag.profile("orange"), Tag.profile("flower")));

        org.assertj.core.api.Assertions.assertThat(dbMap)
            .isInstanceOf(HashMap.class)
            .contains(Map.entry("password", "test"))
            .contains(Map.entry("port", "abcdef2"))
            .contains(Map.entry("uri", "my.sql.com"))
            .contains(Map.entry("salt", "pepper4"));

    }

    @Test
    public void temporaryNode() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .addTemporaryNodeAccessCount("password", 1)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));

        Assertions.assertEquals("", gestalt.getConfigOptional("db.password", String.class).get());
    }

    @Test
    public void encryptedPassword() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .addEncryptedSecret("password")
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));

        Assertions.assertEquals("tags: Tags{[]} = MapNode{admin=ArrayNode{values=[LeafNode{value='John2'}, " +
            "LeafNode{value='Steve2'}]}, db=MapNode{password=EncryptedLeafNode{value='*****'}, port=LeafNode{value='3306'}, " +
            "name=LeafNode{value='test2'}}}", gestalt.debugPrint());
    }

    @Test
    public void encryptedPasswordAndTemporaryNode() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .addEncryptedSecret("password")
            .addTemporaryNodeAccessCount("password", 1)
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));

        Assertions.assertEquals("tags: Tags{[]} = MapNode{admin=ArrayNode{values=[LeafNode{value='John2'}, " +
            "LeafNode{value='Steve2'}]}, db=MapNode{password=TemporaryLeafNode{value='*****'}, port=LeafNode{value='3306'}, " +
            "name=LeafNode{value='test2'}}}", gestalt.debugPrint());
    }

    @Test
    public void encryptedAllNodes() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
            .addEncryptedSecret(".*")
            .build();

        gestalt.loadConfigs();
        Assertions.assertEquals("pass", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test2", gestalt.getConfig("db.name", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));

        Assertions.assertEquals("tags: Tags{[]} = MapNode{admin=ArrayNode{values=[EncryptedLeafNode{value='secret'}, " +
            "EncryptedLeafNode{value='secret'}]}, db=MapNode{password=EncryptedLeafNode{value='*****'}, " +
            "port=EncryptedLeafNode{value='secret'}, name=EncryptedLeafNode{value='secret'}}}", gestalt.debugPrint());
    }

    @Test
    public void testImportSubPath() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("sub.$include:1", "source=mapNode1");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("b", "b changed");
        configs2.put("c", "c");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode1", configs2))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("sub.c", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("sub.b", String.class));
    }

    @Test
    public void testImportNested() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include", "source=mapNode1");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("b", "b changed");
        configs2.put("c", "c");
        configs2.put("$include:1", "source=mapNode2");

        Map<String, String> configs3 = new HashMap<>();
        configs3.put("c", "c changed");
        configs3.put("d", "d");


        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode1", configs2))
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode2", configs3))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c changed", gestalt.getConfig("c", String.class));
        Assertions.assertEquals("d", gestalt.getConfig("d", String.class));
    }

    @Test
    public void testImportNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("path.b", "b changed");
        configs.put("path.c", "c");
        configs.put("$include:1", "source=node,path=path");


        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testImportNodeClasspath() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:-1", "source=classPath,resource=include.properties");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testImportFile() throws GestaltException {

        // Load the default property files from resources.
        URL fileNode = GestaltImportProcessorTest.class.getClassLoader().getResource("include.properties");
        File devFile = new File(fileNode.getFile());

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=file,file=" + devFile.getAbsolutePath());

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testTreatMissingValuesAsErrorEmptyPath() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("password", "test");
        //configs.put("uri", "somedatabase");
        configs.put("port", "3306");

        ConfigLoaderRegistry configLoaderRegistry = new ConfigLoaderRegistry();
        configLoaderRegistry.addLoader(new MapConfigLoader());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        DBInfo dbInfo = gestalt.getConfig("", DBInfo.class);

        Assertions.assertEquals("test", dbInfo.getPassword());
        Assertions.assertNull(dbInfo.getUri());
        Assertions.assertEquals(3306, dbInfo.getPort());

        DBInfo dbInfoDef = gestalt.getConfig("", new DBInfo(), DBInfo.class);

        Assertions.assertEquals("test", dbInfoDef.getPassword());
        Assertions.assertNull(dbInfoDef.getUri());
        Assertions.assertEquals(3306, dbInfoDef.getPort());

        Optional<DBInfo> dbInfoOpt = gestalt.getConfigOptional("", DBInfo.class);

        Assertions.assertEquals("test", dbInfoOpt.get().getPassword());
        Assertions.assertNull(dbInfoOpt.get().getUri());
        Assertions.assertEquals(3306, dbInfoOpt.get().getPort());
    }

    @Test
    public void testGettingEmptyPathWithNullField() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("password", "test");
        configs.put("uri", "somedatabase");
        // configs.put("port", "3306");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        DBInfo dbInfo = gestalt.getConfig("", DBInfo.class);
        Assertions.assertEquals("test", dbInfo.getPassword());
        Assertions.assertEquals("somedatabase", dbInfo.getUri());
        Assertions.assertEquals(0, dbInfo.getPort()); // 0 is the default value for int
    }

    @Test
    public void testGettingEmptyPathWithOptionalField() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("password", "test");
        configs.put("uri", "somedatabase");
        // configs.put("port", "3306");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build();

        gestalt.loadConfigs();

        DBInfoOptional dbInfo = gestalt.getConfig("", DBInfoOptional.class);
        Assertions.assertEquals("test", dbInfo.getPassword().orElse(""));
        Assertions.assertEquals("somedatabase", dbInfo.getUri().orElse(""));
        Assertions.assertEquals(0, dbInfo.getPort().orElse(0)); // 0 is the default value for int
    }

    @Test
    public void testGettingEmptyPathWithDefaultValue() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("password", "test");
        configs.put("uri", "somedatabase");
        // configs.put("port", "3306");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        DBInfoNoConstructor dbInfo = gestalt.getConfig("", DBInfoNoConstructor.class);
        Assertions.assertEquals("test", dbInfo.getPassword());
        Assertions.assertEquals("somedatabase", dbInfo.getUri());
        Assertions.assertEquals(100, dbInfo.getPort()); // 0 is the default value for int
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

    public static class TestReloadListener implements CoreReloadListener {

        int count = 0;

        @Override
        public void reload() {
            count++;
        }
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

        public HostMethodAnnotations() {
        }

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

    public static class SubService {
        private boolean isEnabled;
        private Connection service;

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public Connection getService() {
            return service;
        }

        public void setService(Connection service) {
            this.service = service;
        }
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

    public static class DBConnection {
        @Config(path = "host")
        private String uri;
        private int dbPort;
        private String dbPath;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public int getDbPort() {
            return dbPort;
        }

        public void setDbPort(int dbPort) {
            this.dbPort = dbPort;
        }

        public String getDbPath() {
            return dbPath;
        }

        public void setDbPath(String dbPath) {
            this.dbPath = dbPath;
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
