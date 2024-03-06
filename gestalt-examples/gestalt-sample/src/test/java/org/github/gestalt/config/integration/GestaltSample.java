package org.github.gestalt.config.integration;


import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.LogicalResponse;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.annotations.ConfigPrefix;
import org.github.gestalt.config.aws.config.AWSBuilder;
import org.github.gestalt.config.aws.s3.S3ConfigSourceBuilder;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.decoder.ProxyDecoderMode;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.git.GitConfigSourceBuilder;
import org.github.gestalt.config.google.storage.GCSConfigSourceBuilder;
import org.github.gestalt.config.guice.GestaltModule;
import org.github.gestalt.config.guice.InjectConfig;
import org.github.gestalt.config.post.process.transform.RandomTransformer;
import org.github.gestalt.config.post.process.transform.SystemPropertiesTransformer;
import org.github.gestalt.config.post.process.transform.TransformerPostProcessor;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.FileChangeReloadStrategy;
import org.github.gestalt.config.source.*;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.vault.config.VaultBuilder;
import org.github.gestalt.config.vault.config.VaultModuleConfig;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.utils.AttributeMap;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static software.amazon.awssdk.http.SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES;

@Testcontainers
public class GestaltSample {

    private static final String BUCKET_NAME = "testbucket";
    private static final String BUCKET_NAME_2 = "testbucket2";

    private static final String S3MOCK_VERSION = System.getProperty("s3mock.version", "latest");
    private static final Collection<String> INITIAL_BUCKET_NAMES = asList(BUCKET_NAME, BUCKET_NAME_2);
    private static final String TEST_ENC_KEYREF =
        "arn:aws:kms:us-east-1:1234567890:key/valid-test-key-ref";

    private static final String UPLOAD_FILE_NAME = "src/test/resources/default.properties";

    private S3Client s3Client;

    @Container
    private static final S3MockContainer s3Mock =
        new S3MockContainer(S3MOCK_VERSION)
            .withValidKmsKeys(TEST_ENC_KEYREF)
            .withInitialBuckets(String.join(",", INITIAL_BUCKET_NAMES));

    private static final String VAULT_TOKEN = "my-root-token";

    @SuppressWarnings("rawtypes")
    @Container
    private static final VaultContainer vaultContainer = new VaultContainer("hashicorp/vault:1.13.0").withVaultToken(VAULT_TOKEN);

    private static Vault vault;

    @BeforeAll
    public static void setupVault() throws VaultException {
        vaultContainer.start();

        final VaultConfig config = new VaultConfig()
            .address("http://" + vaultContainer.getHost() + ":" + vaultContainer.getFirstMappedPort())
            .token(VAULT_TOKEN)
            .build();

        vault = Vault.create(config);

        final Map<String, Object> secrets = new HashMap<>();
        secrets.put("timeout", "123");
        secrets.put("bookingIsEnabled", "true");
        secrets.put("bookingHost", "https://dev.booking.host.name");
        secrets.put("bookingPort", "443");

        // Write operation
        final LogicalResponse writeResponse = vault.logical().write("secret/path", secrets);
        Assertions.assertEquals(200, writeResponse.getRestResponse().getStatus());
    }

    @BeforeEach
    void setUp() {
        // Must create S3Client after S3MockContainer is started, otherwise we can't request the random
        // locally mapped port for the endpoint
        var endpoint = s3Mock.getHttpsEndpoint();
        s3Client = createS3ClientV2(endpoint);
    }

    protected S3Client createS3ClientV2(String endpoint) {
        return S3Client.builder()
            .region(Region.of("us-east-1"))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create("foo", "bar")))
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .endpointOverride(URI.create(endpoint))
            .httpClient(UrlConnectionHttpClient.builder().buildWithDefaults(
                AttributeMap.builder().put(TRUST_ALL_CERTIFICATES, Boolean.TRUE).build()))
            .build();
    }

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }

    @Test
    public void integrationTest() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // Load the default property files from resources.
        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        // Load the configurations, this will thow exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Test
    public void integrationTestNoCache() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // Load the default property files from resources.
        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.properties").build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        // Load the configurations, this will thow exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);
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
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/dev.properties").setTags(Tags.of("toy", "ball")).build())
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
    public void integrationTestDefaultTags() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // Load the default property files from resources.
        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").setTags(Tags.profile("test")).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).setTags(Tags.profile("test")).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).setTags(Tags.profile("test")).build())
            .setDefaultTags(Tags.profile("test"))
            .build();

        // Load the configurations, this will thow exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);
    }


    @Test
    public void testDontTreatEmptyCollectionAsErrors() throws GestaltException {

        String hoconStr = "database: {\n" +
                          "  global: {\n" +
                          "    volumes: []\n" +
                          "  }\n" +
                          "}\n";

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(StringConfigSourceBuilder.builder().setConfig(hoconStr).setFormat("conf").build())
            .build();

        gestalt.loadConfigs();

        try {
            List<String> admins = gestalt.getConfig("database.global.volumes", new TypeCapture<>() {});
            Assertions.assertEquals(0, admins.size());
        } catch (GestaltException e) {
            Assertions.fail("Should not reach here");
        }
    }

    @Test
    public void integrationTestProxyPassThrough() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // Load the default property files from resources.
        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build();

        // Load the configurations, this will thow exceptions if there are any errors.
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

        URL defaultFileURL = GestaltSample.class.getClassLoader().getResource("default.properties");
        File defaultFile = new File(defaultFileURL.getFile());

        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        Path tempFile = Files.createTempFile("gestalt", "dev.properties");
        tempFile.toFile().deleteOnExit();
        Files.write(tempFile, Files.readAllBytes(devFile.toPath()));

        devFile = tempFile.toFile();

        TestReloadListener reloadListener = new TestReloadListener();
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(FileConfigSourceBuilder.builder().setFile(defaultFile).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).addConfigReloadStrategy(new FileChangeReloadStrategy()).build())
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
        Assertions.assertTrue(gestalt.getConfig("db.unkown", true, Boolean.class));

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
        Assertions.assertTrue(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("db.unkown", true, Boolean.class));

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

        hosts = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<>() {
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
    public void integrationTestEnvVars() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        String urlFile = "https://raw.githubusercontent.com/gestalt-config/gestalt/main/gestalt-examples/gestalt-sample/src/test/resources/default.json";

        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
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
            .addSource(URLConfigSourceBuilder.builder().setSourceURL(urlFile).build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(EnvironmentConfigSourceBuilder.builder().build())
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

        URL employeeURL = GestaltSample.class.getClassLoader().getResource("employee");
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

    @Test
    public void integrationTestJson() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL defaultFileURL = GestaltSample.class.getClassLoader().getResource("default.json");
        File defaultFile = new File(defaultFileURL.getFile());

        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.json");
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

    @Test
    public void integrationTestYaml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.yml");
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

    @Test
    public void integrationTestHocon() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL defaultFileURL = GestaltSample.class.getClassLoader().getResource("default.conf");
        File defaultFile = new File(defaultFileURL.getFile());

        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.yml");
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

    @Test
    public void integrationTestToml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        URL defaultFileURL = GestaltSample.class.getClassLoader().getResource("default.conf");
        File defaultFile = new File(defaultFileURL.getFile());

        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.toml");
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

    @Test
    public void integrationGitTest() throws GestaltException, IOException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        Path configDirectory = Files.createTempDirectory("gitConfigIntegration");
        configDirectory.toFile().deleteOnExit();


        GitConfigSourceBuilder gitBuilder = GitConfigSourceBuilder.builder()
            .setRepoURI("https://github.com/gestalt-config/gestalt.git")
            .setConfigFilePath("gestalt-examples/gestalt-sample/src/test/resources/default.properties")
            .setLocalRepoDirectory(configDirectory);
        ConfigSourcePackage source = gitBuilder.build();

        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(source)
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
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

        s3Client.putObject(
            PutObjectRequest.builder().bucket(BUCKET_NAME).key(uploadFile.getName()).build(),
            RequestBody.fromFile(uploadFile));

        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(S3ConfigSourceBuilder
                .builder()
                .setS3(s3Client)
                .setBucketName(BUCKET_NAME)
                .setKeyName(uploadFile.getName())
                .build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    @Tag("cloud")
    @Test
    public void integrationTestGoogleCloud() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // Load the default property files from resources.
        // Contents of the dev.properties on GCP Storage, note the use of ${gcpSecret:booking-host}
        // the secrets "bank-host" value is "booking.host.name"
        /*
        db.hosts[0].url=jdbc:postgresql://dev.host.name1:5432/mydb
        db.hosts[1].url=jdbc:postgresql://dev.host.name2:5432/mydb
        db.hosts[2].url=jdbc:postgresql://dev.host.name3:5432/mydb
        db.connectionTimeout=600

        http.pool.maxTotal=1000
        http.pool.maxPerRoute=50

        subservice.booking.service.isEnabled=true
        subservice.booking.service.host=https://dev.${gcpSecret:bank-host}
        subservice.booking.service.port=443
        subservice.booking.service.path=booking

        subservice.search.service.isEnabled=false

        admin.user=Peter, Kim, Steve
        admin.overrideEnabled=true
         */


        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())
            .addSource(GCSConfigSourceBuilder.builder().setBucketName("gestalt-test").setObjectName("dev.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        // Load the configurations, this will thow exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);


        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
    }

    @Tag("cloud")
    @Test
    public void integrationTestAws() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // Load the default property files from resources.
        // Contents of the dev.properties on aws Storage, note the use of ${awsSecret:booking-host:host}
        // the secrets "booking-host:host" value is "booking.host.name"
        /*
        db.hosts[0].url=jdbc:postgresql://dev.host.name1:5432/mydb
        db.hosts[1].url=jdbc:postgresql://dev.host.name2:5432/mydb
        db.hosts[2].url=jdbc:postgresql://dev.host.name3:5432/mydb
        db.connectionTimeout=600

        http.pool.maxTotal=1000
        http.pool.maxPerRoute=50

        subservice.booking.service.isEnabled=true
        subservice.booking.service.host=https://dev.${awsSecret:booking-host:host}
        subservice.booking.service.port=443
        subservice.booking.service.path=booking

        subservice.search.service.isEnabled=false

        admin.user=Peter, Kim, Steve
        admin.overrideEnabled=true
         */
        S3Client s3Client = S3Client.builder()
                                    .credentialsProvider(DefaultCredentialsProvider.create())
                                    .region(Region.US_EAST_1)
                                    .httpClient(UrlConnectionHttpClient.builder().build())
                                    .build();


        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())
            .addSource(S3ConfigSourceBuilder.builder().setS3(s3Client).setBucketName("gestalt-test").setKeyName("dev.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(AWSBuilder.builder().setRegion("us-east-1").build())
            .build();

        // Load the configurations, this will thow exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);


        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
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

        Map<String, Integer> httpPoolMap = gestalt.getConfig("http.pool", new TypeCapture<>() { });

        Assertions.assertEquals(50, httpPoolMap.get("maxperroute"));
        Assertions.assertEquals(6000, httpPoolMap.get("validateafterinactivity"));
        Assertions.assertEquals(60000, httpPoolMap.get("keepalivetimeoutms"));
        Assertions.assertEquals(25, httpPoolMap.get("idletimeoutsec"));

        Map<String, Integer> poolMap = gestalt.getConfig("http", new TypeCapture<>() { });

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

        // result that guice gets the injected config.
        Injector injector = Guice.createInjector(new GestaltModule(gestalt));
        DBQueryService dbService = injector.getInstance(DBQueryService.class);
        db = dbService.getDataBase();
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
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/defaultPPEnv.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("integration.properties").build())
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
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("defaultPPSys.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/integration.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addPostProcessor(new TransformerPostProcessor(List.of(new SystemPropertiesTransformer(), new RandomTransformer())))
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
        Assertions.assertNotNull(gestalt.getConfig("appUUID", UUID.class));
        Assertions.assertTrue(gestalt.getConfig("appId", Integer.class) == 20 ||
            gestalt.getConfig("appId", Integer.class) == 21);
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
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/integration.properties").build())
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
    public void integrationTestPostProcessorVault() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        VaultModuleConfig vaultModuleConfig = VaultBuilder.builder().setVault(vault).build();

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/defaultPPVault.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/integration.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(vaultModuleConfig)
            .build();

        gestalt.loadConfigs();

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());

        validateResults(gestalt);


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
    public void testSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello ${place} it is ${weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
            .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello world it is sunny today", message);
    }

    @Test
    public void testNestedSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("variable", "place");
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello ${${variable}} it is ${weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
            .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello world it is sunny today", message);
    }

    @Test
    public void testEscapedSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello \\${place} it is ${weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
            .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello ${place} it is sunny today", message);
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

    public static class DBQueryService {
        private @InjectConfig(path = "db") DataBase dataBase;

        public DataBase getDataBase() {
            return dataBase;
        }

        public void setDataBase(DataBase dataBase) {
            this.dataBase = dataBase;
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
