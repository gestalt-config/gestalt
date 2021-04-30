package org.github.gestalt.config.integration;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.aws.s3.S3ConfigSource;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.post.process.transform.SystemPropertiesTransformer;
import org.github.gestalt.config.post.process.transform.TransformerPostProcessor;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.FileChangeReloadStrategy;
import org.github.gestalt.config.source.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class GestaltSampleJavaLatest {

    @RegisterExtension
    static final S3MockExtension S3_MOCK =
        S3MockExtension.builder().silent().withSecureConnection(false).build();

    private static final String BUCKET_NAME = "testbucket";
    private static final String UPLOAD_FILE_NAME = "src/test/resources/default.properties";

    private final S3Client s3Client = S3_MOCK.createS3ClientV2();

    @Test
    public void integrationTest() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL devFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new ClassPathConfigSource("/default.properties"))
            .addSource(new FileConfigSource(devFile))
            .addSource(new MapConfigSource(configs))
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Test
    @Disabled
    public void integrationTestReloadFile() throws GestaltException, IOException, InterruptedException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL defaultFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("default.properties");
        File defaultFile = new File(defaultFileURL.getFile());

        URL devFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        Path tempFile = Files.createTempFile("gestalt", "dev.properties");
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, Files.readAllBytes(devFile.toPath()));

        devFile = tempFile.toFile();

        ConfigSource devFileSource = new FileConfigSource(devFile);

        TestReloadListener reloadListener = new TestReloadListener();
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new FileConfigSource(defaultFile))
            .addSource(devFileSource)
            .addSource(new MapConfigSource(configs))
            .addReloadStrategy(new FileChangeReloadStrategy(devFileSource))
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
        Assertions.assertNull(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("db.isEnabled", true, Boolean.class));

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.get(0).user());
        Assertions.assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test", String.class));
        Assertions.assertEquals("1234", db.hosts.get(0).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", db.hosts.get(0).url);
        Assertions.assertEquals("credmond", db.hosts.get(1).user());
        Assertions.assertEquals("5678", db.hosts.get(1).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", db.hosts.get(1).url);
        Assertions.assertEquals("credmond", db.hosts.get(2).user());
        Assertions.assertEquals("9012", db.hosts.get(2).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", db.hosts.get(2).url);

        Assertions.assertEquals("test", gestalt.getConfig("db.does.not.exist", "test", String.class));

        List<Host> hosts = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<List<Host>>() {
        });
        Assertions.assertEquals(3, hosts.size());
        Assertions.assertEquals("credmond", hosts.get(0).user());
        Assertions.assertEquals("1234", hosts.get(0).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hosts.get(0).url);
        Assertions.assertEquals("credmond", hosts.get(1).user());
        Assertions.assertEquals("5678", hosts.get(1).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hosts.get(1).url);
        Assertions.assertEquals("credmond", hosts.get(2).user());
        Assertions.assertEquals("9012", hosts.get(2).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hosts.get(2).url);

        List<Host> noHosts = gestalt.getConfig("db.not.hosts", Collections.emptyList(), new TypeCapture<List<Host>>() {
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
        Files.write(devFile.toPath(), config.getBytes(UTF_8));

        // Reloads sometimes take a little bit of time, so wait till the update has been triggered.
        for (int i = 0; i < 10; i++) {
            if (reloadListener.count > 1) {
                break;
            } else {
                Thread.sleep(100);
            }
        }
        db = gestalt.getConfig("db", DataBase.class);
        Assertions.assertTrue(reloadListener.count >= 1);

        Assertions.assertEquals(2222, db.connectionTimeout);
        Assertions.assertEquals(2222, gestalt.getConfig("db.connectionTimeout", Integer.class));
        Assertions.assertEquals(123, db.idleTimeout);
        Assertions.assertEquals(60000.0F, db.maxLifetime);
        Assertions.assertNull(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("db.isEnabled", true, Boolean.class));

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.get(0).user());
        Assertions.assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test", String.class));
        Assertions.assertEquals("1234", db.hosts.get(0).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb2", db.hosts.get(0).url);
        Assertions.assertEquals("credmond", db.hosts.get(1).user());
        Assertions.assertEquals("5678", db.hosts.get(1).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb2", db.hosts.get(1).url);
        Assertions.assertEquals("credmond", db.hosts.get(2).user());
        Assertions.assertEquals("9012", db.hosts.get(2).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb2", db.hosts.get(2).url);

        Assertions.assertEquals("test", gestalt.getConfig("db.does.not.exist", "test", String.class));

        hosts = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<List<Host>>() {
        });
        Assertions.assertEquals(3, hosts.size());
        Assertions.assertEquals("credmond", hosts.get(0).user());
        Assertions.assertEquals("1234", hosts.get(0).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb2", hosts.get(0).url);
        Assertions.assertEquals("credmond", hosts.get(1).user());
        Assertions.assertEquals("5678", hosts.get(1).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb2", hosts.get(1).url);
        Assertions.assertEquals("credmond", hosts.get(2).user());
        Assertions.assertEquals("9012", hosts.get(2).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb2", hosts.get(2).url);
    }

    @Test
    public void integrationTestEnvVars() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        String urlFile = "https://raw.githubusercontent.com/gestalt-config/gestalt/main/gestalt-sample/src/test/resources/default.json";

        URL devFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        /*
        Expects the following environment variables
            DB_IDLETIMEOUT: 123
            SUBSERVICE_BOOKING_ISENABLED: true
            SUBSERVICE_BOOKING_SERVICE_HOST: https://dev.booking.host.name
            SUBSERVICE_BOOKING_SERVICE_PORT: 443
         */

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new URLConfigSource(urlFile))
            .addSource(new FileConfigSource(devFile))
            .addSource(new MapConfigSource(configs))
            .addSource(new EnvironmentConfigSource())
            .setEnvVarsTreatErrorsAsWarnings(true)
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.service().host());
        Assertions.assertEquals(443, booking.service().port());
        Assertions.assertEquals("booking", booking.service().path());
    }

    @Test
    public void integrationTestJson() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL defaultFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("default.json");
        File defaultFile = new File(defaultFileURL.getFile());

        URL devFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("dev.json");
        File devFile = new File(devFileURL.getFile());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new FileConfigSource(defaultFile))
            .addSource(new FileConfigSource(devFile))
            .addSource(new MapConfigSource(configs))
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Test
    public void integrationTestYaml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL devFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("dev.yml");
        File devFile = new File(devFileURL.getFile());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new ClassPathConfigSource("/default.yml"))
            .addSource(new FileConfigSource(devFile))
            .addSource(new MapConfigSource(configs))
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Test
    public void integrationTestJsonAndYaml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new ClassPathConfigSource("/default.json"))
            .addSource(new ClassPathConfigSource("/dev.yml"))
            .addSource(new MapConfigSource(configs))
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Test
    public void integrationTestHocon() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL defaultFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("default.conf");
        File defaultFile = new File(defaultFileURL.getFile());

        URL devFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("dev.yml");
        File devFile = new File(devFileURL.getFile());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new FileConfigSource(defaultFile))
            .addSource(new FileConfigSource(devFile))
            .addSource(new MapConfigSource(configs))
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Test
    public void integrationS3Test() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        final File uploadFile = new File(UPLOAD_FILE_NAME);

        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
        s3Client.putObject(
            PutObjectRequest.builder().bucket(BUCKET_NAME).key(uploadFile.getName()).build(),
            RequestBody.fromFile(uploadFile));


        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, uploadFile.getName());

        URL devFileURL = GestaltSampleJavaLatest.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(source)
            .addSource(new FileConfigSource(devFile))
            .addSource(new MapConfigSource(configs))
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

        long startTime = System.nanoTime();
        gestalt.getConfig("db", DataBase.class);
        long timeTaken = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        DataBase db = gestalt.getConfig("db", DataBase.class);
        long cacheTimeTaken = System.nanoTime() - startTime;

        // not really a great test for ensuring we are hitting a cache
        Assertions.assertTrue(timeTaken > cacheTimeTaken);

        Assertions.assertEquals(600, db.connectionTimeout);
        Assertions.assertEquals(600, gestalt.getConfig("DB.connectionTimeout", Integer.class));
        Assertions.assertEquals(123, db.idleTimeout);
        Assertions.assertEquals(60000.0F, db.maxLifetime);
        Assertions.assertNull(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("db.isEnabled", true, Boolean.class));

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.get(0).user());
        // index into the path of an array.
        Assertions.assertEquals("credmond", gestalt.getConfig("db.hosts[0].user", "test", String.class));
        Assertions.assertEquals("1234", db.hosts.get(0).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", db.hosts.get(0).url);
        Assertions.assertEquals("credmond", db.hosts.get(1).user());
        Assertions.assertEquals("5678", db.hosts.get(1).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", db.hosts.get(1).url);
        Assertions.assertEquals("credmond", db.hosts.get(2).user());
        Assertions.assertEquals("9012", db.hosts.get(2).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", db.hosts.get(2).url);

        Assertions.assertEquals("test", gestalt.getConfig("db.does.not.exist", "test", String.class));

        List<Host> hosts = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<List<Host>>() {
        });
        Assertions.assertEquals(3, hosts.size());
        Assertions.assertEquals("credmond", hosts.get(0).user());
        Assertions.assertEquals("1234", hosts.get(0).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name1:5432/mydb", hosts.get(0).url);
        Assertions.assertEquals("credmond", hosts.get(1).user());
        Assertions.assertEquals("5678", hosts.get(1).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name2:5432/mydb", hosts.get(1).url);
        Assertions.assertEquals("credmond", hosts.get(2).user());
        Assertions.assertEquals("9012", hosts.get(2).password());
        Assertions.assertEquals("jdbc:postgresql://dev.host.name3:5432/mydb", hosts.get(2).url);

        List<Host> noHosts = gestalt.getConfig("db.not.hosts", Collections.emptyList(), new TypeCapture<List<Host>>() {
        });
        Assertions.assertEquals(0, noHosts.size());

        User admin = gestalt.getConfig("admin", new TypeCapture<User>() {
        });
        Assertions.assertEquals(3, admin.user.length);
        Assertions.assertEquals("Peter", admin.user[0]);
        Assertions.assertEquals("Kim", admin.user[1]);
        Assertions.assertEquals("Steve", admin.user[2]);
        Assertions.assertEquals(Role.LEVEL0, admin.accessRole);
        Assertions.assertTrue(admin.overrideEnabled);

        User user = gestalt.getConfig("employee", new TypeCapture<User>() {
        });
        Assertions.assertEquals(1, user.user.length);
        Assertions.assertEquals("Janice", user.user[0]);
        Assertions.assertEquals(Role.LEVEL1, user.accessRole);
        Assertions.assertFalse(user.overrideEnabled);

        Assertions.assertEquals("active", gestalt.getConfig("serviceMode", TypeCapture.of(String.class)));
        Assertions.assertEquals('a', gestalt.getConfig("serviceMode", TypeCapture.of(Character.class)));
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
            .addSource(new ClassPathConfigSource("/defaultPPEnv.properties"))
            .addSource(new ClassPathConfigSource("/integration.properties"))
            .addSource(new MapConfigSource(configs))
            .setEnvVarsTreatErrorsAsWarnings(true)
            .addDefaultPostProcessors()
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.service().host());
        Assertions.assertEquals(443, booking.service().port());
        Assertions.assertEquals("booking", booking.service().path());
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
            .addSource(new ClassPathConfigSource("/defaultPPSys.properties"))
            .addSource(new ClassPathConfigSource("/integration.properties"))
            .addSource(new MapConfigSource(configs))
            .setEnvVarsTreatErrorsAsWarnings(true)
            .addPostProcessor(new TransformerPostProcessor(Collections.singletonList(new SystemPropertiesTransformer())))
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.service().host());
        Assertions.assertEquals(443, booking.service().port());
        Assertions.assertEquals("booking", booking.service().path());
    }

    @Test
    public void integrationTestPostProcessorNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new ClassPathConfigSource("/defaultPPNode.properties"))
            .addSource(new ClassPathConfigSource("/integration.properties"))
            .addSource(new MapConfigSource(configs))
            .setEnvVarsTreatErrorsAsWarnings(true)
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.service().host());
        Assertions.assertEquals(443, booking.service().port());
        Assertions.assertEquals("booking", booking.service().path());
    }

    public enum Role {
        LEVEL0, LEVEL1
    }

    public static class TestReloadListener implements CoreReloadListener {

        int count = 0;

        @Override
        public void reload() {
            count++;
        }
    }

    public static record HttpPool(short maxTotal, long maxPerRoute, int validateAfterInactivity, double keepAliveTimeoutMs,
                                  int idleTimeoutSec, float defaultWait) {
    }


    public static record Host(String user, String url, String password) {
    }

    public static record DataBase(List<Host> hosts, int connectionTimeout, Integer idleTimeout, float maxLifetime, Boolean isEnabled) {
    }

    public static class User {
        public String[] user;
        public Boolean overrideEnabled = false;
        public Role accessRole;
    }

    public static record SubService(boolean isEnabled, Connection service) {
    }


    public static record Connection(String host, int port, String path) {
    }
}
