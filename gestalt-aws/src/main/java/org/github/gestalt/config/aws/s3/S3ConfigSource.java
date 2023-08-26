package org.github.gestalt.config.aws.s3;


import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.tag.Tags;
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
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class S3ConfigSource implements ConfigSource {

    private final S3Client s3;
    private final String keyName;
    private final String bucketName;
    private final UUID id = UUID.randomUUID();

    private final Tags tags;

    /**
     * Constructor for S3ConfigSource.
     *
     * @param s3 S3 client
     * @param bucketName name of the S3 bucket
     * @param keyName name of the S3 key
     * @throws GestaltException any exceptions thrown
     */
    public S3ConfigSource(S3Client s3, String bucketName, String keyName) throws GestaltException {
        this(s3, bucketName, keyName, Tags.of());
    }

    /**
     * Constructor for S3ConfigSource.
     *
     * @param s3 S3 client
     * @param bucketName name of the S3 bucket
     * @param keyName name of the S3 key
     * @param tags tags associated with the source
     * @throws GestaltException any exceptions thrown
     */
    public S3ConfigSource(S3Client s3, String bucketName, String keyName, Tags tags) throws GestaltException {
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
        this.tags = tags;
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
            throw new GestaltException("Exception loading S3 key: " + keyName + ", bucket: " + bucketName + ", with error: " +
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

    /**
     * Finds the extension of a file to get the file format.
     *
     * @param fileName the name of the file
     * @return the extension of the file
     */
    private String format(String fileName) {
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
    public Tags getTags() {
        return tags;
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
