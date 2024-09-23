package org.github.gestalt.config.azure.blob;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.specialized.BlobInputStream;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.mockito.Mockito.when;

class BlobConfigSourceMockTest {

    private final BlobClient blobClient = Mockito.mock();

    @Test
    void idTest() {
        BlobConfigSource source = new BlobConfigSource(blobClient);
        Assertions.assertNotNull(source.id());
    }


    @Test
    void fileType() {
        BlobConfigSource source = new BlobConfigSource(blobClient);

        when(blobClient.getBlobName()).thenReturn("myfile.properties");

        Assertions.assertEquals("properties", source.format());
    }

    @Test
    void fileTypeEmpty() {
        BlobConfigSource source = new BlobConfigSource(blobClient);

        when(blobClient.getBlobName()).thenReturn("");

        Assertions.assertEquals("", source.format());
    }

    @Test
    void name() {
        BlobConfigSource source = new BlobConfigSource(blobClient);

        when(blobClient.getBlobName()).thenReturn("myfile.properties");
        when(blobClient.getContainerName()).thenReturn("container");

        Assertions.assertEquals("BlobConfigSource, with container: container file: myfile.properties", source.name());
    }

    @Test
    void unsupportedList() {
        BlobConfigSource source = new BlobConfigSource(blobClient);

        Assertions.assertFalse(source.hasList());
        Assertions.assertThrows(GestaltException.class, source::loadList);
    }

    @Test
    void loadStream() throws GestaltException, IOException {
        BlobConfigSource source = new BlobConfigSource(blobClient);

        BlobInputStream blobInputStream = Mockito.mock();

        when(blobInputStream.readAllBytes()).thenReturn("hello world".getBytes(Charset.defaultCharset()));

        when(blobClient.openInputStream()).thenReturn(blobInputStream);

        Assertions.assertTrue(source.hasStream());
        String data = new String(source.loadStream().readAllBytes(), Charset.defaultCharset());
        Assertions.assertEquals("hello world", data);
    }

    @Test
    void equals() {
        BlobConfigSource source = new BlobConfigSource(blobClient);

        BlobClient blobClient2 = Mockito.mock();

        BlobConfigSource source2 = new BlobConfigSource(blobClient2);

        Assertions.assertEquals(source, source);
        Assertions.assertNotEquals(source, source2);
        Assertions.assertNotEquals(source, null);
        Assertions.assertNotEquals(source, 1L);
    }

    @Test
    void hash() {
        BlobConfigSource source = new BlobConfigSource(blobClient);
        Assertions.assertTrue(source.hashCode() != 0);
    }

    @Test
    void tags() {
        BlobConfigSource source = new BlobConfigSource(blobClient);

        Assertions.assertEquals(Tags.of(), source.getTags());
    }
}
