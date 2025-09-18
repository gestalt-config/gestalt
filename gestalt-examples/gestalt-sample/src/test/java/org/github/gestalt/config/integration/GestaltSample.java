package org.github.gestalt.config.integration;


import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.github.gestalt.config.validation.hibernate.builder.HibernateModuleBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.response.LogicalResponse;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.annotation.Nullable;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.annotations.ConfigPrefix;
import org.github.gestalt.config.aws.config.AWSBuilder;
import org.github.gestalt.config.aws.s3.S3ConfigSourceBuilder;
import org.github.gestalt.config.azure.blob.BlobConfigSourceBuilder;
import org.github.gestalt.config.azure.config.AzureModuleBuilder;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.decoder.ProxyDecoderMode;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.git.GitConfigSourceBuilder;
import org.github.gestalt.config.git.builder.GitModuleConfigBuilder;
import org.github.gestalt.config.google.storage.GCSConfigSourceBuilder;
import org.github.gestalt.config.guice.GestaltModule;
import org.github.gestalt.config.guice.InjectConfig;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.PathLexerBuilder;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.EnvironmentVarsLoaderModuleConfigBuilder;
import org.github.gestalt.config.micrometer.builder.MicrometerModuleConfigBuilder;
import org.github.gestalt.config.node.TagMergingStrategyCombine;
import org.github.gestalt.config.processor.config.transform.LoadtimeStringSubstitutionConfigNodeProcessor;
import org.github.gestalt.config.processor.config.transform.RandomTransformer;
import org.github.gestalt.config.processor.config.transform.SystemPropertiesTransformer;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.reload.FileChangeReloadStrategy;
import org.github.gestalt.config.secret.rules.MD5SecretObfuscator;
import org.github.gestalt.config.source.*;
import org.github.gestalt.config.node.factory.MapNodeImportFactory;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.SystemWrapper;
import org.github.gestalt.config.vault.config.VaultBuilder;
import org.github.gestalt.config.vault.config.VaultModuleConfig;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.LogManager;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.github.gestalt.config.lexer.PathLexer.DEFAULT_EVALUATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static software.amazon.awssdk.http.SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GestaltSample {

    private static final String BUCKET_NAME = "testbucket";
    private static final String BUCKET_NAME_2 = "testbucket2";

    private static final String S3MOCK_VERSION = System.getProperty("s3mock.version", "latest");
    private static final Collection<String> INITIAL_BUCKET_NAMES = asList(BUCKET_NAME, BUCKET_NAME_2);
    private static final String TEST_ENC_KEYREF =
        "arn:aws:kms:us-east-1:1234567890:key/valid-test-key-ref";

    private static final String UPLOAD_FILE_NAME = "src/test/resources/default.properties";

    private S3Client s3Client;

    private static final String BLOB_UPLOAD_FILE_NAME = "src/test/resources/include.properties";
    private static final String connectionString = "AccountName=devstoreaccount1;" +
        "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;" +
        "DefaultEndpointsProtocol=http;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;" +
        "QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;TableEndpoint=http://127.0.0.1:10002/devstoreaccount1;";
    @Container
    private static final GenericContainer<?> azureStorage =
        new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:3.35.0"))
            .withExposedPorts(10000, 10001, 10002);
    private static final String testContainer = "testcontainer";
    private static final String testBlobName = "testBlobName.properties";
    private static BlobClient blobClient;

    @Container
    private static final S3MockContainer s3Mock =
        new S3MockContainer(S3MOCK_VERSION)
            .withValidKmsKeys(TEST_ENC_KEYREF)
            .withInitialBuckets(String.join(",", INITIAL_BUCKET_NAMES));

    private static final String VAULT_TOKEN = "my-root-token";

    @SuppressWarnings("rawtypes")
    @Container
    private static final VaultContainer vaultContainer = new VaultContainer("hashicorp/vault:1.18.0").withVaultToken(VAULT_TOKEN);

    private static Vault vault;

    @BeforeAll
    public static void setupVault() throws VaultException, FileNotFoundException {
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

        // setup Azure Storage
        azureStorage.start();

        StorageSharedKeyCredential credential = StorageSharedKeyCredential.fromConnectionString(connectionString);

        BlobContainerClient container = new BlobServiceClientBuilder()
            .endpoint("http://127.0.0.1:" + azureStorage.getFirstMappedPort() + "/devstoreaccount1")
            .credential(credential)
            .buildClient()
            .getBlobContainerClient(testContainer);

        if (!container.exists()) {
            container.create();
        }

        blobClient = container.getBlobClient(testBlobName);

        final File uploadFile = new File(BLOB_UPLOAD_FILE_NAME);

        Assertions.assertTrue(uploadFile.exists());
        InputStream fileStream = new FileInputStream(uploadFile);
        blobClient.upload(fileStream);
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
        try (InputStream is = GestaltSample.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // This example shows a how to load multiple sources and how the priority of the configurations
    // is dictated by the order they are added.
    // So the default.properties has the lowest priority and if any other source provides
    // a configuration on the same path it will be overwritten.
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

        // Load the configurations, this will throw exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    // This example shows a how to disable the gestalt configuration.
    // So each call will get decode the value from the config tree.
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

    // This example shows a how to provide a default configuration then tag source.
    // Then how to get the configuration with the tag with a falllback to the default or if present the taged version.
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

    // This example shows a how to use tag source files and use a default tag to always get values using the tag.
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

        // Load the configurations, this will throw exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);
    }


    // This example shows a how to configure gestalt to allow empty collections.
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

    // This example shows a how to get an interface that is provided with a proxy in pass thgrough mode.
    // So each call to the proxy will go to gestalt to lookup the data.
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


    // This example shows a how to use dynamic configurations and reload a config.
    @Test
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

    // This example shows a how to load Environment Variables as a source and layer them over a properties file.
    // the Env Vars will be parsed into a path where DB_IDLETIMEOUT becomes "db.idletimeout"
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
    public void testK8secrets() throws GestaltException, URISyntaxException {

        // Load the default property files from resources.
        URL testFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
        Path testFileDir = Paths.get(testFileURL.toURI());
        Path kubernetesPath = testFileDir.getParent().resolve("kubernetes");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(KubernetesSecretConfigSourceBuilder.builder().setPath(kubernetesPath).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("abcdef", gestalt.getConfig("db.host.password", String.class));
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/mydb1", gestalt.getConfig("db.host.uri", String.class));
        Assertions.assertEquals(111222333, gestalt.getConfig("subservice.booking.token", Integer.class));
    }

    // This example shows a how multiple string substitutions can work from diffrent loacations in the same config file.
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

    // This example shows a how to load a Json source.
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

    // This example shows a how to load a Yaml source.
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

    // This example shows a how to load a Hocon source.
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

    // This example shows a how to load a TOML source.
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

    // This example shows a how to load a source from a git project.
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

    // This example shows a how to load a source from an S3 bucket.
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

    // This example shows a how to load a source from an S3 bucket.
    @Test
    public void integrationS3NodeSubstitutionTest() throws GestaltException {
        final File uploadFile = new File(UPLOAD_FILE_NAME);

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");
        configs.put("$include", "source=s3,bucket=" + BUCKET_NAME + ",key=" + uploadFile.getName());

        s3Client.putObject(
            PutObjectRequest.builder().bucket(BUCKET_NAME).key(uploadFile.getName()).build(),
            RequestBody.fromFile(uploadFile));

        URL devFileURL = GestaltSample.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(AWSBuilder.builder().setS3Client(s3Client).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    // This example shows a how to load a source from a GCP storage.
    // must be logged in to run test
    // gcloud init
    // gcloud auth login
    // gcloud auth application-default login
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

    // This example shows a how to load a source from an S3 bucket.
    // must be logged in to run the test
    // aws configure
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
        subservice.booking.isEnabled=true

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

    // This example shows a how to load a source from an Blob bucket.
    // must be logged in to run the test
    // azure configure
    // to log in you must run winget install --id=Microsoft.AzureCLI or install if for your OS
    // ro instal Azure CLI: https://learn.microsoft.com/en-us/cli/azure/install-azure-cli.
    // then run
    // az login
    @Tag("cloud")
    @Test
    public void integrationTestAzure() throws GestaltException {
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
        subservice.booking.service.host=https://dev.${azureSecret:bookingHost}
        subservice.booking.service.port=443
        subservice.booking.service.path=booking
        subservice.booking.isEnabled=true

        subservice.search.service.isEnabled=false

        admin.user=Peter, Kim, Steve
        admin.overrideEnabled=true
         */

        String azureAccount = System.getenv("AZURE_ACCOUNT");

        var credentials = new DefaultAzureCredentialBuilder().build();
        // verify we are logged in and have access
        //The secret bookingHost = booking.host.name
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl("https://" + azureAccount+ ".vault.azure.net")
            .credential(credentials)
            .buildClient();

        //ensure we are logged in and can access azure
        Assertions.assertEquals("booking.host.name", secretClient.getSecret("bookingHost").getValue());

        // to generate the SAS go to the Azure portal, storage accounts, select the storage account the the data,
        // then generate a Shared access signature
        String azureSASUrl = System.getenv("AZURE_SAS_URL");
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .endpoint(azureSASUrl)
            .buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("gestalt");

        // ensure we have access and can view the item.
        for (BlobItem blobItem : containerClient.listBlobs()) {
            Assertions.assertEquals("dev.properties", blobItem.getName());
        }

        BlobClient blobClient = containerClient.getBlobClient("dev.properties");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())
            .addSource(BlobConfigSourceBuilder.builder()
                .setBlobClient(blobClient)
                .build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(AzureModuleBuilder.builder().setSecretClient(secretClient).build())
            .build();

        // Load the configurations, this will throw exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
    }

    @Test
    void integrationIncludeAzureTest() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=blob");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(AzureModuleBuilder.builder().setBlobClient(blobClient).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
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

    // This example shows a how to use the string substitution with Environment variable substitutions.
    //in the defaultPPEnv file it contains the properties
    // db.idleTimeout=${env:DB_IDLETIMEOUT:=900}
    //which maps to the Environment properties,
    // DB_IDLETIMEOUT: 123
    //so in the sample the result is 123, not if the Environment Variable was missing the default value would be 900.
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

    // This example shows a how to use the string substitution with System variable substitutions.
    //in the defaultPPSys file it contains the properties
    // db.idleTimeout=${DB_IDLETIMEOUT}
    //which maps to the system properties, although it is also defined in an Env Var,
    // it will pick the system property since the transform has a higher priority.
    // System.getProperties().put("DB_IDLETIMEOUT", "123");
    //so in the sample the result is 123
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
            .addConfigNodeProcessor(new LoadtimeStringSubstitutionConfigNodeProcessor(List.of(new SystemPropertiesTransformer(), new RandomTransformer())))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals(123, gestalt.getConfig("db.idleTimeout", Integer.class));

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

    // This example shows a how to use the string substitution with node substitutions.
    //in the defaultPPNode file it contains the properties
    //db.idleTimeout=${node:alternate.db.idleTimeout}
    //which maps to the properties
    //alternate.db.idleTimeout=123
    //so in the sample the result is 123
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

        Assertions.assertEquals(123, gestalt.getConfig("db.idleTimeout", Integer.class));

        validateResults(gestalt);

        SubService booking = gestalt.getConfig("subservice.booking", TypeCapture.of(SubService.class));
        Assertions.assertTrue(booking.isEnabled());
        Assertions.assertEquals("https://dev.booking.host.name", booking.getService().getHost());
        Assertions.assertEquals(443, booking.getService().getPort());
        Assertions.assertEquals("booking", booking.getService().getPath());
    }

    // This example shows a how to use the vault string substitution.
    // in the defaultPPVault file it contains the properties
    //subservice.booking.service.host=${vault:secret/path:bookingHost}
    //subservice.booking.service.port=${vault:secret/path:bookingPort}
    // that will be replaced by values in the vault. which has secret values setup in the test container
    // secrets.put("bookingHost", "https://dev.booking.host.name");
    // secrets.put("bookingPort", "443");
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

    // This example shows a how-to camel case path mapper can be used to convert dbPath into a path of "db.path"
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

    // This example shows a how to use string substitution to reference another node.
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

    // This example shows how to use nested string substitution.
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

    // This show an example of how escape substitution can be used in a string substitution
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

    // This shows an example of a metrics being exposed and how to configure them.
    @Test
    public void testMetrics() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setObservationsEnabled(true)
            .addModuleConfig(MicrometerModuleConfigBuilder.builder()
                .setMeterRegistry(registry)
                .setPrefix("myApp")
                .build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.password", String.class));

        org.assertj.core.api.Assertions.assertThat(registry.getMetersAsString())
                .startsWith("myApp.config.get(TIMER)[]; count=1.0, total_time=");

        Assertions.assertEquals("test", gestalt.getConfig("db.password", String.class));

        org.assertj.core.api.Assertions.assertThat(registry.getMetersAsString())
            .contains("myApp.config.get(TIMER)[]; count=1.0, total_time=")
            .contains("myApp.cache.hit(COUNTER)[]; count=1.0");
    }

    // This shows an example of a hibernate validator returning an ok and returning the object.
    @Test
    public void testValidationOk() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setValidationEnabled(true)
            .addModuleConfig(HibernateModuleBuilder.builder()
                .setValidator(validator)
                .build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("test", gestalt.getConfig("db", DBInfo.class).password);
        Assertions.assertEquals(123, gestalt.getConfig("db", DBInfo.class).port);

        Assertions.assertEquals("test", gestalt.getConfig("db", DBInfoValid.class).password);
        Assertions.assertEquals(123, gestalt.getConfig("db", DBInfoValid.class).port);
    }

    // This shows an example of a hibernate validator returning an error.
    @Test
    public void testValidationError() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "12345678901234567890");
        configs.put("db.port", "0");
        configs.put("db.uri", "my.sql.com");

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setValidationEnabled(true)
            .addModuleConfig(HibernateModuleBuilder.builder()
                .setValidator(validator)
                .build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("12345678901234567890", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("12345678901234567890", gestalt.getConfig("db", DBInfo.class).password);
        Assertions.assertEquals(0, gestalt.getConfig("db", DBInfo.class).port);

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoValid.class));
        org.assertj.core.api.Assertions.assertThat(ex.getMessage())
            .startsWith("Validation failed for config path: db, and " +
                "class: org.github.gestalt.config.integration.GestaltSample$DBInfoValid")
            .contains("- level: ERROR, message: Hibernate Validator, on path: db, error: size must be between 2 and 14")
            .contains("- level: ERROR, message: Hibernate Validator, on path: db, error: port should not be less than 10");
    }

    // This shows how to customise the sentence lexer used to generate paths so it is case sensative.
    @Test
    public void testCustomSentenceLexerCaseSensitive() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.uri", "test");
        configs.put("db.port", "3306");
        configs.put("db.URI", "TEST");
        configs.put("db.PORT", "1234");
        configs.put("db.password", "abc123");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            // do not normalize the sentence return it as is.
            .setSentenceLexer(new PathLexer(".", DEFAULT_EVALUATOR, (it) -> it))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.uri", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));

        Assertions.assertEquals("TEST", gestalt.getConfig("db.URI", String.class));
        Assertions.assertEquals("1234", gestalt.getConfig("db.PORT", String.class));

        Assertions.assertEquals("abc123", gestalt.getConfig("db.password", String.class));

        Assertions.assertTrue(gestalt.getConfigOptional("db.Uri", String.class).isEmpty());

        Assertions.assertEquals("test", gestalt.getConfig("db", DBInfo.class).getUri());
        Assertions.assertEquals(3306, gestalt.getConfig("db", DBInfo.class).getPort());
        Assertions.assertEquals("abc123", gestalt.getConfig("db", DBInfo.class).getPassword());
    }

    // This shows how to customise the Environment Variables format so instead of requiring
    // a single "_" it requires a "__" to delineate the segments in the path.
    @Test
    public void integrationTestCustomEnvironmentVariablesStyle() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("DB__URI", "test");
        configs.put("DB__PORT", "3306");
        configs.put("DB__PASSWORD", "abc123");

        try (MockedStatic<SystemWrapper> mocked = mockStatic(SystemWrapper.class)) {
            mocked.when(SystemWrapper::getEnvVars).thenReturn(configs);

            GestaltBuilder builder = new GestaltBuilder();
            Gestalt gestalt = builder
                .addSource(EnvironmentConfigSourceBuilder.builder().build())
                .addModuleConfig(EnvironmentVarsLoaderModuleConfigBuilder
                    .builder()
                    .setLexer(new PathLexer("__"))
                    .build())
                .build();

            gestalt.loadConfigs();

            Assertions.assertEquals("test", gestalt.getConfig("db.uri", String.class));
            Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));
            Assertions.assertEquals("abc123", gestalt.getConfig("db.password", String.class));

            Assertions.assertEquals("test", gestalt.getConfig("db", DBInfo.class).getUri());
            Assertions.assertEquals(3306, gestalt.getConfig("db", DBInfo.class).getPort());
            Assertions.assertEquals("abc123", gestalt.getConfig("db", DBInfo.class).getPassword());
        }
    }

    @Test
    public void testRelaxedLexer() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.uri", "test");
        configs.put("db_port", "3306");
        configs.put("db-password", "abc123");
        configs.put("dbTimeout", "1000");

        SentenceLexer lexer = PathLexerBuilder.builder()
            .setDelimiter("([._-])|(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[0-9])(?=[A-Z][a-z])|(?<=[a-zA-Z])(?=[0-9])")
            .build();

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            // do not normalize the sentence return it as is.
            .setSentenceLexer(lexer)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.uri", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));


        Assertions.assertEquals("abc123", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("1000", gestalt.getConfig("db.timeout", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db", DBInfo.class).getUri());
        Assertions.assertEquals(3306, gestalt.getConfig("db", DBInfo.class).getPort());
        Assertions.assertEquals("abc123", gestalt.getConfig("db", DBInfo.class).getPassword());
    }

    @Test
    public void testSecretMaskingHash() throws GestaltException, NoSuchAlgorithmException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "abcdef");
        configs.put("db.uri", "my.sql.com");
        configs.put("db.salt", "pepper");
        configs.put("db.secret.user", "12345");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.CACHE)
            .setSecretObfuscation(new MD5SecretObfuscator())
            .useCacheDecorator(false)
            .build();

        gestalt.loadConfigs();

        String rootNode = gestalt.debugPrint(Tags.of());

        Assertions.assertEquals("MapNode{db=MapNode{password=LeafNode{value='098f6bcd4621d373cade4e832627b4f6'}, " +
            "salt=LeafNode{value='b3f952d5d9adea6f63bee9d4c6fceeaa'}, port=LeafNode{value='abcdef'}, " +
            "secret=MapNode{user=LeafNode{value='827ccb0eea8a706c4c34a16891f84e7b'}}, uri=LeafNode{value='my.sql.com'}}}", rootNode);
    }

    @Test
    public void testRelaxedLexerMultiFormat() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.uri", "test");
        String json = "{\"db_port\":3306}";
        String toml = "db-password = \"abc123\"";

        SentenceLexer lexer = PathLexerBuilder.builder()
            .setDelimiter("[._-]")
            .build();

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(StringConfigSourceBuilder.builder().setConfig(json).setFormat("json").build())
            .addSource(StringConfigSourceBuilder.builder().setConfig(toml).setFormat("toml").build())
            .useCacheDecorator(false)
            // do not normalize the sentence return it as is.
            .setSentenceLexer(lexer)
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test", gestalt.getConfig("db.uri", String.class));
        Assertions.assertEquals("3306", gestalt.getConfig("db.port", String.class));


        Assertions.assertEquals("abc123", gestalt.getConfig("db.password", String.class));

        Assertions.assertEquals("test", gestalt.getConfig("db", DBInfo.class).getUri());
        Assertions.assertEquals(3306, gestalt.getConfig("db", DBInfo.class).getPort());
        Assertions.assertEquals("abc123", gestalt.getConfig("db", DBInfo.class).getPassword());
    }

    @Test
    public void testMergeTags() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "3306");
        configs.put("db.uri", "my.sql.com");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.password", "test2");
        configs2.put("db.port", "456");
        configs2.put("db.uri", "my.postgresql.com");

        Map<String, String> configs3 = new HashMap<>();
        configs3.put("db.password", "test3");
        configs3.put("db.port", "789");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs2).setTags(Tags.profile("one")).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs3).setTags(Tags.profiles("one", "two")).build())
            .setTreatWarningsAsErrors(true)
            .setTagMergingStrategy(new TagMergingStrategyCombine())
            .setDefaultTags(Tags.profile("one"))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("456", gestalt.getConfig("db.port", String.class));
        Assertions.assertEquals("my.postgresql.com", gestalt.getConfig("db.uri", String.class));

        Assertions.assertEquals("test2", gestalt.getConfig("db.password", String.class, Tags.profile("one")));
        Assertions.assertEquals("456", gestalt.getConfig("db.port", String.class, Tags.profile("one")));
        Assertions.assertEquals("my.postgresql.com", gestalt.getConfig("db.uri", String.class, Tags.profile("one")));

        Assertions.assertEquals("test3", gestalt.getConfig("db.password", String.class, Tags.profile("two")));
        Assertions.assertEquals("789", gestalt.getConfig("db.port", String.class, Tags.profile("two")));
        Assertions.assertEquals("my.sql.com", gestalt.getConfig("db.uri", String.class, Tags.profile("two")));

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
    public void temporaryNodeAnnotation() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass@{temp:1}");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
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
    public void encryptedPasswordAnnotation() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("db.name", "test2");
        configs2.put("db.password", "pass@{encrypt}");
        configs2.put("admin[0]", "John2");
        configs2.put("admin[1]", "Steve2");

        List<ConfigSourcePackage> sources = new ArrayList<>();
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs).build());
        sources.add(MapConfigSourceBuilder.builder().setCustomConfig(configs2).build());

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSources(sources)
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
    public void testSecretAnnotationViaPrint() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        configs.put("db.port", "123@{secret}");
        configs.put("db.uri", "my.sql.com");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("tags: Tags{[]} = MapNode{db=MapNode{password=LeafNode{value='*****'}, " +
            "port=LeafNode{value='*****'}, uri=LeafNode{value='my.sql.com'}}}", gestalt.debugPrint());
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
    public void testNullableAnnotation() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "test");
        //configs.put("db.port", "123");
        configs.put("db.uri", "my.sql.com");

        SimpleMeterRegistry registry = new SimpleMeterRegistry();

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        var hostAnnotations =  gestalt.getConfig("db", HostNullableAnnotations.class);
        Assertions.assertEquals("test", hostAnnotations.getPassword());
        Assertions.assertNull(hostAnnotations.getPort());
        Assertions.assertEquals("my.sql.com", hostAnnotations.getUri());

        var hostMethodAnnotations =  gestalt.getConfig("db", HostNullableMethodAnnotations.class);
        Assertions.assertEquals("test", hostMethodAnnotations.getPassword());
        Assertions.assertNull(hostMethodAnnotations.getPort());
        Assertions.assertEquals("my.sql.com", hostMethodAnnotations.getUri());
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
        URL fileNode = GestaltSample.class.getClassLoader().getResource("include.properties");
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
    public void testImportFileFromGit() throws IOException, GestaltException {
        Path configDirectory = Files.createTempDirectory("gitConfigTest");
        configDirectory.toFile().deleteOnExit();

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=git,repoURI=https://github.com/gestalt-config/gestalt.git," +
            "configFilePath=gestalt-git/src/test/resources/include.properties," +
            "localRepoDirectory=" + configDirectory.toAbsolutePath());

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        assertEquals("a", gestalt.getConfig("a", String.class));
        assertEquals("b changed", gestalt.getConfig("b", String.class));
        assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    void includeWithGithubToken() throws GestaltException, IOException {

        Path configDirectory = Files.createTempDirectory("gitConfigTest");
        configDirectory.toFile().deleteOnExit();

        // Must set the git user and password in Env Vars
        String githubToken = System.getenv("GITHUB_TOKEN");
        Assumptions.assumeTrue(githubToken != null, "must have GITHUB_TOKEN defined");

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=git,repoURI=https://github.com/gestalt-config/gestalt.git," +
            "configFilePath=gestalt-git/src/test/resources/include.properties," +
            "localRepoDirectory=" + configDirectory.toAbsolutePath());

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(GitModuleConfigBuilder.builder()
                .setCredentials(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .build())
            .build();

        gestalt.loadConfigs();

        assertEquals("a", gestalt.getConfig("a", String.class));
        assertEquals("b changed", gestalt.getConfig("b", String.class));
        assertEquals("c", gestalt.getConfig("c", String.class));
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

    public static class HostNullableAnnotations {
        private String password;
        private String uri;
        @Nullable
        private Integer port;

        public HostNullableAnnotations() {
        }

        public String getPassword() {
            return password;
        }
        public String getUri() {
            return uri;
        }
        public Integer getPort() {
            return port;
        }
    }

    public static class HostNullableMethodAnnotations {
        private String password;

        private String uri;
        private Integer port;

        public HostNullableMethodAnnotations() {
        }

        public String getPassword() {
            return password;
        }
        public String getUri() {
            return uri;
        }
        @Nullable
        public Integer getPort() {
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

    public static class DBInfo {
        private Integer port;
        private String uri;
        private String password;

        public DBInfo() {
        }

        @Config(defaultVal = "200")
        private Integer connections;

        public Integer getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getConnections() {
            return connections;
        }

        public void setConnections(Integer connections) {
            this.connections = connections;
        }
    }

    public static class DBInfoValid {

        public DBInfoValid() {
        }

        @Min(value = 10, message = "port should not be less than 10")
        @Max(value = 200, message = "port should not be greater than 200")
        private Integer port;

        private String uri;

        @NotNull
        @Size(min = 2, max = 14)
        private String password;

        @NotNull
        @Config(defaultVal = "200")
        private Integer connections;

        public Integer getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Integer getConnections() {
            return connections;
        }

        public void setConnections(Integer connections) {
            this.connections = connections;
        }
    }
}
