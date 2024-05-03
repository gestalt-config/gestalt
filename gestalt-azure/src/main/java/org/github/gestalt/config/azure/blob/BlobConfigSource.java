package org.github.gestalt.config.azure.blob;


import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobStorageException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Loads a file from Azure Blob Storage.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class BlobConfigSource implements ConfigSource {

    private final BlobClient blobClient;
    private final UUID id = UUID.randomUUID();

    /**
     * Constructor for S3ConfigSource.
     *
     * @param blobClient Blob Client
     */
    public BlobConfigSource(BlobClient blobClient) {
        this.blobClient = blobClient;
    }

    @Override
    public boolean hasStream() {
        return true;
    }

    @Override
    public InputStream loadStream() throws GestaltException {
        try {

            return blobClient.openInputStream();
        } catch (BlobStorageException e) {
            throw new GestaltException("Exception loading from blobClient, with container: " + blobClient.getContainerName() +
                " file: " + blobClient.getBlobName() + ", with message: " + e.getMessage(), e);
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
        return format(blobClient.getBlobName());
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
        return "BlobConfigSource, with container: " + blobClient.getContainerName() +
            " file: " + blobClient.getBlobName();
    }

    @Override
    public UUID id() {  //NOPMD
        return id;
    }

    @Override
    public Tags getTags() {
        return Tags.of();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlobConfigSource)) {
            return false;
        }
        BlobConfigSource that = (BlobConfigSource) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
