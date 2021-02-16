package org.github.gestalt.config.aws.s3;


import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.utils.Pair;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Loads a file from S3.
 *
 * @author Colin Redmond
 */
public class S3ConfigSource implements ConfigSource {

    private final S3Client s3;
    private final String keyName;
    private final String bucketName;
    private final UUID id = UUID.randomUUID();

    public S3ConfigSource(S3Client s3, String bucketName, String keyName) throws GestaltException {
        if (s3 == null) {
            throw new GestaltException("S3 client can not be null");
        }

        if (bucketName == null) {
            throw new GestaltException("S3 bucketName can not be null");
        }

        if (keyName == null) {
            throw new GestaltException("S3 keyName can not be null");
        }

        this.s3 = s3;
        this.keyName = keyName;
        this.bucketName = bucketName;
    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        try {
            GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(keyName)
                .bucket(bucketName)
                .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            return objectBytes.asInputStream();

        } catch (S3Exception e) {
            throw new GestaltException("Exception loading S3 key: " + keyName + ", bucket: " + bucketName + ", with error:" +
                e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {
        throw new GestaltException("Unsupported operation loadList on an S3ConfigSource");
    }

    @Override
    public String format() {
        return format(keyName);
    }

    protected String format(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            return fileName.substring(index + 1);
        } else {
            return "";
        }
    }

    @Override
    public String name() {
        return "S3 Config Source key: " + keyName + ", bucket: " + bucketName;
    }

    @Override
    public UUID id() {  //NOPMD
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof S3ConfigSource)) {
            return false;
        }
        S3ConfigSource that = (S3ConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
