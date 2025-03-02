package org.github.gestalt.config.integration;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.github.gestalt.config.validation.hibernate.builder.HibernateModuleBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.jopenlibs.vault.Vault;
import io.github.jopenlibs.vault.VaultConfig;
import io.github.jopenlibs.vault.VaultException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.annotations.ConfigPrefix;
import org.github.gestalt.config.aws.config.AWSBuilder;
import org.github.gestalt.config.aws.s3.S3ConfigSourceBuilder;
import org.github.gestalt.config.azure.blob.BlobConfigSourceBuilder;
import org.github.gestalt.config.azure.config.AzureModuleBuilder;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.git.GitConfigSourceBuilder;
import org.github.gestalt.config.google.storage.GCSConfigSourceBuilder;
import org.github.gestalt.config.guice.GestaltModule;
import org.github.gestalt.config.guice.InjectConfig;
import org.github.gestalt.config.micrometer.builder.MicrometerModuleConfigBuilder;
import org.github.gestalt.config.node.factory.MapNodeImportFactory;
import org.github.gestalt.config.processor.config.transform.LoadtimeStringSubstitutionConfigNodeProcessor;
import org.github.gestalt.config.processor.config.transform.RandomTransformer;
import org.github.gestalt.config.processor.config.transform.SystemPropertiesTransformer;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.reload.CoreReloadListener;
import org.github.gestalt.config.source.*;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.vault.config.VaultBuilder;
import org.github.gestalt.config.vault.config.VaultModuleConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GestaltConfigTest {

    public void integrationTest() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        // Load the configurations, this will thow exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    public void integrationTestNoCache() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .useCacheDecorator(false)
            .build();

        // Load the configurations, this will thow exceptions if there are any errors.
        gestalt.loadConfigs();

        validateResults(gestalt);
    }

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
                .setResource("dev.properties")
                .setTags(Tags.of("toy", "ball"))
                .build())
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

    public void integrationTestEnvVars() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");

        String urlFile = "https://raw.githubusercontent.com/gestalt-config/gestalt/main/gestalt-examples/gestalt-sample/src/test/resources/default.json";

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
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
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

    public void integrationTestJson() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.json").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.json").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    public void integrationTestYaml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.yml").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.yml").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    public void integrationTestJsonAndYaml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.json").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.yml").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    public void integrationTestHocon() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.conf").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.yml").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

    public void integrationTestToml() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.conf").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.toml").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

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

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(source)
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        validateResults(gestalt);
    }

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
            .addSource(S3ConfigSourceBuilder.builder()
                .setS3(s3Client)
                .setBucketName("gestalt-test")
                .setKeyName("dev.properties")
                .build())
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

    public void integrationTestPostProcessorVault() throws GestaltException, VaultException {
        String VAULT_TOKEN = "my-root-token-2";


        final VaultConfig config = new VaultConfig()
            .address("http://127.0.0.1:8080")
            .token(VAULT_TOKEN)
            .build();

        Vault vault = Vault.create(config);

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

        gestalt.getConfig("db", DataBase.class);

        DataBase db = gestalt.getConfig("db", DataBase.class);

        // not really a great test for ensuring we are hitting a cache
        //Assertions.assertTrue(timeTaken > cacheTimeTaken);

        Assertions.assertEquals(600, db.connectionTimeout);
        Assertions.assertEquals(600, gestalt.getConfig("DB.connectionTimeout", Integer.class));
        Assertions.assertEquals(123, db.idleTimeout);
        Assertions.assertEquals(60000.0F, db.maxLifetime);
        Assertions.assertTrue(db.isEnabled);
        Assertions.assertTrue(gestalt.getConfig("db.isEnabled", true, Boolean.class));

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.getFirst().getUser());
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
        Assertions.assertEquals("credmond", dbPrefix.hosts.getFirst().getUser());
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
        Assertions.assertEquals("credmond", hosts.getFirst().getUser());
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
        Assertions.assertEquals("credmond", ihosts.getFirst().getUser());
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
        Assertions.assertEquals("credmond", ihostsDefault.getFirst().getUser());
        Assertions.assertEquals("1234", ihostsDefault.getFirst().getPassword());
        //Assertions.assertEquals(10, ihostsDefault.get(0).getPort());

        List<IHostAnnotations> iHostAnnotations = gestalt.getConfig("db.hosts", Collections.emptyList(), new TypeCapture<>() {
        });
        Assertions.assertEquals(3, iHostAnnotations.size());
        Assertions.assertEquals("credmond", iHostAnnotations.getFirst().getUser());
        Assertions.assertEquals("1234", iHostAnnotations.getFirst().getPassword());
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
        Assertions.assertEquals("credmond", hostsAnnotations.getFirst().getUser());
        Assertions.assertEquals("1234", hostsAnnotations.getFirst().getPassword());
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
        Assertions.assertEquals("credmond", hostsMethodAnnotations.getFirst().getUser());
        Assertions.assertEquals("1234", hostsMethodAnnotations.getFirst().getSecret());
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
        Assertions.assertTrue(gestalt.getConfig("db.isEnabled", true, Boolean.class));

        Assertions.assertEquals(3, db.hosts.size());
        Assertions.assertEquals("credmond", db.hosts.getFirst().getUser());
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
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("integration.properties").build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addConfigNodeProcessor(
                new LoadtimeStringSubstitutionConfigNodeProcessor(List.of(new SystemPropertiesTransformer(), new RandomTransformer())))
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

    public void integrationTestPostProcessorNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("defaultPPNode.properties").build())
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
    public void testObservations() throws GestaltException {
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

    @Test
    public void testValidationError() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("db.password", "12345678901234567890");
        configs.put("db.port", "0");
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

        Assertions.assertEquals("12345678901234567890", gestalt.getConfig("db.password", String.class));
        Assertions.assertEquals("12345678901234567890", gestalt.getConfig("db", DBInfo.class).password);
        Assertions.assertEquals(0, gestalt.getConfig("db", DBInfo.class).port);

        var ex = Assertions.assertThrows(GestaltException.class, () -> gestalt.getConfig("db", DBInfoValid.class));

        org.assertj.core.api.Assertions.assertThat(ex.getMessage())
            .startsWith("Validation failed for config path: db, and " +
                "class: org.github.gestalt.config.integration.GestaltConfigTest$DBInfoValid")
            .contains("- level: ERROR, message: Hibernate Validator, on path: db, error: size must be between 2 and 14")
            .contains("- level: ERROR, message: Hibernate Validator, on path: db, error: port should not be less than 10");
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
        URL fileNode = GestaltConfigTest.class.getClassLoader().getResource("include.properties");
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

    public enum Role {
        LEVEL0, LEVEL1
    }

    public interface IHostDefault {
        String getUser();

        String getUrl();

        String getPassword();

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


    public static class DBInfo {
        private Integer port;
        private String uri;
        private String password;
        @Config(defaultVal = "200")
        private Integer connections;

        public DBInfo() {
        }

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

        public DBInfoValid() {
        }

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
