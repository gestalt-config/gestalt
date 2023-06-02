package org.github.gestalt.config.toml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TomlLoaderTest {

    @Test
    void name() {
        TomlLoader tomlLoader = new TomlLoader();
        Assertions.assertEquals("tomlLoader", tomlLoader.name());
    }

    @Test
    void accepts() {
        TomlLoader tomlLoader = new TomlLoader();
        Assertions.assertTrue(tomlLoader.accepts("toml"));
        Assertions.assertFalse(tomlLoader.accepts("properties"));
    }

    @Test
    void loadSource() throws GestaltException {

        StringConfigSource source = new StringConfigSource("name = \"Steve\" \n" +
            "age = 42\n" +

            "[[cars]]\n" +
            "name = \"Ford\"\n" +
            "models = [ \"Fiesta\", \"Focus\", \"Mustang\" ]\n" +

            "[[cars]]\n" +
            "name = \"BMW\"\n" +
            "models = [ \"320\", \"X3\", \"X5\" ]\n" +

            "[[cars]]\n" +
            "name = \"Fiat\"\n" +
            "models = [ \"500\", \"Panda\" ]\n", "toml");

        TomlLoader tomlLoader = new TomlLoader();

        ValidateOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(source);

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

        StringConfigSource source = new StringConfigSource("name = \"Steve\"\n" +
            "Age = 42\n" +

            "[[cars]]\n" +
            "name = \"Ford\"\n" +
            "models = [ \"Fiesta\", \"Focus\", \"Mustang\" ]\n" +

            "[[cars]]\n" +
            "name = \"BMW\"\n" +
            "models = [ \"320\", \"X3\", \"X5\" ]\n" +

            "[[cars]]\n" +
            "name = \"Fiat\"\n" +
            "models = [ \"500\", \"Panda\" ]", "toml");

        TomlLoader tomlLoader = new TomlLoader();

        ValidateOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(source);

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

        StringConfigSource source = new StringConfigSource("name = \"Steve\"\n" +
            "age = 42\n" +

            "[[cars]]\n" +
            "name = \"Ford\"\n" +
            "models = [ \"Fiesta\", \"Focus\", \"Mustang\" ]\n" +

            "[[cars]]\n" +
            "name = \"BMW\"\n" +
            "models = [ \"320\", \"X3\", \"X5\" ]\n" +

            "[[cars]]\n" +
            "name = \"Fiat\"\n" +
            "models = [ \"500\", \"Panda\" ]\n", "toml");

        TomlLoader tomlLoader = new TomlLoader(new ObjectMapper(new TomlFactory()));

        ValidateOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(source);

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

        StringConfigSource source = new StringConfigSource("name = \n" +
            "age = 42\n", "toml");

        TomlLoader tomlLoader = new TomlLoader();

        try {
            tomlLoader.loadSource(source);
            Assertions.fail("should not reach here, should throw exception");
        } catch (GestaltException e) {
            Assertions.assertEquals("Exception loading source: String format: toml", e.getMessage());
        }
    }

    @Test
    void loadSourceBadInput() throws GestaltException {

        StringConfigSource source = new StringConfigSource("&&&& ", "toml");

        TomlLoader tomlLoader = new TomlLoader();

        try {
            tomlLoader.loadSource(source);
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Exception loading source: String format: toml", e.getMessage());
        }
    }

    @Test
    void loadSourceBadSource() {

        Map<String, String> configs = new HashMap<>();
        MapConfigSource source = new MapConfigSource(configs);

        TomlLoader tomlLoader = new TomlLoader();

        try {
            tomlLoader.loadSource(source);
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Config source: mapConfig does not have a stream to load.", e.getMessage());
        }
    }
}
