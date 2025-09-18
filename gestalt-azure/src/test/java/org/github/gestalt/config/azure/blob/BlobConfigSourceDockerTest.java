package org.github.gestalt.config.azure.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.*;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BlobConfigSourceDockerTest {

    private static final String UPLOAD_FILE_NAME = "src/test/resources/default.properties";
    private static final String connectionString = "AccountName=devstoreaccount1;" +
        "AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;" +
        "DefaultEndpointsProtocol=http;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;" +
        "QueueEndpoint=http://127.0.0.1:10001/devstoreaccount1;TableEndpoint=http://127.0.0.1:10002/devstoreaccount1;";
    @Container
    private static final GenericContainer<?> azureStorage =
        new GenericContainer<>(DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite:3.35.0"))
            .withExposedPorts(10000, 10001, 10002);
    private static final String testContainer = "testcontainer";
    private static final String testBlobName = "testBlobName";
    private BlobClient blobClient;
    private BlobContainerClient container;

    @BeforeAll
    void setUp() throws FileNotFoundException {
        azureStorage.start();

        StorageSharedKeyCredential credential = StorageSharedKeyCredential.fromConnectionString(connectionString);

        container = new BlobServiceClientBuilder()
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
    void loadFile() throws GestaltException, IOException {
        var configSourcePackage = BlobConfigSourceBuilder.builder()
            .setBlobClient(blobClient)
            .build();

        var source = configSourcePackage.getConfigSource();

        Assertions.assertTrue(source.hasStream());
        Assertions.assertFalse(source.hasList());

        var allBytes = source.loadStream().readAllBytes();

        final File uploadFile = new File(UPLOAD_FILE_NAME);
        InputStream fileStream = new FileInputStream(uploadFile);
        Assertions.assertEquals(new String(allBytes, Charset.defaultCharset()),
            new String(fileStream.readAllBytes(), Charset.defaultCharset()));
    }

    @Test
    void loadFileDoesNotExist() throws GestaltException {

        var blobClientNotExist = container.getBlobClient("notAValidFile.properties");

        var configSourcePackage = BlobConfigSourceBuilder.builder()
            .setBlobClient(blobClientNotExist)
            .build();

        var source = configSourcePackage.getConfigSource();

        Assertions.assertTrue(source.hasStream());
        Assertions.assertFalse(source.hasList());

        GestaltException ex = Assertions.assertThrows(GestaltException.class, source::loadStream);

        assertThat(ex).isInstanceOf(GestaltException.class)
            .hasMessageContaining("Exception loading from blobClient, with container: testcontainer " +
                "file: notAValidFile.properties, with message: Status code 404");
    }
}
