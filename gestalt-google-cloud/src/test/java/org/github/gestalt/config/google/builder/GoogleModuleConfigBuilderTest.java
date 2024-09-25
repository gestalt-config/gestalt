package org.github.gestalt.config.google.builder;

import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


class GoogleModuleConfigBuilderTest {

    @Test
    public void testBuilder() {
        var googleModuleConfigBuilder = GoogleModuleConfigBuilder.builder().setProjectId("test");

        Assertions.assertEquals("test", googleModuleConfigBuilder.getProjectId());

        var config =  googleModuleConfigBuilder.build();

        Assertions.assertEquals("test", config.getProjectId());
        Assertions.assertFalse(config.hasStorage());
        Assertions.assertNull(config.getStorage());
    }

    @Test
    public void testBuilderStorageClient() {
        Storage storage = Mockito.mock();

        var googleModuleConfigBuilder = GoogleModuleConfigBuilder.builder()
            .setProjectId("test")
            .setStorage(storage);

        Assertions.assertEquals("test", googleModuleConfigBuilder.getProjectId());
        Assertions.assertEquals(storage, googleModuleConfigBuilder.getStorage());

        var config =  googleModuleConfigBuilder.build();

        Assertions.assertEquals("test", config.getProjectId());
        Assertions.assertTrue(config.hasStorage());
        Assertions.assertEquals(storage, config.getStorage());
    }

}
