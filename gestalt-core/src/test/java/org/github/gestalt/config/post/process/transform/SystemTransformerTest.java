package org.github.gestalt.config.post.process.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SystemTransformerTest {

    @Test
    void name() {
        SystemTransformer systemTransformer = new SystemTransformer();
        Assertions.assertEquals("sys", systemTransformer.name());
    }

    @Test
    void process() {
        System.getProperties().put("test", "value");
        SystemTransformer systemTransformer = new SystemTransformer();
        Assertions.assertEquals("value", systemTransformer.process("path", "test").get());
    }
}
