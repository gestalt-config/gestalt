package org.github.gestalt.config.bugs;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

public class ConfigTest {
    private static final Locale savedLocale = Locale.getDefault();

    public static class Elasticsearch {
        private String index;

        public Elasticsearch() {
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }
    }

    @AfterEach
    public void resetLocale() {
        Locale.setDefault(savedLocale);
    }

    @Test
    public void testLocaleUS() throws Exception {
        Locale.setDefault(savedLocale);
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .addCustomConfig("elasticsearch.index", "foo")
                .build())
            .build();
        gestalt.loadConfigs();
        Assertions.assertTrue(gestalt.getConfigOptional("elasticsearch", Elasticsearch.class).isPresent());
        Assertions.assertEquals("foo", gestalt.getConfigOptional("elasticsearch", Elasticsearch.class).get().index);
    }

    @Test
    public void testLocaleAz() throws Exception {
        Locale.setDefault(new Locale.Builder().setLanguageTag("az").build());
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .addCustomConfig("elasticsearch.index", "foo")
                .build())
            .build();
        gestalt.loadConfigs();
        Assertions.assertTrue(gestalt.getConfigOptional("elasticsearch", Elasticsearch.class).isPresent());
        Assertions.assertEquals("foo", gestalt.getConfigOptional("elasticsearch", Elasticsearch.class).get().index);
    }

    @Test
    public void testLocaleFr() throws Exception {
        Locale.setDefault(new Locale.Builder().setLanguageTag("fr").build());
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .addCustomConfig("elasticsearch.index", "foo")
                .build())
            .build();
        gestalt.loadConfigs();
        Assertions.assertTrue(gestalt.getConfigOptional("elasticsearch", Elasticsearch.class).isPresent());
        Assertions.assertEquals("foo", gestalt.getConfigOptional("elasticsearch", Elasticsearch.class).get().index);
    }
}
