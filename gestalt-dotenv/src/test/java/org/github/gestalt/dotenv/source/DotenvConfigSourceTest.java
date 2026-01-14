package org.github.gestalt.dotenv.source;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.github.gestalt.config.loader.PropertyLoader;
import org.github.gestalt.config.source.EnvironmentConfigSource;
import org.github.gestalt.config.utils.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DotenvConfigSourceTest {

    private Dotenv dotenv;

    @BeforeEach
    void setUp() {
        dotenv = mock(Dotenv.class);
    }

    @Test
    void testNameAndDefaultFormatAndFlags() {
        DotenvConfigSource source = new DotenvConfigSource(dotenv);

        assertEquals("dotEnv", source.name());
        assertEquals(EnvironmentConfigSource.ENV_VARS, source.format());
        assertFalse(source.hasStream());
        assertTrue(source.hasList());
        assertNull(source.getTags());
        assertNull(source.id());
    }

    @Test
    void testNameAndCustomFormatAndFlags() {
        DotenvConfigSource source = new DotenvConfigSource(dotenv, "properties");

        assertEquals("dotEnv", source.name());
        assertEquals("properties", source.format());
        assertFalse(source.hasStream());
        assertTrue(source.hasList());
        assertNull(source.getTags());
        assertNull(source.id());
    }

    @Test
    void testLoadStreamThrowsUnsupported() {
        DotenvConfigSource source = new DotenvConfigSource(dotenv);

        assertThrows(UnsupportedOperationException.class, source::loadStream);
    }

    @Test
    void testLoadListWithoutFilter() throws Exception {
        DotenvEntry entry1 = mock(DotenvEntry.class);
        DotenvEntry entry2 = mock(DotenvEntry.class);

        when(entry1.getKey()).thenReturn("KEY1");
        when(entry1.getValue()).thenReturn("VALUE1");
        when(entry2.getKey()).thenReturn("KEY2");
        when(entry2.getValue()).thenReturn("VALUE2");

        Set<DotenvEntry> entries = Set.of(entry1, entry2);
        when(dotenv.entries()).thenReturn(entries);

        DotenvConfigSource source = new DotenvConfigSource(dotenv);

        List<Pair<String, String>> list = source.loadList();

        assertNotNull(list);
        assertEquals(2, list.size());
        // Ensure both expected pairs are present
        assertTrue(list.stream().anyMatch(p -> p.getFirst().equals("KEY1") && p.getSecond().equals("VALUE1")));
        assertTrue(list.stream().anyMatch(p -> p.getFirst().equals("KEY2") && p.getSecond().equals("VALUE2")));
    }

    @Test
    void testLoadListWithFilter() throws Exception {
        Dotenv.Filter filter = mock(Dotenv.Filter.class);

        DotenvEntry entry = mock(DotenvEntry.class);
        when(entry.getKey()).thenReturn("SOME_KEY");
        when(entry.getValue()).thenReturn("SOME_VALUE");

        Set<DotenvEntry> entries = Set.of(entry);
        when(dotenv.entries(filter)).thenReturn(entries);

        DotenvConfigSource source = new DotenvConfigSource(dotenv, filter);

        List<Pair<String, String>> list = source.loadList();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("SOME_KEY", list.get(0).getFirst());
        assertEquals("SOME_VALUE", list.get(0).getSecond());
    }

    @Test
    void testEqualsAndHashCode() {
        DotenvConfigSource a = new DotenvConfigSource(dotenv);
        DotenvConfigSource b = new DotenvConfigSource(dotenv);

        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, 10L);
        assertNotEquals(a.hashCode(), b.hashCode());
    }
}

