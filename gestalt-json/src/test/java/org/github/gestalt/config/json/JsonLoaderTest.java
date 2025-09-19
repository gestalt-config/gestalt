package org.github.gestalt.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
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

class JsonLoaderTest {

    @Test
    void name() {
        JsonLoader jsonLoader = new JsonLoader();
        Assertions.assertEquals("JsonLoader", jsonLoader.name());
    }

    @Test
    void accepts() {
        JsonLoader jsonLoader = new JsonLoader();
        Assertions.assertTrue(jsonLoader.accepts("json"));
        Assertions.assertFalse(jsonLoader.accepts("properties"));
    }

    @Test
    void loadSource() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();
        GestaltConfig config = new GestaltConfig();
        config.setSentenceLexer(new PathLexer());

        jsonLoader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
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
    void loadSourceLexedMiltiPath() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"user.name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();
        GestaltConfig config = new GestaltConfig();
        config.setSentenceLexer(new PathLexer());

        jsonLoader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
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
    void loadSourceModuleConfig() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();
        GestaltConfig config = new GestaltConfig();

        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var lexer = new PathLexer();
        var moduleConfig = JsonModuleConfigBuilder.builder()
            .setObjectMapper(objectMapper)
            .setLexer(lexer)
            .build();

        config.registerModuleConfig(moduleConfig);

        jsonLoader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
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
    void loadSourceModuleConfigConstructor() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader(new ObjectMapper(), new PathLexer());
        GestaltConfig config = new GestaltConfig();

        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var lexer = new PathLexer();
        var moduleConfig = JsonModuleConfigBuilder.builder()
            .setObjectMapper(objectMapper)
            .setLexer(lexer)
            .build();

        config.registerModuleConfig(moduleConfig);

        jsonLoader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
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
    void loadSourceModuleConfigNoMapper() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();
        GestaltConfig config = new GestaltConfig();

        var lexer = new PathLexer();
        var moduleConfig = JsonModuleConfigBuilder.builder()
            .setLexer(lexer)
            .build();

        config.registerModuleConfig(moduleConfig);

        jsonLoader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
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

    @SuppressWarnings("VariableDeclarationUsageDistance")
    @Test
    void loadSourceModuleConfigGestaltConfigLexer() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();
        GestaltConfig config = new GestaltConfig();

        var objectMapper = new ObjectMapper().findAndRegisterModules();
        var lexer = new PathLexer();
        var moduleConfig = JsonModuleConfigBuilder.builder()
            .setObjectMapper(objectMapper)
            //.setLexer(lexer)
            .build();

        config.registerModuleConfig(moduleConfig);
        config.setSentenceLexer(lexer);

        jsonLoader.applyConfig(config);

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals(Tags.of(), resultContainer.results().get(0).getTags());
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

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"Age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"Models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"Models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"Models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader(new ObjectMapper(), new PathLexer());

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
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

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":\"\"\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("", result.getKey("age").get().getValue().get());
    }

    @Test
    void loadSourceEmptyArray() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": []\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());
        ConfigNode result = resultContainer.results().get(0).getConfigNode();

        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals(0, result.getKey("cars").get().size());
    }

    @Test
    void loadSourceEmptyObject() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": {}\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        GResultOf<List<ConfigNodeContainer>> resultContainer = jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals(0, result.getKey("cars").get().size());
    }

    @Test
    void loadSourceBadInput() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": {},\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        try {
            jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Exception loading source: String format: json", e.getMessage());
        }
    }

    @Test
    void loadSourceBadSource() {

        Map<String, String> configs = new HashMap<>();
        MapConfigSource source = new MapConfigSource(configs);

        JsonLoader jsonLoader = new JsonLoader();

        try {
            jsonLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Config source: mapConfig does not have a stream to load.", e.getMessage());
        }
    }
}
