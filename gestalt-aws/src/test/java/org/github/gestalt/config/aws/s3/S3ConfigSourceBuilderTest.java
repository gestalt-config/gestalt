package org.github.gestalt.config.aws.s3;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.*;


class S3ConfigSourceBuilderTest {

    private final S3Client s3Client = Mockito.mock();

    @Test
    void buildS3ConfigSource() throws GestaltException {
        S3ConfigSourceBuilder builder = S3ConfigSourceBuilder.builder();
        builder.setS3(s3Client);
        builder.setBucketName("testBucket");
        builder.setKeyName("testKey");

        assertEquals(s3Client, builder.getS3());
        assertEquals("testBucket", builder.getBucketName());
        assertEquals("testKey", builder.getKeyName());

        ConfigSourcePackage configSourcePackage = builder.build();
        assertNotNull(configSourcePackage);
        assertNotNull(configSourcePackage.getConfigSource());

        S3ConfigSource s3ConfigSource = (S3ConfigSource) configSourcePackage.getConfigSource();
        assertTrue(s3ConfigSource.hasStream());
    }

    @Test
    void buildS3ConfigSourceNullClient() {
        S3ConfigSourceBuilder builder = S3ConfigSourceBuilder.builder();
        //builder.setS3(s3Client);
        builder.setBucketName("testBucket");
        builder.setKeyName("testKey");

        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("S3 client can not be null", e.getMessage());
    }

    @Test
    void buildS3ConfigSourceNullBucket() {
        S3ConfigSourceBuilder builder = S3ConfigSourceBuilder.builder();
        builder.setS3(s3Client);
        //builder.setBucketName("testBucket");
        builder.setKeyName("testKey");

        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("S3 bucketName can not be null", e.getMessage());
    }


    @Test
    void buildS3ConfigSourceNullBKeyName() {
        S3ConfigSourceBuilder builder = S3ConfigSourceBuilder.builder();
        builder.setS3(s3Client);
        builder.setBucketName("testBucket");
        //builder.setKeyName("testKey");

        GestaltException e = assertThrows(GestaltException.class, builder::build);

        assertEquals("S3 keyName can not be null", e.getMessage());
    }
}
