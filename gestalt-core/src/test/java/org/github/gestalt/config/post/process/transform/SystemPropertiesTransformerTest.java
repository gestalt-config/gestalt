package org.github.gestalt.config.post.process.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SystemPropertiesTransformerTest {

    @Test
    void name() {
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        Assertions.assertEquals("sys", systemPropertiesTransformer.name());
    }

    @Test
    void process() {
        System.getProperties().put("test", "value");
        SystemPropertiesTransformer systemPropertiesTransformer = new SystemPropertiesTransformer();
        Assertions.assertEquals("value", systemPropertiesTransformer.process("path", "test").get());
    }
}
