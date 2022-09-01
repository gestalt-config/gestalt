package org.github.gestalt.config.aws.s3;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;

class S3ConfigSourceTest {

    @RegisterExtension
    static final S3MockExtension S3_MOCK =
        S3MockExtension.builder().silent().withSecureConnection(false).build();

    private static final String BUCKET_NAME = "testbucket";
    private static final String UPLOAD_FILE_NAME = "src/test/resources/default.properties";

    private final S3Client s3Client = S3_MOCK.createS3ClientV2();

    @Test
    void loadFile() throws GestaltException {

        final File uploadFile = new File(UPLOAD_FILE_NAME);

        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
        s3Client.putObject(
            PutObjectRequest.builder().bucket(BUCKET_NAME).key(uploadFile.getName()).build(),
            RequestBody.fromFile(uploadFile));


        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, uploadFile.getName());

        Assertions.assertTrue(source.hasStream());
        Assertions.assertNotNull(source.loadStream());
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
    void tags() throws GestaltException {
        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, UPLOAD_FILE_NAME, Tags.of("toy", "ball"));
        Assertions.assertEquals(Tags.of("toy", "ball"), source.getTags());
    }
}
