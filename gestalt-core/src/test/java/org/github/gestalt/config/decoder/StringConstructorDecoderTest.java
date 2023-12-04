package org.github.gestalt.config.decoder;

import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.github.gestalt.config.entity.ValidationLevel.ERROR;
import static org.github.gestalt.config.entity.ValidationLevel.MISSING_VALUE;

class StringConstructorDecoderTest {

    @Test
    void priority() {
        StringConstructorDecoder decoder = new StringConstructorDecoder();

        Assertions.assertEquals(Priority.LOW, decoder.priority());
    }

    @Test
    void name() {
        StringConstructorDecoder decoder = new StringConstructorDecoder();

        Assertions.assertEquals("StringConstructor", decoder.name());
    }

    @Test
    void canDecode() {
        StringConstructorDecoder decoder = new StringConstructorDecoder();

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), TypeCapture.of(MyClass.class)));
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<MyClass>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<MyStringClass>() {
        }));
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), new LeafNode(""), new TypeCapture<MyStringClass>() {
        }));

        Assertions.assertFalse(decoder.canDecode("", Tags.of(), new MapNode(Map.of()), new TypeCapture<MyStringClass>() {
        }));

    }

    @Test
    void decode() {

        StringConstructorDecoder decoder = new StringConstructorDecoder();
        var results = decoder.decode("hello", Tags.of(), new LeafNode("test"), TypeCapture.of(MyStringClass.class), null);

        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        MyStringClass decoded = (MyStringClass) results.results();

        Assertions.assertEquals("test", decoded.myData);
    }

    @Test
    void decodeEmptyLeaf() {

        StringConstructorDecoder decoder = new StringConstructorDecoder();
        var results = decoder.decode("hello", Tags.of(), new LeafNode(null), TypeCapture.of(MyStringClass.class), null);

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());

        Assertions.assertEquals(MISSING_VALUE, results.getErrors().get(0).level());
        Assertions.assertEquals("Leaf nodes is null on path: hello decoding type MyStringClass",
            results.getErrors().get(0).description());
    }

    @Test
    void decodeEmptyWrongType() {

        StringConstructorDecoder decoder = new StringConstructorDecoder();
        var results = decoder.decode("hello", Tags.of(), new LeafNode("test"), TypeCapture.of(MyClass.class), null);

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());

        Assertions.assertEquals(ERROR, results.getErrors().get(0).level());
        Assertions.assertEquals("String Constructor for: MyClass is not found on Path: hello",
            results.getErrors().get(0).description());
    }

    private static class MyClass {
        Integer myData;

        public MyClass(Integer myData) {
            this.myData = myData;
        }

        public MyClass() {

        }
    }

    private static class MyStringClass {
        String myData;

        public MyStringClass(String myData) {
            this.myData = myData;
        }

        public MyStringClass() {

        }
    }
}
