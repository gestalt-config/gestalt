package org.config.gestalt.decoder;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.node.ConfigNodeService;
import org.config.gestalt.node.LeafNode;
import org.config.gestalt.reflect.TypeCapture;
import org.config.gestalt.token.ArrayToken;
import org.config.gestalt.token.ObjectToken;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.ValidateOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

class DecoderRegistryTest {

    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void getDecoder() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer);

        List<Decoder> decoders = decoderRegistry.getDecoders();

        Assertions.assertEquals(4, decoders.size());
    }


    @Test
    void setDecoder() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer);

        List<Decoder> decoders = decoderRegistry.getDecoders();

        Assertions.assertEquals(4, decoders.size());

        decoderRegistry.setDecoders(Arrays.asList(new DoubleDecoder(), new LongDecoder()));

        decoders = decoderRegistry.getDecoders();

        Assertions.assertEquals(2, decoders.size());
    }

    @Test
    void getDecoderForClass() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer);

        List<Decoder> decoders = decoderRegistry.getDecoderForClass(TypeCapture.of(String.class));

        Assertions.assertEquals(1, decoders.size());
        Assertions.assertTrue(decoders.get(0).matches(TypeCapture.of(String.class)));
    }

    @Test
    void addDecoderForClass() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Collections.singletonList(new StringDecoder()), configNodeService, lexer);

        List<Decoder> decoders = decoderRegistry.getDecoderForClass(TypeCapture.of(Double.class));

        Assertions.assertEquals(0, decoders.size());

        decoderRegistry.addDecoders(Collections.singletonList(new DoubleDecoder()));

        decoders = decoderRegistry.getDecoderForClass(TypeCapture.of(Double.class));

        Assertions.assertEquals(1, decoders.size());
        Assertions.assertTrue(decoders.get(0).matches(TypeCapture.of(Double.class)));
    }

    @Test
    void decoderRegistryConfigNodeNull() {
        try {
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder()), null, lexer);
        } catch (GestaltException e) {
            Assertions.assertEquals("ConfigNodeService can not be null", e.getMessage());
        }
    }

    @Test
    void decoderLexerNull() {
        try {
            new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder()), configNodeService, null);
        } catch (GestaltException e) {
            Assertions.assertEquals("SentenceLexer can not be null", e.getMessage());
        }
    }

    @Test
    void getDecoderForClassNull() {
        try {
            new DecoderRegistry(null, configNodeService, lexer);
        } catch (GestaltException e) {
            Assertions.assertEquals("Decoder list was null", e.getMessage());
        }
    }

    @Test
    void getNextNodeObject() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder(), new LongDecoder()), configNodeService, lexer);

        ConfigNode leaf = new LeafNode("test");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(ValidateOf.valid(Collections.singletonList(nextToken)));
        Mockito.when(configNodeService.navigateToNextNode("test", nextToken, leaf)).thenReturn(ValidateOf.valid(leaf));


        ValidateOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals(leaf, test.results());

        Mockito.verify(lexer, Mockito.times(1)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNextNode(any(), any(), any());
    }

    @Test
    void decodeNode() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer);

        ConfigNode leaf = new LeafNode("value");

        ValidateOf<String> test = decoderRegistry.decodeNode("test", leaf, TypeCapture.of(String.class));
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals("value", test.results());
    }

    @Test
    void decodeNodeDuplicates() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder(), new LongDecoder()), configNodeService, lexer);

        ConfigNode leaf = new LeafNode("100");

        ValidateOf<Long> test = decoderRegistry.decodeNode("test", leaf, TypeCapture.of(Long.class));
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals(100, test.results());
    }

    @Test
    void decodeNodeEmpty() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Collections.emptyList(), configNodeService, lexer);

        ConfigNode leaf = new LeafNode("value");

        ValidateOf<String> test = decoderRegistry.decodeNode("test", leaf, TypeCapture.of(String.class));
        Assertions.assertFalse(test.hasResults());
        Assertions.assertTrue(test.hasErrors());

        Assertions.assertEquals(1, test.getErrors().size());
        Assertions.assertEquals("No decoders found for class: java.lang.String", test.getErrors().get(0).description());
    }

    @Test
    void getNextNodeObjectBadToken() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer);

        ConfigNode leaf = new LeafNode("value");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(ValidateOf.inValid(new ValidationError.FailedToTokenizeElement("run", "test.run")));
        Mockito.when(configNodeService.navigateToNextNode("test", nextToken, leaf)).thenReturn(ValidateOf.valid(leaf));


        ValidateOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertFalse(test.hasResults());
        Assertions.assertTrue(test.hasErrors());

        Assertions.assertEquals(1, test.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element run for path: test.run", test.getErrors().get(0).description());

        Mockito.verify(lexer, Mockito.times(1)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(0)).navigateToNextNode(any(), any(), any());
    }

    @Test
    void getNextNodeObjectNoResultToken() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer);

        ConfigNode leaf = new LeafNode("value");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(ValidateOf.validateOf(null, Collections.emptyList()));
        Mockito.when(configNodeService.navigateToNextNode("test", nextToken, leaf)).thenReturn(ValidateOf.valid(leaf));


        ValidateOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertFalse(test.hasResults());
        Assertions.assertTrue(test.hasErrors());

        Assertions.assertEquals(1, test.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: test, for class: MapNode", test.getErrors().get(0).description());

        Mockito.verify(lexer, Mockito.times(1)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(0)).navigateToNextNode(any(), any(), any());
    }

    @Test
    void getNextNodeArray() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Arrays.asList(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer);

        ConfigNode leaf = new LeafNode("value");

        Token nextToken = new ArrayToken(1);
        Mockito.when(lexer.scan("run")).thenReturn(ValidateOf.valid(Collections.singletonList(nextToken)));
        Mockito.when(configNodeService.navigateToNextNode("test", nextToken, leaf)).thenReturn(ValidateOf.valid(leaf));


        ValidateOf<ConfigNode> test = decoderRegistry.getNextNode("test", 1, leaf);
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals(leaf, test.results());

        Mockito.verify(lexer, Mockito.times(0)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNextNode(any(), any(), any());
    }
}
