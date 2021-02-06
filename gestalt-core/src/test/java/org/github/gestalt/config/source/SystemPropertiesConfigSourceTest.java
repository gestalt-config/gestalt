package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class SystemPropertiesConfigSourceTest {

    @Test
    void hasStream() {
        SystemPropertiesConfigSource configSource = new SystemPropertiesConfigSource();

        Assertions.assertTrue(configSource.hasStream());
    }

    @Test
    void loadStream() throws GestaltException {
        SystemPropertiesConfigSource configSource = new SystemPropertiesConfigSource();
        Assertions.assertNotNull(configSource.loadStream());
    }

    @Test
    void hasList() {
        SystemPropertiesConfigSource configSource = new SystemPropertiesConfigSource();

        Assertions.assertTrue(configSource.hasList());
    }

    @Test
    void loadList() {

        SystemPropertiesConfigSource configSource = new SystemPropertiesConfigSource();

        List<Pair<String, String>> list = configSource.loadList();

        Optional<Pair<String, String>> version = list.stream().filter(it -> it.getFirst().equals("java.specification.version")).findFirst();

        Assertions.assertEquals("java.specification.version", version.get().getFirst());
        Assertions.assertEquals("11", version.get().getSecond());
    }

    @Test
    void format() {
        SystemPropertiesConfigSource configSource = new SystemPropertiesConfigSource();

        Assertions.assertEquals(SystemPropertiesConfigSource.SYSTEM_PROPERTIES, configSource.format());
    }

    @Test
    void name() {
        SystemPropertiesConfigSource configSource = new SystemPropertiesConfigSource();

        Assertions.assertEquals(SystemPropertiesConfigSource.SYSTEM_PROPERTIES, configSource.name());
    }

    @Test
    void idTest() {
        SystemPropertiesConfigSource configSource = new SystemPropertiesConfigSource();
        Assertions.assertNotNull(configSource.id());
    }

    @Test
    void testEquals() {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");

        SystemPropertiesConfigSource configSource = new SystemPropertiesConfigSource();
        MapConfigSource mapConfigSource2 = new MapConfigSource(configs);

        Assertions.assertEquals(configSource, configSource);
        Assertions.assertNotEquals(configSource, mapConfigSource2);
        Assertions.assertNotEquals(configSource, null);
        Assertions.assertNotEquals(configSource, Long.valueOf(1));
    }

    @Test
    void testHashCode() {
        SystemPropertiesConfigSource configSource = new SystemPropertiesConfigSource();
        Assertions.assertTrue(configSource.hashCode() != 0);
    }
}
