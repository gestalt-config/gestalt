package org.github.gestalt.config.google.storage;

import com.google.cloud.storage.*;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Loads a file from Google Cloud Storage.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class GCSConfigSource implements ConfigSource {

    private final Storage storage;
    private final String objectName;
    private final String bucketName;
    private final UUID id = UUID.randomUUID();

    private final Tags tags;

    /**
     * Constructor for GCSConfigSource.
     *
     * @param bucketName name of the storage bucket
     * @param objectName name of the storage object
     * @throws GestaltException any exceptions thrown
     */
    public GCSConfigSource(String bucketName, String objectName) throws GestaltException {
        this(StorageOptions.getDefaultInstance().getService(), bucketName, objectName, Tags.of());
    }

    /**
     * Constructor for GCSConfigSource.
     *
     * @param storage gc storage client
     * @param bucketName name of the storage bucket
     * @param objectName name of the storage object
     * @param tags tags associated with the source
     * @throws GestaltException any exceptions thrown
     */
    public GCSConfigSource(Storage storage, String bucketName, String objectName, Tags tags) throws GestaltException {

        if (bucketName == null) {
            throw new GestaltException("Google Cloud Storage bucketName can not be null");
        }

        if (objectName == null) {
            throw new GestaltException("Google Cloud Storage objectName can not be null");
        }

        if (storage == null) {
            throw new GestaltException("Google cloud storage service null");
        }

        this.storage = storage;
        this.objectName = objectName;
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
            Blob blob = storage.get(BlobId.of(bucketName, objectName));

            byte[] objectBytes = blob.getContent();
            return new ByteArrayInputStream(objectBytes);

        } catch (StorageException e) {
            throw new GestaltException("Exception loading Google Cloud Storage object: " + objectName + ", bucket: " + bucketName +
                ", with error:" + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasList() {
        return false;
    }

    @Override
    public List<Pair<String, String>> loadList() throws GestaltException {
        throw new GestaltException("Unsupported operation loadList on an GCSConfigSource");
    }

    @Override
    public String format() {
        return format(objectName);
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
        return "Google Cloud Storage Object: " + objectName + ", bucket: " + bucketName;
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
        if (!(o instanceof GCSConfigSource)) {
            return false;
        }
        GCSConfigSource that = (GCSConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
