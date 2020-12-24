package org.config.gestalt.utils;

import org.config.gestalt.token.ArrayToken;
import org.config.gestalt.token.ObjectToken;
import org.config.gestalt.token.TagToken;
import org.config.gestalt.token.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class PathUtilTest {

    @Test
    void toPath() {
        List<Token> tokens = Arrays.asList(new ObjectToken("obj"), new ArrayToken(1), new TagToken("tag", "value"));
        Assertions.assertEquals("obj[1]tag=value", PathUtil.toPath(tokens));
    }
}
