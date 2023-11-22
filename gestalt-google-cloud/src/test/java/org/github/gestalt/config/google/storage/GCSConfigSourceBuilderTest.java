package org.github.gestalt.config.google.storage;

import com.google.cloud.storage.Storage;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;


class GCSConfigSourceBuilderTest {

    private final Storage storage = Mockito.mock();

    @Test
    void buildGCSConfigSource() throws GestaltException {
        GCSConfigSourceBuilder builder = GCSConfigSourceBuilder.builder();
        builder.setStorage(storage);
        builder.setBucketName("testBucket");
        builder.setObjectName("testObject");

        assertEquals(storage, builder.getStorage());
        assertEquals("testBucket", builder.getBucketName());
        assertEquals("testObject", builder.getObjectName());

        ConfigSourcePackage<GCSConfigSource> configSourcePackage = builder.build();
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        GCSConfigSource gcsConfigSource = configSourcePackage.getConfigSource();
        assertTrue(gcsConfigSource.hasStream());
    }

    @Test
    void buildGCSConfigSourceNullStorage() throws GestaltException {
        GCSConfigSourceBuilder builder = GCSConfigSourceBuilder.builder();
        //builder.setStorage(storage);
        builder.setBucketName("testBucket");
        builder.setObjectName("testObject");

        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("Google cloud storage service null", e.getMessage());
    }

    @Test
    void buildGCSConfigSourceNullBucket() throws GestaltException {
        GCSConfigSourceBuilder builder = GCSConfigSourceBuilder.builder();
        builder.setStorage(storage);
        //builder.setBucketName("testBucket");
        builder.setObjectName("testObject");

        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("Google Cloud Storage bucketName can not be null", e.getMessage());
    }

    @Test
    void buildGCSConfigSourceNullObject() throws GestaltException {
        GCSConfigSourceBuilder builder = GCSConfigSourceBuilder.builder();
        builder.setStorage(storage);
        builder.setBucketName("testBucket");
        //builder.setObjectName("testObject");

        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("Google Cloud Storage objectName can not be null", e.getMessage());
    }
}
