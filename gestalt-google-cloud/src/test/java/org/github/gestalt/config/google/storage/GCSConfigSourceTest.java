package org.github.gestalt.config.google.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class GCSConfigSourceTest {

    private static final String BUCKET_NAME = "testbucket";
    private static final String UPLOAD_FILE_NAME = "src/test/resources/default.properties";

    private Storage storage;
    private Blob blob;

    @BeforeEach
    void setup() {
        storage = Mockito.mock();
        blob = Mockito.mock();
    }

    @Test
    void loadFile() throws GestaltException, IOException {

        final File uploadFile = new File(UPLOAD_FILE_NAME);
        byte[] bytes = Files.readAllBytes(uploadFile.toPath());

        Mockito.when(storage.get(BlobId.of(BUCKET_NAME, uploadFile.getName()))).thenReturn(blob);
        Mockito.when(blob.getContent()).thenReturn(bytes);

        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, uploadFile.getName(), Tags.of());

        Assertions.assertTrue(source.hasStream());
        Assertions.assertNotNull(source.loadStream());
    }

    @Test
    void loadFileException() throws GestaltException, IOException {

        final File uploadFile = new File(UPLOAD_FILE_NAME);

        Mockito.when(storage.get(BlobId.of(BUCKET_NAME, uploadFile.getName()))).thenReturn(blob);
        Mockito.when(blob.getContent()).thenThrow(new StorageException(504, "bad data"));

        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, uploadFile.getName(), Tags.of());

        Assertions.assertTrue(source.hasStream());
        GestaltException exception = Assertions.assertThrows(GestaltException.class, source::loadStream);
        Assertions.assertEquals("Exception loading Google Cloud Storage " +
            "object: default.properties, bucket: testbucket, with error:bad data", exception.getMessage());
    }

    @Test
    void idTest() throws GestaltException {
        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, "test", Tags.of());
        Assertions.assertNotNull(source.id());
    }

    @Test
    void loadStorageClientNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> new GCSConfigSource(null, BUCKET_NAME, UPLOAD_FILE_NAME, Tags.of()));

        Assertions.assertEquals("Google cloud storage service null", exception.getMessage());
    }

    @Test
    void loadStorageBucketNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> new GCSConfigSource(storage, null, UPLOAD_FILE_NAME, Tags.of()));

        Assertions.assertEquals("Google Cloud Storage bucketName can not be null", exception.getMessage());
    }

    @Test
    void loadStorageKeyNull() {
        GestaltException exception = Assertions.assertThrows(GestaltException.class,
            () -> new GCSConfigSource(storage, BUCKET_NAME, null, Tags.of()));

        Assertions.assertEquals("Google Cloud Storage objectName can not be null", exception.getMessage());
    }

    @Test
    void fileType() throws GestaltException {
        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, UPLOAD_FILE_NAME, Tags.of());

        Assertions.assertEquals("properties", source.format());
    }

    @Test
    void formatEmpty() throws GestaltException {
        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, "test", Tags.of());
        Assertions.assertEquals("", source.format());
    }

    @Test
    void name() throws GestaltException {
        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, UPLOAD_FILE_NAME, Tags.of());

        Assertions.assertEquals("Google Cloud Storage Object: src/test/resources/default.properties, bucket: testbucket", source.name());
    }

    @Test
    void unsupportedList() throws GestaltException {
        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, UPLOAD_FILE_NAME, Tags.of());

        Assertions.assertFalse(source.hasList());
        Assertions.assertThrows(GestaltException.class, source::loadList);
    }

    @Test
    void equals() throws GestaltException {
        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, UPLOAD_FILE_NAME, Tags.of());

        GCSConfigSource source2 = new GCSConfigSource(storage, BUCKET_NAME + "diff", UPLOAD_FILE_NAME, Tags.of());

        Assertions.assertEquals(source, source);
        Assertions.assertNotEquals(source, source2);
        Assertions.assertNotEquals(source, null);
        Assertions.assertNotEquals(source, 1L);
    }

    @Test
    void hash() throws GestaltException {
        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, UPLOAD_FILE_NAME, Tags.of());
        Assertions.assertTrue(source.hashCode() != 0);
    }

    @Test
    void tags() throws GestaltException {
        GCSConfigSource source = new GCSConfigSource(storage, BUCKET_NAME, UPLOAD_FILE_NAME, Tags.of("toy", "ball"));
        Assertions.assertEquals(Tags.of("toy", "ball"), source.getTags());
    }
}
