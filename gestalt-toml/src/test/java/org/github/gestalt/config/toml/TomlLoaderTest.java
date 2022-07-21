package org.github.gestalt.config.toml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        ValidateOf<ConfigNode> result = tomlLoader.loadSource(source);

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

        ValidateOf<ConfigNode> result = tomlLoader.loadSource(source);

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

        ValidateOf<ConfigNode> result = tomlLoader.loadSource(source);

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
}
