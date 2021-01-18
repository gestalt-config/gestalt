package org.config.gestalt.source;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.utils.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapConfigSourceTest {

    private Map<String, String> configs;

    @BeforeEach
    void setup() {
        configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
    }

    @Test
    void hasStream() {
        MapConfigSource mapConfigSource = new MapConfigSource(configs);
        Assertions.assertFalse(mapConfigSource.hasStream());
    }

    @Test
    void loadStream() {
        MapConfigSource mapConfigSource = new MapConfigSource(configs);
        Assertions.assertThrows(GestaltException.class, mapConfigSource::loadStream);
    }

    @Test
    void hasList() {
        MapConfigSource mapConfigSource = new MapConfigSource(configs);
        Assertions.assertTrue(mapConfigSource.hasList());
    }

    @Test
    void loadList() {
        MapConfigSource mapConfigSource = new MapConfigSource(configs);

        List<Pair<String, String>> configs = mapConfigSource.loadList();
        Assertions.assertNotNull(configs);
        Assertions.assertEquals(2, configs.size());
        assertThat(configs).contains(new Pair<>("db.name", "test"))
            .contains(new Pair<>("db.port", "3306"));
    }

    @Test
    void format() {
        MapConfigSource mapConfigSource = new MapConfigSource(configs);
        Assertions.assertEquals("mapConfig", mapConfigSource.format());
    }

    @Test
    void name() {
        MapConfigSource mapConfigSource = new MapConfigSource(configs);
        Assertions.assertEquals("mapConfig", mapConfigSource.name());
    }


    @Test
    void equals() {
        MapConfigSource mapConfigSource = new MapConfigSource(configs);
        MapConfigSource mapConfigSource2 = new MapConfigSource(configs);

        Assertions.assertEquals(mapConfigSource, mapConfigSource);
        Assertions.assertNotEquals(mapConfigSource, mapConfigSource2);
        Assertions.assertNotEquals(mapConfigSource, null);
        Assertions.assertNotEquals(mapConfigSource, Long.valueOf(1));
    }

    @Test
    void hash() {
        MapConfigSource mapConfigSource = new MapConfigSource(configs);
        Assertions.assertTrue(mapConfigSource.hashCode() != 0);
    }
}
