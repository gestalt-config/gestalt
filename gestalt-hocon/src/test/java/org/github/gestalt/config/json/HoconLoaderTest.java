package org.github.gestalt.config.json;

import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.hocon.HoconLoader;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        ValidateOf<ConfigNode> result = hoconLoader.loadSource(source);

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

        ValidateOf<ConfigNode> result = hoconLoader.loadSource(source);

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

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": [\n" +
            "    { \"name\":\"Ford\", \"models\":[ \"Fiesta\", \"Focus\", \"Mustang\" ] },\n" +
            "    { \"name\":\"BMW\", \"models\":[ \"320\", \"X3\", \"X5\" ] },\n" +
            "    { \"name\":\"Fiat\", \"models\":[ \"500\", \"Panda\" ] }\n" +
            "  ]\n" +
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader(ConfigParseOptions.defaults().setSyntax(ConfigSyntax.JSON));

        ValidateOf<ConfigNode> result = hoconLoader.loadSource(source);

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

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":\"\"\n" +
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        ValidateOf<ConfigNode> result = hoconLoader.loadSource(source);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("Steve", result.results().getKey("name").get().getValue().get());
        Assertions.assertEquals("", result.results().getKey("age").get().getValue().get());
    }

    @Test
    void loadSourceEmptyArray() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": []\n" +
            " } ", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        ValidateOf<ConfigNode> result = hoconLoader.loadSource(source);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("Steve", result.results().getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.results().getKey("age").get().getValue().get());
        Assertions.assertEquals(0, result.results().getKey("cars").get().size());
    }

    @Test
    void loadSourceEmptyObject() throws GestaltException {

        StringConfigSource source = new StringConfigSource(
            "  \"name\"=\"Steve\",\n" +
                "  \"age\":42,\n" +
                "  \"cars\": {}\n", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        ValidateOf<ConfigNode> result = hoconLoader.loadSource(source);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("Steve", result.results().getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.results().getKey("age").get().getValue().get());
        Assertions.assertEquals(0, result.results().getKey("cars").get().size());
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
            hoconLoader.loadSource(source);
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

        ValidateOf<ConfigNode> result = hoconLoader.loadSource(source);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("a:b:c:d", result.results().getKey("path").get().getValue().get());
    }

    @Test
    void loadSourceReferentialEnvironmentVariables() throws GestaltException {

        StringConfigSource source = new StringConfigSource(
            "path : ${DB_IDLETIMEOUT}", "conf");

        HoconLoader hoconLoader = new HoconLoader();

        ValidateOf<ConfigNode> result = hoconLoader.loadSource(source);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("123", result.results().getKey("path").get().getValue().get());
    }
}
