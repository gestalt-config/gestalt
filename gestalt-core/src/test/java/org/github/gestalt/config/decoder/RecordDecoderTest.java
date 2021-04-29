package org.github.gestalt.config.decoder;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNodeManager;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.test.classes.DBInfo;
import org.github.gestalt.config.test.classes.DBInfoExtended;
import org.github.gestalt.config.test.classes.DBInfoGeneric;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

class RecordDecoderTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer = new PathLexer();
    DecoderRegistry registry;

    @BeforeEach
    void setup() throws GestaltConfigurationException {
        configNodeService = new ConfigNodeManager();
        registry = new DecoderRegistry(Arrays.asList(new LongDecoder(), new IntegerDecoder(), new StringDecoder(),
            new ObjectDecoder(), new FloatDecoder()), configNodeService, lexer);
    }

    @Test
    void name() {
        RecordDecoder decoder = new RecordDecoder();
        Assertions.assertEquals("Record", decoder.name());
    }

    @Test
    void priority() {
        RecordDecoder decoder = new RecordDecoder();
        Assertions.assertEquals(Priority.MEDIUM, decoder.priority());
    }

    @Test
    void matches() {
        RecordDecoder decoder = new RecordDecoder();

        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfo.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(DBInfoExtended.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Long.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Long>() {
        }));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(long.class)));

        Assertions.assertFalse(decoder.matches(TypeCapture.of(String.class)));
        Assertions.assertFalse(decoder.matches(TypeCapture.of(Date.class)));
        Assertions.assertFalse(decoder.matches(new TypeCapture<List<Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<Map<String, Long>>() {
        }));
        Assertions.assertFalse(decoder.matches(new TypeCapture<DBInfoGeneric<String>>() {
        }));
    }
}
