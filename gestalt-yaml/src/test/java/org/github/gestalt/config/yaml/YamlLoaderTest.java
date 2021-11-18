package org.github.gestalt.config.yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        ValidateOf<ConfigNode> result = yamlLoader.loadSource(source);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("Steve", result.results().getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.results().getKey("age").get().getValue().get());
        Assertions.assertEquals("Ford", result.results().getKey("cars").get().getIndex(0).get().getKey("name")
            .get().getValue().get());
        Assertions.assertEquals("Fiesta", result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Focus", result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Mustang", result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(2).get().getValue().get());
        Assertions.assertFalse(result.results().getKey("cars").get().getIndex(0).get().getKey("models")
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

        ValidateOf<ConfigNode> result = yamlLoader.loadSource(source);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("Steve", result.results().getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.results().getKey("age").get().getValue().get());
        Assertions.assertEquals("Ford", result.results().getKey("cars").get().getIndex(0).get().getKey("name")
            .get().getValue().get());
        Assertions.assertEquals("Fiesta", result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Focus", result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Mustang", result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(2).get().getValue().get());
        Assertions.assertFalse(result.results().getKey("cars").get().getIndex(0).get().getKey("models")
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

        ValidateOf<ConfigNode> result = yamlLoader.loadSource(source);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("Steve", result.results().getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.results().getKey("age").get().getValue().get());
        Assertions.assertEquals("Ford", result.results().getKey("cars").get().getIndex(0).get().getKey("name")
            .get().getValue().get());
        Assertions.assertEquals("Fiesta", result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("Focus", result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("Mustang", result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(2).get().getValue().get());
        Assertions.assertFalse(result.results().getKey("cars").get().getIndex(0).get().getKey("models")
            .get().getIndex(3).isPresent());
    }

    @Test
    void loadSourceEmptyValue() throws GestaltException {

        StringConfigSource source = new StringConfigSource("name: Steve\n" +
            "age: \n", "yml");

        YamlLoader yamlLoader = new YamlLoader();

        ValidateOf<ConfigNode> result = yamlLoader.loadSource(source);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals("Unable to find node matching path: age", result.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: age", result.getErrors().get(1).description());

        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("Steve", result.results().getKey("name").get().getValue().get());
        Assertions.assertFalse(result.results().getKey("age").isPresent());
    }

    @Test
    void loadSourceEmptyArray() throws GestaltException {

        StringConfigSource source = new StringConfigSource("name: Steve\n" +
            "age: 42\n" +
            "cars: \n", "yml");

        YamlLoader yamlLoader = new YamlLoader();

        ValidateOf<ConfigNode> result = yamlLoader.loadSource(source);

        Assertions.assertTrue(result.hasErrors());
        Assertions.assertEquals("Unable to find node matching path: cars", result.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: cars", result.getErrors().get(1).description());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("Steve", result.results().getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.results().getKey("age").get().getValue().get());
        Assertions.assertFalse(result.results().getKey("cars").isPresent());
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
}
