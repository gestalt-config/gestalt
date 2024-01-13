package org.github.gestalt.config.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class YamlLoaderTest {

    @Test
    void name() {
        YamlLoader yamlLoader = new YamlLoader();
        Assertions.assertEquals("YamlLoader", yamlLoader.name());
    }

    @Test
    void accepts() {
        YamlLoader yamlLoader = new YamlLoader();
        Assertions.assertTrue(yamlLoader.accepts("yml"));
        Assertions.assertTrue(yamlLoader.accepts("yaml"));
        Assertions.assertFalse(yamlLoader.accepts("properties"));
    }

    @Test
    void loadSource() throws GestaltException {

        StringConfigSource source = new StringConfigSource("name: Steve\n" +
            "age: 42\n" +
            "cars: \n" +
            "  - name: Ford\n" +
            "    models: [Fiesta, Focus, Mustang]\n" +
            "  - name: BMW\n" +
            "    models: [320, X3, X5]\n" +
            "  - name: Fiat\n" +
            "    models: [500, Panda]", "yml");

        YamlLoader yamlLoader = new YamlLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = yamlLoader.loadSource(source);

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals("Ford", result.getKey("cars").get().getIndex(0).get().getKey("name")
                                              .get().getValue().get());
        Assertions.assertEquals("Fiesta", result.getKey("cars").get().getIndex(0).get().getKey("models")
                                                .get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Focus", result.getKey("cars").get().getIndex(0).get().getKey("models")
                                               .get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Mustang", result.getKey("cars").get().getIndex(0).get().getKey("models")
                                                 .get().getIndex(2).get().getValue().get());
        Assertions.assertFalse(result.getKey("cars").get().getIndex(0).get().getKey("models")
                                     .get().getIndex(3).isPresent());
    }

    @Test
    void loadSourceChangeCase() throws GestaltException {

        StringConfigSource source = new StringConfigSource("name: Steve\n" +
            "Age: 42\n" +
            "cars: \n" +
            "  - name: Ford\n" +
            "    Models: [Fiesta, Focus, Mustang]\n" +
            "  - name: BMW\n" +
            "    Models: [320, X3, X5]\n" +
            "  - name: Fiat\n" +
            "    Models: [500, Panda]", "yml");

        YamlLoader yamlLoader = new YamlLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = yamlLoader.loadSource(source);

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals("Ford", result.getKey("cars").get().getIndex(0).get().getKey("name")
                                              .get().getValue().get());
        Assertions.assertEquals("Fiesta", result.getKey("cars").get().getIndex(0).get().getKey("models")
                                                .get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Focus", result.getKey("cars").get().getIndex(0).get().getKey("models")
                                               .get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Mustang", result.getKey("cars").get().getIndex(0).get().getKey("models")
                                                 .get().getIndex(2).get().getValue().get());
        Assertions.assertFalse(result.getKey("cars").get().getIndex(0).get().getKey("models")
                                     .get().getIndex(3).isPresent());
    }

    @Test
    void loadSourceCustomMapper() throws GestaltException {

        StringConfigSource source = new StringConfigSource("name: Steve\n" +
            "age: 42\n" +
            "cars: \n" +
            "  - name: Ford\n" +
            "    models: [Fiesta, Focus, Mustang]\n" +
            "  - name: BMW\n" +
            "    models: [320, X3, X5]\n" +
            "  - name: Fiat\n" +
            "    models: [500, Panda]", "yml");

        YamlLoader yamlLoader = new YamlLoader(new ObjectMapper(new YAMLFactory()));

        GResultOf<List<ConfigNodeContainer>> resultContainer = yamlLoader.loadSource(source);

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals("Ford", result.getKey("cars").get().getIndex(0).get().getKey("name")
                                              .get().getValue().get());
        Assertions.assertEquals("Fiesta", result.getKey("cars").get().getIndex(0).get().getKey("models")
                                                .get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Focus", result.getKey("cars").get().getIndex(0).get().getKey("models")
                                               .get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Mustang", result.getKey("cars").get().getIndex(0).get().getKey("models")
                                                 .get().getIndex(2).get().getValue().get());
        Assertions.assertFalse(result.getKey("cars").get().getIndex(0).get().getKey("models")
                                     .get().getIndex(3).isPresent());
    }

    @Test
    void loadSourceEmptyValue() throws GestaltException {

        StringConfigSource source = new StringConfigSource("name: Steve\n" +
            "age: \n", "yml");

        YamlLoader yamlLoader = new YamlLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = yamlLoader.loadSource(source);

        Assertions.assertTrue(resultContainer.hasErrors());
        Assertions.assertEquals("Unable to find node matching path: age", resultContainer.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: age", resultContainer.getErrors().get(1).description());


        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertFalse(result.getKey("age").isPresent());
    }

    @Test
    void loadSourceEmptyArray() throws GestaltException {

        StringConfigSource source = new StringConfigSource("name: Steve\n" +
            "age: 42\n" +
            "cars: \n", "yml");

        YamlLoader yamlLoader = new YamlLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = yamlLoader.loadSource(source);

        Assertions.assertTrue(resultContainer.hasErrors());
        Assertions.assertEquals("Unable to find node matching path: cars", resultContainer.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: cars", resultContainer.getErrors().get(1).description());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertFalse(result.getKey("cars").isPresent());
    }

    @Test
    void loadSourceBadInput() throws GestaltException {

        StringConfigSource source = new StringConfigSource("&&&& ", "yml");

        YamlLoader yamlLoader = new YamlLoader();

        try {
            yamlLoader.loadSource(source);
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Exception loading source: String format: yml", e.getMessage());
        }
    }

    @Test
    void loadSourceBadSource() {

        Map<String, String> configs = new HashMap<>();
        MapConfigSource source = new MapConfigSource(configs);

        YamlLoader yamlLoader = new YamlLoader();

        try {
            yamlLoader.loadSource(source);
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Config source: mapConfig does not have a stream to load.", e.getMessage());
        }
    }

    @Test
    void loadSourceEmptySource() throws GestaltException {

        StringConfigSource source = new StringConfigSource("", "yml");

        YamlLoader yamlLoader = new YamlLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = yamlLoader.loadSource(source);

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(0, result.size());
    }
}
