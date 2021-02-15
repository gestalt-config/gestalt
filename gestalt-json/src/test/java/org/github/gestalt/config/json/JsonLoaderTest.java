package org.github.gestalt.config.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        ValidateOf<ConfigNode> result = jsonLoader.loadSource(source);

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
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        ValidateOf<ConfigNode> result = jsonLoader.loadSource(source);

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
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader(new ObjectMapper());

        ValidateOf<ConfigNode> result = jsonLoader.loadSource(source);

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
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        ValidateOf<ConfigNode> result = jsonLoader.loadSource(source);

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
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        ValidateOf<ConfigNode> result = jsonLoader.loadSource(source);

        Assertions.assertFalse(result.hasErrors());
        Assertions.assertTrue(result.hasResults());
        Assertions.assertEquals("Steve", result.results().getKey("name").get().getValue().get());
        Assertions.assertEquals("42", result.results().getKey("age").get().getValue().get());
        Assertions.assertEquals(0, result.results().getKey("cars").get().size());
    }

    @Test
    void loadSourceEmptyObject() throws GestaltException {

        StringConfigSource source = new StringConfigSource("{\n" +
            "  \"name\":\"Steve\",\n" +
            "  \"age\":42,\n" +
            "  \"cars\": {}\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        ValidateOf<ConfigNode> result = jsonLoader.loadSource(source);

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
            "  \"cars\": {},\n" +
            " } ", "json");

        JsonLoader jsonLoader = new JsonLoader();

        try {
            jsonLoader.loadSource(source);
            Assertions.fail("should not reach here");
        } catch (Exception e) {
            Assertions.assertEquals("Exception loading source: String format: json", e.getMessage());
        }
    }
}
