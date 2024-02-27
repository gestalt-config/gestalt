package org.github.gestalt.config.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    void isInteger() {
        Assertions.assertTrue(StringUtils.isInteger("1"));
        Assertions.assertTrue(StringUtils.isInteger("234"));
        Assertions.assertTrue(StringUtils.isInteger("-234"));
        Assertions.assertFalse(StringUtils.isInteger("-"));
        Assertions.assertFalse(StringUtils.isInteger("--"));
        Assertions.assertFalse(StringUtils.isInteger("0-"));
        Assertions.assertFalse(StringUtils.isInteger("354s4"));
        Assertions.assertFalse(StringUtils.isInteger("354.4"));
        Assertions.assertFalse(StringUtils.isInteger("354-4"));
        Assertions.assertFalse(StringUtils.isInteger("a"));
        Assertions.assertFalse(StringUtils.isInteger("-a"));
        Assertions.assertFalse(StringUtils.isInteger(""));
        Assertions.assertFalse(StringUtils.isInteger(null));
    }

    @Test
    void isReal() {
        Assertions.assertTrue(StringUtils.isReal("1"));
        Assertions.assertTrue(StringUtils.isReal("234"));
        Assertions.assertTrue(StringUtils.isReal("-234"));
        Assertions.assertFalse(StringUtils.isReal("-"));
        Assertions.assertFalse(StringUtils.isReal("--"));
        Assertions.assertFalse(StringUtils.isReal("0-"));
        Assertions.assertFalse(StringUtils.isReal("354s4"));
        Assertions.assertFalse(StringUtils.isReal("354-4"));
        Assertions.assertFalse(StringUtils.isReal("a"));
        Assertions.assertFalse(StringUtils.isReal("-a"));
        Assertions.assertFalse(StringUtils.isReal(""));
        Assertions.assertFalse(StringUtils.isReal(null));

        Assertions.assertTrue(StringUtils.isReal("1.0"));
        Assertions.assertTrue(StringUtils.isReal("10.0"));
        Assertions.assertTrue(StringUtils.isReal("1."));
        Assertions.assertTrue(StringUtils.isReal("10."));
        Assertions.assertTrue(StringUtils.isReal("0.1"));
        Assertions.assertTrue(StringUtils.isReal("0.11"));
        Assertions.assertTrue(StringUtils.isReal(".1"));
        Assertions.assertTrue(StringUtils.isReal("-.9"));
        Assertions.assertTrue(StringUtils.isReal("-1.9"));
        Assertions.assertFalse(StringUtils.isReal("-"));
        Assertions.assertFalse(StringUtils.isReal("-."));
        Assertions.assertFalse(StringUtils.isReal("."));
        Assertions.assertFalse(StringUtils.isReal(".-"));
        Assertions.assertFalse(StringUtils.isReal(".."));
        Assertions.assertFalse(StringUtils.isReal("3.54.4"));
        Assertions.assertFalse(StringUtils.isReal("3.54s4"));
        Assertions.assertFalse(StringUtils.isReal("3.."));
    }

    @Test
    void startsWith() {
        var stringToMatch = "ABC_DEF";
        Assertions.assertFalse(StringUtils.startsWith(stringToMatch, "DEF", false));
        Assertions.assertFalse(StringUtils.startsWith(stringToMatch, "DEF", true));

        Assertions.assertTrue(StringUtils.startsWith(stringToMatch, "ABC", false));
        Assertions.assertTrue(StringUtils.startsWith(stringToMatch, "ABC", true));

        Assertions.assertFalse(StringUtils.startsWith(stringToMatch, "abc", false));
        Assertions.assertTrue(StringUtils.startsWith(stringToMatch, "abc", true));

        Assertions.assertFalse(StringUtils.startsWith(stringToMatch, "this is a really long prefix", false));
    }

    @Test
    void startsWithNull() {
        Assertions.assertTrue(StringUtils.startsWith(null, null, false));
        Assertions.assertTrue(StringUtils.startsWith(null, null, true));

        Assertions.assertFalse(StringUtils.startsWith("ABC_DEF", null, false));
        Assertions.assertFalse(StringUtils.startsWith(null, "ABC", true));
    }
}

