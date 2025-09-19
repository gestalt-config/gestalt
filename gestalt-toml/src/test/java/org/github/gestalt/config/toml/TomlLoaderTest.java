package org.github.gestalt.config.toml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
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

        GestaltConfig config = new GestaltConfig();
        config.setSentenceLexer(new PathLexer());

        tomlLoader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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
    void loadSourceLexedMultiPath() throws GestaltException {

        StringConfigSource source = new StringConfigSource("user.name = \"Steve\" \n" +
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

        GestaltConfig config = new GestaltConfig();
        config.setSentenceLexer(new PathLexer());

        tomlLoader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Steve", result.getKey("user").get().getKey("name").get().getValue().get());
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
    void loadSourceWithModuleConfig() throws GestaltException {
        var lexer = new PathLexer();
        var objectMapper = new ObjectMapper(new TomlFactory()).findAndRegisterModules();

        var moduleConfig = TomlModuleConfigBuilder.builder()
            .setObjectMapper(objectMapper)
            .setLexer(lexer)
            .build();


        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        TomlLoader tomlLoader = new TomlLoader();
        tomlLoader.applyConfig(config);

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

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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
    void loadSourceWithModuleConfigConstructor() throws GestaltException {
        var lexer = new PathLexer();
        var objectMapper = new ObjectMapper(new TomlFactory()).findAndRegisterModules();

        var moduleConfig = TomlModuleConfigBuilder.builder()
            .setObjectMapper(objectMapper)
            .setLexer(lexer)
            .build();


        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        TomlLoader tomlLoader = new TomlLoader(new ObjectMapper(new TomlFactory()).findAndRegisterModules(), new PathLexer());
        tomlLoader.applyConfig(config);

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

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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
    void loadSourceWithModuleConfigGestaltConfigLexer() throws GestaltException {
        var lexer = new PathLexer();
        var objectMapper = new ObjectMapper(new TomlFactory()).findAndRegisterModules();

        var moduleConfig = TomlModuleConfigBuilder.builder()
            .setObjectMapper(objectMapper)
            //.setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);
        config.setSentenceLexer(lexer);

        TomlLoader tomlLoader = new TomlLoader();
        tomlLoader.applyConfig(config);

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

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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
    void loadSourceWithModuleConfigNoMapper() throws GestaltException {
        var lexer = new PathLexer();

        var moduleConfig = TomlModuleConfigBuilder.builder()
            .setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        TomlLoader tomlLoader = new TomlLoader();
        tomlLoader.applyConfig(config);

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

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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

        TomlLoader tomlLoader = new TomlLoader(new ObjectMapper(new TomlFactory()), new PathLexer());

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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
    void loadSourceTomlSample() throws GestaltException {

        StringConfigSource source = new StringConfigSource("\n" +
            "\n" +
            "# This is a TOML document\n" +
            "\n" +
            "title = \"TOML Example\"\n" +
            "\n" +
            "[owner]\n" +
            "name = \"Tom Preston-Werner\"\n" +
            "dob = 1979-05-27T07:32:00-08:00\n" +
            "\n" +
            "[database]\n" +
            "enabled = true\n" +
            "ports = [ 8000, 8001, 8002 ]\n" +
            "data = [ [\"delta\", \"phi\"], [3.14] ]\n" +
            "temp_targets = { cpu = 79.5, case = 72.0 }\n" +
            "\n" +
            "[servers]\n" +
            "\n" +
            "[servers.alpha]\n" +
            "ip = \"10.0.0.1\"\n" +
            "role = \"frontend\"\n" +
            "\n" +
            "[servers.beta]\n" +
            "ip = \"10.0.0.2\"\n" +
            "role = \"backend\"\n" +
            "\n", "toml");

        TomlLoader tomlLoader = new TomlLoader(new ObjectMapper(new TomlFactory()), new PathLexer());

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Tom Preston-Werner", result.getKey("owner").get().getKey("name").get().getValue().get());
        Assertions.assertEquals("1979-05-27T07:32:00-08:00", result.getKey("owner").get().getKey("dob").get().getValue().get());

        Assertions.assertEquals("true", result.getKey("database").get().getKey("enabled").get().getValue().get());
        Assertions.assertEquals("8000", result.getKey("database").get().getKey("ports").get().getIndex(0).get().getValue().get());
        Assertions.assertEquals("8001", result.getKey("database").get().getKey("ports").get().getIndex(1).get().getValue().get());
        Assertions.assertEquals("8002", result.getKey("database").get().getKey("ports").get().getIndex(2).get().getValue().get());

        Assertions.assertEquals("delta", result.getKey("database").get().getKey("data").get().getIndex(0).get().getIndex(0).get()
            .getValue().get());
        Assertions.assertEquals("phi", result.getKey("database").get().getKey("data").get().getIndex(0).get().getIndex(1).get()
            .getValue().get());
        Assertions.assertEquals("3.14", result.getKey("database").get().getKey("data").get().getIndex(1).get().getIndex(0).get()
            .getValue().get());

        Assertions.assertEquals("79.5", result.getKey("database").get().getKey("temp_targets").get().getKey("cpu").get()
            .getValue().get());
        Assertions.assertEquals("72", result.getKey("database").get().getKey("temp_targets").get().getKey("case").get()
            .getValue().get());

        Assertions.assertEquals("10.0.0.1", result.getKey("servers").get().getKey("alpha").get().getKey("ip").get() //NOPMD
            .getValue().get());
        Assertions.assertEquals("frontend", result.getKey("servers").get().getKey("alpha").get().getKey("role").get()
            .getValue().get());


        Assertions.assertEquals("10.0.0.2", result.getKey("servers").get().getKey("beta").get().getKey("ip").get()  //NOPMD
            .getValue().get());
        Assertions.assertEquals("backend", result.getKey("servers").get().getKey("beta").get().getKey("role").get()
            .getValue().get());
    }

    @Test
    void loadSourceEmptyValue() throws GestaltException {

        StringConfigSource source = new StringConfigSource("name = \n" +
            "age = 42\n", "toml");

        TomlLoader tomlLoader = new TomlLoader();

        try {
            tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not reach here, should throw exception");
        } catch (GestaltException e) {
            Assertions.assertEquals("Exception loading source: String format: toml", e.getMessage());
        }
    }

    @Test
    void loadSourceEmpty() throws GestaltException {

        StringConfigSource source = new StringConfigSource("", "toml");

        TomlLoader tomlLoader = new TomlLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(0, result.size());
    }

    @Test
    void loadSourceBadInput() throws GestaltException {

        StringConfigSource source = new StringConfigSource("&&&& ", "toml");

        TomlLoader tomlLoader = new TomlLoader();

        try {
            tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
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
            tomlLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Config source: mapConfig does not have a stream to load.", e.getMessage());
        }
    }
}
