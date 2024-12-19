package org.github.gestalt.config.azure.node.factory;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.azure.config.AzureModuleBuilder;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BlobConfigNodeDockerTest {

    private static final String UPLOAD_FILE_NAME = "src/test/resources/include.properties";
    private static final String connectionString = "AccountName=devstoreaccount1;" +
        "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;" +
        "DefaultEndpointsProtocol=http;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;" +
        "QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;TableEndpoint=http://127.0.0.1:10002/devstoreaccount1;";
    @Container
    private static final GenericContainer<?> azureStorage =
        new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:3.33.0"))
            .withExposedPorts(10000, 10001, 10002);
    private static final String testContainer = "testcontainer";
    private static final String testBlobName = "testBlobName.properties";
    private BlobClient blobClient;

    @BeforeEach
    void setUp() throws FileNotFoundException {
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

        final File uploadFile = new File(UPLOAD_FILE_NAME);

        Assertions.assertTrue(uploadFile.exists());
        InputStream fileStream = new FileInputStream(uploadFile);
        blobClient.upload(fileStream);
    }

    @Test
    void integrationTest() throws GestaltException {

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

    @Test
    void integrationTestNoModule() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$include:1", "source=blob,endpoint=http://127.0.0.1:" + azureStorage.getFirstMappedPort() + "/devstoreaccount1," +
            "blob=" + testBlobName + ",container=" + testContainer);

        StorageSharedKeyCredential credential = StorageSharedKeyCredential.fromConnectionString(connectionString);

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addModuleConfig(AzureModuleBuilder.builder().setStorageSharedKeyCredential(credential).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }
}
