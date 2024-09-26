package org.github.gestalt.config.aws.s3;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3Client;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class S3ConfigSourceMockTest {

    private static final String BUCKET_NAME = "testbucket";

    private static final String UPLOAD_FILE_NAME = "src/test/resources/default.properties";


    private final S3Client s3Client = Mockito.mock();

    @Test
    void idTest() throws GestaltException {
        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, "test");
        Assertions.assertNotNull(source.id());
    }

    @Test
    void loadS3ClientNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> new S3ConfigSource(null, BUCKET_NAME, UPLOAD_FILE_NAME));

        Assertions.assertEquals("S3 client can not be null", exception.getMessage());
    }

    @Test
    void loadS3BucketNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> new S3ConfigSource(s3Client, null, UPLOAD_FILE_NAME));

        Assertions.assertEquals("S3 bucketName can not be null", exception.getMessage());
    }

    @Test
    void loadS3KeyNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> new S3ConfigSource(s3Client, BUCKET_NAME, null));

        Assertions.assertEquals("S3 keyName can not be null", exception.getMessage());
    }

    @Test
    void fileType() throws GestaltException {
        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, UPLOAD_FILE_NAME);

        Assertions.assertEquals("properties", source.format());
    }

    @Test
    void fileTypeEmpty() throws GestaltException {
        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, "src/test/resources/default");

        Assertions.assertEquals("", source.format());
    }

    @Test
    void name() throws GestaltException {
        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, UPLOAD_FILE_NAME);

        Assertions.assertEquals("S3 Config Source key: src/test/resources/default.properties, bucket: testbucket", source.name());
    }

    @Test
    void unsupportedList() throws GestaltException {
        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, UPLOAD_FILE_NAME);

        Assertions.assertFalse(source.hasList());
        Assertions.assertThrows(GestaltException.class, source::loadList);
    }

    @Test
    void equals() throws GestaltException {
        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, UPLOAD_FILE_NAME);

        S3ConfigSource source2 = new S3ConfigSource(s3Client, BUCKET_NAME + "diff", UPLOAD_FILE_NAME);

        Assertions.assertEquals(source, source);
        Assertions.assertNotEquals(source, source2);
        Assertions.assertNotEquals(source, null);
        Assertions.assertNotEquals(source, 1L);
    }

    @Test
    void hash() throws GestaltException {
        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, UPLOAD_FILE_NAME);
        Assertions.assertTrue(source.hashCode() != 0);
    }

    @Test
    @SuppressWarnings("removal")
    void tags() throws GestaltException {
        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, UPLOAD_FILE_NAME, Tags.of("toy", "ball"));
        Assertions.assertEquals(Tags.of("toy", "ball"), source.getTags());
    }
}
