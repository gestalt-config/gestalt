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

        ConfigSourcePackage configSourcePackage = builder.build();
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        GCSConfigSource gcsConfigSource = (GCSConfigSource) configSourcePackage.getConfigSource();
        assertTrue(gcsConfigSource.hasStream());
    }

    @Test
    void buildGCSConfigSourceNullStorageUseDefault() throws GestaltException {
        GCSConfigSourceBuilder builder = GCSConfigSourceBuilder.builder();
        //builder.setStorage(storage);
        builder.setBucketName("testBucket");
        builder.setObjectName("testObject");

        var config = builder.build();

        assertNotNull(config);
    }

    @Test
    void buildGCSConfigSourceNullBucket() {
        GCSConfigSourceBuilder builder = GCSConfigSourceBuilder.builder();
        builder.setStorage(storage);
        //builder.setBucketName("testBucket");
        builder.setObjectName("testObject");

        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("Google Cloud Storage bucketName can not be null", e.getMessage());
    }

    @Test
    void buildGCSConfigSourceNullObject() {
        GCSConfigSourceBuilder builder = GCSConfigSourceBuilder.builder();
        builder.setStorage(storage);
        builder.setBucketName("testBucket");
        //builder.setObjectName("testObject");

        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("Google Cloud Storage objectName can not be null", e.getMessage());
    }
}
