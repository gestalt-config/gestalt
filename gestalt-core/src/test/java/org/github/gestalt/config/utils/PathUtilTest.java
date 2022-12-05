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
}
