package org.github.gestalt.config.token;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TokenTest {

    @Test
    void testEquals() {
        ArrayToken arrayToken = new ArrayToken(0);
        ArrayToken arrayTokenSame = new ArrayToken(0);
        ArrayToken arrayTokenDiff = new ArrayToken(1);

        ObjectToken objectToken = new ObjectToken("test");
        ObjectToken objectTokenSame = new ObjectToken("test");
        ObjectToken objectTokenDiff = new ObjectToken("different");

        TagToken tagToken = new TagToken("tag", "token");
        TagToken tagTokenSame = new TagToken("tag", "token");
        TagToken tagTokenDiff = new TagToken("diff", "diff");

        Assertions.assertEquals(arrayToken, arrayToken);
        Assertions.assertEquals(arrayToken, arrayTokenSame);
        Assertions.assertNotEquals(arrayToken, arrayTokenDiff);
        Assertions.assertNotEquals(arrayToken, objectToken);

        Assertions.assertEquals(objectToken, objectToken);
        Assertions.assertEquals(objectToken, objectTokenSame);
        Assertions.assertNotEquals(objectToken, objectTokenDiff);
        Assertions.assertNotEquals(objectToken, arrayToken);

        Assertions.assertEquals(tagToken, tagToken);
        Assertions.assertEquals(tagToken, tagTokenSame);
        Assertions.assertNotEquals(tagToken, tagTokenDiff);
        Assertions.assertNotEquals(tagToken, arrayToken);
    }

    @Test
    void testHashCode() {
        ArrayToken arrayToken = new ArrayToken(0);
        ObjectToken objectToken = new ObjectToken("test");
        TagToken tagToken = new TagToken("tag", "token");

        Assertions.assertEquals(31, arrayToken.hashCode());


        Assertions.assertEquals(3556529, objectToken.hashCode());


        Assertions.assertEquals(114094432, tagToken.hashCode());
    }

    @Test
    void testArrayIndex() {
        ArrayToken arrayToken = new ArrayToken(0);

        Assertions.assertEquals(0, arrayToken.getIndex());
    }

    @Test
    void testGetName() {
        ObjectToken objectToken = new ObjectToken("test");

        Assertions.assertEquals("test", objectToken.getName());
    }

    @Test
    void testGetTag() {
        TagToken tagToken = new TagToken("tag", "token");

        Assertions.assertEquals("tag", tagToken.getTag());
        Assertions.assertEquals("token", tagToken.getValue());
    }
}
