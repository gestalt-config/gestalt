package org.github.gestalt.config.aws.s3;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
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
import java.util.Collection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static software.amazon.awssdk.http.SdkHttpConfigurationOption.TRUST_ALL_CERTIFICATES;

@Testcontainers
class S3ConfigSourceDockerTest {
    private static final String BUCKET_NAME = "testbucket";
    private static final String BUCKET_NAME_2 = "testbucket2";

    private static final String S3MOCK_VERSION = System.getProperty("s3mock.version", "latest");
    private static final Collection<String> INITIAL_BUCKET_NAMES = asList(BUCKET_NAME, BUCKET_NAME_2);
    private static final String TEST_ENC_KEYREF =
        "arn:aws:kms:us-east-1:1234567890:key/valid-test-key-ref";

    private static final String UPLOAD_FILE_NAME = "src/test/resources/default.properties";

    private S3Client s3Client;

    @Container
    private static final S3MockContainer s3Mock =
        new S3MockContainer(S3MOCK_VERSION)
            .withValidKmsKeys(TEST_ENC_KEYREF)
            .withInitialBuckets(String.join(",", INITIAL_BUCKET_NAMES));

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

    @Test
    void loadFile() throws GestaltException, IOException {

        final File uploadFile = new File(UPLOAD_FILE_NAME);

        s3Client.putObject(
            PutObjectRequest.builder().bucket(BUCKET_NAME).key(uploadFile.getName()).build(),
            RequestBody.fromFile(uploadFile));


        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME, uploadFile.getName());

        Assertions.assertTrue(source.hasStream());

        var allBytes = source.loadStream().readAllBytes();

        InputStream fileStream = new FileInputStream(uploadFile);
        Assertions.assertEquals(new String(allBytes, UTF_8), new String(fileStream.readAllBytes(), UTF_8));
    }

    @Test
    void loadFileDoesNotExist() throws GestaltException {

        final File uploadFile = new File(UPLOAD_FILE_NAME);

        s3Client.putObject(
            PutObjectRequest.builder().bucket(BUCKET_NAME_2).key(uploadFile.getName()).build(),
            RequestBody.fromFile(uploadFile));


        S3ConfigSource source = new S3ConfigSource(s3Client, BUCKET_NAME_2, uploadFile.getName() + ".noMatch");

        Assertions.assertTrue(source.hasStream());
        GestaltException ex = Assertions.assertThrows(GestaltException.class, source::loadStream);

        Assertions.assertEquals("Exception loading S3 key: default.properties.noMatch, bucket: testbucket2, " +
            "with error: The specified key does not exist.", ex.getMessage());
    }
}
