package org.github.gestalt.config.loader;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConfigLoaderUtilsTest {

    @Test
    public void testOk() throws GestaltException {

        ConfigSource source = MapConfigSourceBuilder.builder().addCustomConfig("path", "value").build().getConfigSource();
        ConfigLoaderService configLoaderService = new ConfigLoaderRegistry();
        configLoaderService.addLoader(new MapConfigLoader());

        GResultOf<List<ConfigNode>> results = ConfigLoaderUtils.convertSourceToNodes(source, configLoaderService);
        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(1, results.results().size());
        Assertions.assertEquals("value", results.results().get(0).getKey("path").get().getValue().get());
    }

}
