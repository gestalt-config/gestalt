package org.github.gestalt.config.decoder;

import org.github.gestalt.config.annotations.ConfigPriority;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.NodeType;
import org.github.gestalt.config.path.mapper.PathMapper;
import org.github.gestalt.config.path.mapper.StandardPathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogManager;

import static org.mockito.ArgumentMatchers.any;

@SuppressWarnings({"unchecked", "rawtypes"})
class DecoderRegistryTest {
    ConfigNodeService configNodeService;
    SentenceLexer lexer;

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = DecoderRegistryTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @BeforeEach
    void setup() {
        configNodeService = Mockito.mock(ConfigNodeService.class);
        lexer = Mockito.mock(SentenceLexer.class);
    }

    @Test
    void getDecoder() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        List<Decoder<?>> decoders = decoderRegistry.getDecoders();

        Assertions.assertEquals(4, decoders.size());
    }


    @Test
    void setDecoder() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        List<Decoder<?>> decoders = decoderRegistry.getDecoders();

        Assertions.assertEquals(4, decoders.size());

        decoderRegistry.setDecoders(List.of(new DoubleDecoder(), new LongDecoder()));

        decoders = decoderRegistry.getDecoders();

        Assertions.assertEquals(2, decoders.size());
    }

    @Test
    void getDecoderForClass() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        List<Decoder> decoders = decoderRegistry.getDecoderForClass("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class));

        Assertions.assertEquals(1, decoders.size());
        Assertions.assertTrue(decoders.get(0).canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(String.class)));
    }

    @Test
    void addDecoderForClass() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(Collections.singletonList(new StringDecoder()), configNodeService, lexer,
            List.of(new StandardPathMapper()));

        List<Decoder> decoders = decoderRegistry.getDecoderForClass("", Tags.of(), new LeafNode(""), TypeCapture.of(Double.class));

        Assertions.assertEquals(0, decoders.size());

        decoderRegistry.addDecoders(Collections.singletonList(new DoubleDecoder()));

        decoders = decoderRegistry.getDecoderForClass("", Tags.of(), new LeafNode(""), TypeCapture.of(Double.class));

        Assertions.assertEquals(1, decoders.size());
        Assertions.assertTrue(decoders.get(0).canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(Double.class)));
    }

    @Test
    void decoderRegistryPathMapperNull() {
        try {
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder()), configNodeService, lexer, null);
        } catch (GestaltException e) {
            Assertions.assertEquals("pathMappers can not be null or empty", e.getMessage());
        }
    }

    @Test
    void decoderRegistryPathMapperEmpty() {
        try {
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder()), configNodeService, lexer, List.of());
        } catch (GestaltException e) {
            Assertions.assertEquals("pathMappers can not be null or empty", e.getMessage());
        }
    }

    @Test
    void decoderRegistryConfigNodeNull() {
        try {
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder()), null, lexer, List.of(new StandardPathMapper()));
        } catch (GestaltException e) {
            Assertions.assertEquals("ConfigNodeService can not be null", e.getMessage());
        }
    }

    @Test
    void decoderLexerNull() {
        try {
            new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
                new StringDecoder()), configNodeService, null, List.of(new StandardPathMapper()));
        } catch (GestaltException e) {
            Assertions.assertEquals("SentenceLexer can not be null", e.getMessage());
        }
    }

    @Test
    void getDecoderForClassNull() {
        try {
            new DecoderRegistry(null, configNodeService, lexer, List.of(new StandardPathMapper()));
        } catch (GestaltException e) {
            Assertions.assertEquals("Decoder list was null or empty", e.getMessage());
        }
    }

    @Test
    void decoderRegistryGetAndSetPathMapper() throws GestaltConfigurationException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        List<PathMapper> pathMappers = List.of(new StandardPathMapper(), new TestPathMapper());
        decoderRegistry.setPathMappers(pathMappers);
        Assertions.assertEquals(decoderRegistry.getPathMappers(), pathMappers);
    }

    @Test
    void getNextNodeObject() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder(), new LongDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        ConfigNode leaf = new LeafNode("test");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(GResultOf.result(Collections.singletonList(nextToken)));
        Mockito.when(configNodeService.navigateToNextNode("test", List.of(nextToken), leaf)).thenReturn(GResultOf.result(leaf));


        GResultOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals(leaf, test.results());

        Mockito.verify(lexer, Mockito.times(1)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNextNode(any(), any(List.class), any());
    }

    @Test
    void decodeNode() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        ConfigNode leaf = new LeafNode("value");

        GResultOf<String> test = decoderRegistry.decodeNode("test", Tags.of(), leaf, TypeCapture.of(String.class),
            new DecoderContext(decoderRegistry, null, null, new PathLexer()));
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals("value", test.results());
    }

    @Test
    void decodeNodeDuplicates() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder(), new LongDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        ConfigNode leaf = new LeafNode("100");

        GResultOf<Long> test = decoderRegistry.decodeNode("test", Tags.of(), leaf, TypeCapture.of(Long.class),
            new DecoderContext(decoderRegistry, null, null, new PathLexer()));
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals(100, test.results());
    }

    @Test
    void decodeNodeDuplicatesCustom() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder(), new LongDecoderCustomHigh(), new LongDecoderCustomVH()), configNodeService, lexer,
            List.of(new StandardPathMapper()));

        ConfigNode leaf = new LeafNode("100");

        GResultOf<Long> test = decoderRegistry.decodeNode("test", Tags.of(), leaf, TypeCapture.of(Long.class),
            new DecoderContext(decoderRegistry, null, null, new PathLexer()));
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals(1000L, test.results());
    }

    @Test
    void decodeNodeEmpty() {
        GestaltException ex = Assertions.assertThrows(GestaltException.class,
            () -> new DecoderRegistry(Collections.emptyList(), configNodeService, lexer, List.of(new StandardPathMapper())));
        Assertions.assertEquals("Decoder list was null or empty", ex.getMessage());

    }

    @Test
    void getNextNodeObjectBadToken() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        ConfigNode leaf = new LeafNode("value");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(GResultOf.errors(new ValidationError.FailedToTokenizeElement("run", "test.run")));
        Mockito.when(configNodeService.navigateToNextNode("test", nextToken, leaf)).thenReturn(GResultOf.result(leaf));


        GResultOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertFalse(test.hasResults());
        Assertions.assertTrue(test.hasErrors());

        Assertions.assertEquals(1, test.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element run for path: test.run", test.getErrors().get(0).description());

        Mockito.verify(lexer, Mockito.times(1)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(0)).navigateToNextNode(any(), any(Token.class), any());
    }

    @Test
    void getNextNodeObjectNoResultToken() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        ConfigNode leaf = new LeafNode("value");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(GResultOf.resultOf(null, Collections.emptyList()));
        Mockito.when(configNodeService.navigateToNextNode("test", nextToken, leaf)).thenReturn(GResultOf.result(leaf));


        GResultOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertFalse(test.hasResults());
        Assertions.assertTrue(test.hasErrors());

        Assertions.assertEquals(1, test.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: test, for class: leaf, during decoding",
            test.getErrors().get(0).description());

        Mockito.verify(lexer, Mockito.times(1)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(0)).navigateToNextNode(any(), any(Token.class), any());
    }

    @Test
    void getNextNodeArray() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new StandardPathMapper()));

        ConfigNode leaf = new LeafNode("value");

        Token nextToken = new ArrayToken(1);
        Mockito.when(lexer.scan("run")).thenReturn(GResultOf.result(Collections.singletonList(nextToken)));
        Mockito.when(configNodeService.navigateToNextNode("test", List.of(nextToken), leaf)).thenReturn(GResultOf.result(leaf));


        GResultOf<ConfigNode> test = decoderRegistry.getNextNode("test", 1, leaf);
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals(leaf, test.results());

        Mockito.verify(lexer, Mockito.times(0)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNextNode(any(), any(List.class), any());
    }

    @Test
    void getNextNodeMultiPathMappers() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new StandardPathMapper(), new TestPathMapper()));

        ConfigNode leaf = new LeafNode("test");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(GResultOf.result(Collections.singletonList(nextToken)));
        Mockito.when(configNodeService.navigateToNextNode("test", List.of(nextToken), leaf)).thenReturn(GResultOf.result(leaf));


        GResultOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals(leaf, test.results());

        Mockito.verify(lexer, Mockito.times(1)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNextNode(any(), any(List.class), any());
    }

    @Test
    void getNextNodeMultiPathMappersFirstNoFind() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new TestPathMapper(), new StandardPathMapper()));

        ConfigNode leaf = new LeafNode("test");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(GResultOf.result(Collections.singletonList(nextToken)));
        Mockito.when(configNodeService.navigateToNextNode("test", List.of(nextToken), leaf)).thenReturn(GResultOf.result(leaf));


        GResultOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertTrue(test.hasResults());
        Assertions.assertFalse(test.hasErrors());

        Assertions.assertEquals(leaf, test.results());

        Mockito.verify(lexer, Mockito.times(1)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNextNode(any(), any(List.class), any());
    }

    @Test
    void getNextNodeMultiPathMappersNoneFind() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new TestPathMapper(), new TestPathMapper()));

        ConfigNode leaf = new LeafNode("test");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(GResultOf.result(Collections.singletonList(nextToken)));
        Mockito.when(configNodeService.navigateToNextNode("test", List.of(nextToken), leaf)).thenReturn(GResultOf.result(leaf));


        GResultOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertFalse(test.hasResults());
        Assertions.assertTrue(test.hasErrors());

        Assertions.assertEquals(2, test.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: test, for class: leaf, during TestPathMapper",
            test.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: test, for class: leaf, during TestPathMapper",
            test.getErrors().get(1).description());

        Mockito.verify(lexer, Mockito.times(0)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(0)).navigateToNextNode(any(), any(List.class), any());
    }

    @Test
    void getNextNodeMultiPathMappersNoneFindDuringNavigateToNextNode() throws GestaltException {
        DecoderRegistry decoderRegistry = new DecoderRegistry(List.of(new DoubleDecoder(), new LongDecoder(), new IntegerDecoder(),
            new StringDecoder()), configNodeService, lexer, List.of(new TestPathMapper(), new StandardPathMapper()));

        ConfigNode leaf = new LeafNode("test");

        Token nextToken = new ObjectToken("run");
        Mockito.when(lexer.scan("run")).thenReturn(GResultOf.result(Collections.singletonList(nextToken)));
        Mockito.when(configNodeService.navigateToNextNode("test", List.of(nextToken), leaf))
            .thenReturn(GResultOf.errors(new ValidationError.NoResultsFoundForNode("test",
                NodeType.LEAF.getType(), "navigate to next node")));

        GResultOf<ConfigNode> test = decoderRegistry.getNextNode("test", "run", leaf);
        Assertions.assertFalse(test.hasResults());
        Assertions.assertTrue(test.hasErrors());

        Assertions.assertEquals(2, test.getErrors().size());
        Assertions.assertEquals("Unable to find node matching path: test, for class: leaf, during navigate to next node",
            test.getErrors().get(0).description());
        Assertions.assertEquals("Unable to find node matching path: test, for class: leaf, during TestPathMapper",
            test.getErrors().get(1).description());

        Mockito.verify(lexer, Mockito.times(1)).scan(any());
        Mockito.verify(configNodeService, Mockito.times(1)).navigateToNextNode(any(), any(List.class), any());
    }

    @ConfigPriority(500)
    public static class TestPathMapper implements PathMapper {
        @Override
        public GResultOf<List<Token>> map(String path, String sentence, SentenceLexer lexer) {
            return GResultOf.errors(new ValidationError.NoResultsFoundForNode(path, NodeType.LEAF.getType(), "TestPathMapper"));
        }
    }

    private static class LongDecoderCustomHigh extends LeafDecoder<Long> {

        @Override
        public Priority priority() {
            return Priority.HIGH;
        }

        @Override
        public String name() {
            return "LongDecoderCustom1";
        }

        @Override
        public boolean canDecode(String path, Tags tags, ConfigNode configNode, TypeCapture<?> klass) {
            return Long.class.isAssignableFrom(klass.getRawType()) || long.class.isAssignableFrom(klass.getRawType());
        }


        @Override
        protected GResultOf<Long> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
            return GResultOf.result(10L);
        }
    }

    private static class LongDecoderCustomVH extends LeafDecoder<Long> {

        @Override
        public Priority priority() {
            return Priority.VERY_HIGH;
        }

        @Override
        public String name() {
            return "LongDecoderCustom1";
        }

        @Override
        public boolean canDecode(String path, Tags tags, ConfigNode configNode, TypeCapture<?> klass) {
            return Long.class.isAssignableFrom(klass.getRawType()) || long.class.isAssignableFrom(klass.getRawType());
        }


        @Override
        protected GResultOf<Long> leafDecode(String path, ConfigNode node, DecoderContext decoderContext) {
            return GResultOf.result(1000L);
        }
    }
}
