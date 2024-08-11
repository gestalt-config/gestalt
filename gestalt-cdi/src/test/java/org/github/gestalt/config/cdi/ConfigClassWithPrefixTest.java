package org.github.gestalt.config.cdi;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConfigClassWithPrefixTest {

    @Test
    public void tests() {
        ConfigClassWithPrefix prefix1 = new ConfigClassWithPrefix(String.class, "test");
        ConfigClassWithPrefix prefix2 = ConfigClassWithPrefix.configClassWithPrefix(String.class, "test");
        ConfigClassWithPrefix prefix3 = ConfigClassWithPrefix.configClassWithPrefix(String.class, "notTest");
        ConfigClassWithPrefix prefix4 = ConfigClassWithPrefix.configClassWithPrefix(Integer.class, "test");

        Assertions.assertEquals(prefix1, prefix1);
        Assertions.assertEquals(prefix1, prefix2);
        Assertions.assertNotEquals(prefix1, prefix3);
        Assertions.assertNotEquals(prefix1, prefix4);
        Assertions.assertNotEquals(prefix1, null);
        Assertions.assertNotEquals(prefix1, 1);
    }
}
