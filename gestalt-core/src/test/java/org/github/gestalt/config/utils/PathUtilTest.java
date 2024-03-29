package org.github.gestalt.config.utils;

import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.ObjectToken;
import org.github.gestalt.config.token.TagToken;
import org.github.gestalt.config.token.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class PathUtilTest {

    @Test
    void toPath() {
        List<Token> tokens = List.of(new ObjectToken("obj"), new ArrayToken(1), new TagToken("tag", "value"));
        Assertions.assertEquals("obj[1]tag=value", PathUtil.toPath(tokens));
    }

    @Test
    void pathForKey() {
        Assertions.assertEquals("my.path.test", PathUtil.pathForKey("my.path", "test"));
        Assertions.assertEquals("test", PathUtil.pathForKey("", "test"));
        Assertions.assertEquals("test", PathUtil.pathForKey(null, "test"));
    }

    @Test
    void pathForIndex() {
        Assertions.assertEquals("my.path[0]", PathUtil.pathForIndex("my.path", 0));
        Assertions.assertEquals("[0]", PathUtil.pathForIndex("", 0));
        Assertions.assertEquals("[0]", PathUtil.pathForIndex(null, 0));
    }
}
