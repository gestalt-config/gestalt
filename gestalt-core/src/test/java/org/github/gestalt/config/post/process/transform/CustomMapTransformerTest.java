package org.github.gestalt.config.post.process.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class CustomMapTransformerTest {

    @Test
    void name() {
        CustomMapTransformer transformer = new CustomMapTransformer(Collections.emptyMap());
        Assertions.assertEquals("map", transformer.name());
    }

    @Test
    void process() {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("test", "value");
        CustomMapTransformer transformer = new CustomMapTransformer(customMap);
        Assertions.assertEquals("value", transformer.process("hello", "test").get());
        Assertions.assertFalse(transformer.process("hello", "novalue").isPresent());
    }
}
