package org.github.gestalt.config.decoder;

import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.*;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.test.classes.DBInfoNullable;
import org.github.gestalt.config.test.classes.DBInfoNullableGetter;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

class ObjectDecoderNullTest {

    final SentenceLexer lexer = new PathLexer();
    ConfigNodeService configNodeService;
    DecoderRegistry decoderService;

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = ObjectDecoderNullTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        decoderService = new DecoderRegistry(List.of(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder(), new OptionalDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));
    }

    @Test
    void decodeNullable() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        //configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoNullable.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.uri, with node: " +
            "MapNode{password=LeafNode{value='pass'}, port=LeafNode{value='100'}}, with class: DBInfoNullable",
            result.getErrors().get(0).description());

        DBInfoNullable results = (DBInfoNullable) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertNull(results.getUri());
    }

    @Test
    void decodeNullableMethod() {
        ObjectDecoder decoder = new ObjectDecoder();

        Map<String, ConfigNode> configs = new HashMap<>();
        configs.put("port", new LeafNode("100"));
        //configs.put("uri", new LeafNode("mysql.com"));
        configs.put("password", new LeafNode("pass"));

        GResultOf<Object> result = decoder.decode("db.host", Tags.of(), new MapNode(configs),
            TypeCapture.of(DBInfoNullableGetter.class),
            new DecoderContext(decoderService, null, null, new PathLexer()));
        Assertions.assertTrue(result.hasResults());
        Assertions.assertTrue(result.hasErrors());

        Assertions.assertEquals(1, result.getErrors().size());
        Assertions.assertEquals(ValidationLevel.MISSING_OPTIONAL_VALUE, result.getErrors().get(0).level());
        Assertions.assertEquals("Missing Optional Value while decoding Object on path: db.host.uri, with node: " +
            "MapNode{password=LeafNode{value='pass'}, port=LeafNode{value='100'}}, with class: DBInfoNullableGetter",
            result.getErrors().get(0).description());

        DBInfoNullableGetter results = (DBInfoNullableGetter) result.results();
        Assertions.assertEquals(100, results.getPort());
        Assertions.assertEquals("pass", results.getPassword());
        Assertions.assertNull(results.getUri());
    }
}

