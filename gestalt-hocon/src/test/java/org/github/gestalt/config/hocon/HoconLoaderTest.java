package org.github.gestalt.config.hocon;

import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class HoconLoaderTest {

    @Test
    void name() {
        HoconLoader hoconLoader = new HoconLoader();
        Assertions.assertEquals("HoconLoader", hoconLoader.name());
    }

    @Test
    void accepts() {
        HoconLoader hoconLoader = new HoconLoader();
        Assertions.assertTrue(hoconLoader.accepts("conf"));
        Assertions.assertFalse(hoconLoader.accepts("properties"));
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
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"user.name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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
    void loadSourceModuleConfig() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var configParser = ConfigParseOptions.defaults();
        var lexer = new PathLexer();
        var moduleConfig = HoconModuleConfigBuilder.builder()
            .setConfigParseOptions(configParser)
            .setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        hoconLoader.applyConfig(config);

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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
    void loadSourceModuleConfigConstructor() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader(ConfigParseOptions.defaults(), new PathLexer());

        var configParser = ConfigParseOptions.defaults();
        var lexer = new PathLexer();
        var moduleConfig = HoconModuleConfigBuilder.builder()
            .setConfigParseOptions(configParser)
            .setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);

        hoconLoader.applyConfig(config);

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var configParser = ConfigParseOptions.defaults();
        var lexer = new PathLexer();
        var moduleConfig = HoconModuleConfigBuilder.builder()
            .setConfigParseOptions(configParser)
            //.setLexer(lexer)
            .build();

        GestaltConfig config = new GestaltConfig();
        config.registerModuleConfig(moduleConfig);
        config.setSentenceLexer(lexer);

        hoconLoader.applyConfig(config);

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"Age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"Models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"Models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"Models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        GestaltConfig config = new GestaltConfig();
        config.setSentenceLexer(new PathLexer());

        hoconLoader.applyConfig(config);

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

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
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader(ConfigParseOptions.defaults().setSyntax(ConfigSyntax.JSON),
            new PathLexer());

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
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
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("", result.getKey("age").get().getValue().get());
    }

    @Test
    void loadSourceEmpty() throws GestaltException {

        StringConfigSource source = new StringConfigSource("", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals(0, result.size());
    }

    @Test
    void loadSourceEmptyArray() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": []\n" +
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("Steve", result.getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.getKey("age").get().getValue().get());
        Assertions.assertEquals(0, result.getKey("cars").get().size());
    }

    @Test
    void loadSourceEmptyObject() throws GestaltException {

        StringConfigSource source = new StringConfigSource(
            "  \"name\"=\"Steve\",\n" +
                "  \"age\":42,\n" +
                "  \"cars\": {}\n", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
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
            "  \"cars\": [1,2,3,,]\n" +
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        try {
            hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Exception loading source: String format: conf", e.getMessage());
        }
    }

    @Test
    void loadSourceReferential() throws GestaltException {

        StringConfigSource source = new StringConfigSource(
            "path : \"a:b:c\"\n" +
                "path : ${path}\":d\"", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("a:b:c:d", result.getKey("path").get().getValue().get());
    }

    @Test
    void loadSourceReferentialEnvironmentVariables() throws GestaltException {

        StringConfigSource source = new StringConfigSource(
            "path : ${DB_IDLETIMEOUT}", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        var resultContainer = hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
        Assertions.assertFalse(resultContainer.hasErrors());
        Assertions.assertTrue(resultContainer.hasResults());

        ConfigNode result = resultContainer.results().get(0).getConfigNode();
        Assertions.assertEquals("123", result.getKey("path").get().getValue().get());
    }

    @Test
    void loadSourceBadSource() {

        Map<String, String> configs = new HashMap<>();
        MapConfigSource source = new MapConfigSource(configs);

        HoconLoader hoconLoader = new HoconLoader();

        try {
            hoconLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("HOCON Config source: mapConfig does not have a stream to load.", e.getMessage());
        }
    }
}
