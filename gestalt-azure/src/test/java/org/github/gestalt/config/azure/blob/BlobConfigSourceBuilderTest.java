package org.github.gestalt.config.azure.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class BlobConfigSourceBuilderTest {

    private final BlobClient blobClient = Mockito.mock();

    @Test
    void buildBlobConfigSource() throws GestaltException {
        BlobConfigSourceBuilder builder = BlobConfigSourceBuilder.builder()
            .setBlobClient(blobClient)
            .setBlobName("testBlob.properties")
            .setContainerName("testContainer");

        when(blobClient.getBlobName()).thenReturn("testBlob.properties");


        ConfigSourcePackage configSourcePackage = builder.build();
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        BlobConfigSource blobConfigSource = (BlobConfigSource) configSourcePackage.getConfigSource();
        assertTrue(blobConfigSource.hasStream());
        assertEquals("properties", blobConfigSource.format());
    }

    @Test
    void buildBlobConfigSourceBuildClient() throws GestaltException {
        BlobConfigSourceBuilder builder = BlobConfigSourceBuilder.builder()
            //.setBlobClient(blobClient)
            .setBlobName("testBlob.properties")
            .setEndpoint("vault.com")
            .setContainerName("testContainer");

        try (MockedConstruction<BlobClientBuilder> blobBuilder = Mockito.mockConstruction(BlobClientBuilder.class,
            (mock, context) -> {
                when(mock.endpoint(any())).thenReturn(mock);
                when(mock.blobName(any())).thenReturn(mock);
                when(mock.containerName(any())).thenReturn(mock);
                when(mock.buildClient()).thenReturn(blobClient);
            })) {

            var config = builder.build();

            assertNotNull(config.getConfigSource());
        }
    }

    @Test
    void buildBlobConfigSourceBuildClientWithCredentials() throws GestaltException {
        final StorageSharedKeyCredential tokenCredential = Mockito.mock();

        BlobConfigSourceBuilder builder = BlobConfigSourceBuilder.builder()
            //.setBlobClient(blobClient)
            .setBlobName("testBlob.properties")
            .setEndpoint("vault.com")
            .setContainerName("testContainer")
            .setCredential(tokenCredential);

        try (MockedConstruction<BlobClientBuilder> blobBuilder = Mockito.mockConstruction(BlobClientBuilder.class,
            (mock, context) -> {
                when(mock.endpoint(any())).thenReturn(mock);
                when(mock.blobName(any())).thenReturn(mock);
                when(mock.containerName(any())).thenReturn(mock);
                when(mock.buildClient()).thenReturn(blobClient);
            })) {

            var config = builder.build();

            assertNotNull(config.getConfigSource());
        }
    }

    @Test
    void buildBlobConfigSourceNullBlobName() {
        BlobConfigSourceBuilder builder = BlobConfigSourceBuilder.builder()
            //.setBlobClient(blobClient)
            //.setBlobName("testBlob.properties")
            .setEndpoint("vault.com")
            .setContainerName("testContainer");

        NullPointerException e = assertThrows(NullPointerException.class, builder::build);

        assertEquals("Must provided either a BlobClient or a valid blobName", e.getMessage());
    }


    @Test
    void buildBlobConfigSourceNullContainer() {
        BlobConfigSourceBuilder builder = BlobConfigSourceBuilder.builder()
            //.setBlobClient(blobClient)
            .setBlobName("testBlob.properties")
            .setEndpoint("vault.com");

        NullPointerException e = assertThrows(NullPointerException.class, builder::build);

        assertEquals("Must provided either a BlobClient or a valid containerName", e.getMessage());
    }

    @Test
    void buildBlobConfigSourceNullEndpoint() {
        BlobConfigSourceBuilder builder = BlobConfigSourceBuilder.builder()
            //.setBlobClient(blobClient)
            .setBlobName("testBlob.properties")
            //.setEndpoint("vault.com")
            .setContainerName("testContainer");

        NullPointerException e = assertThrows(NullPointerException.class, builder::build);

        assertEquals("Must provided either a BlobClient or a valid endpoint", e.getMessage());
    }
}
