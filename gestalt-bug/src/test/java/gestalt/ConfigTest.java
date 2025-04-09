package gestalt;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.source.*;
import org.junit.After;
import org.junit.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

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

    @After
    public void resetLocale() {
        Locale.setDefault(savedLocale);
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
        assertThat(gestalt.getConfigOptional("elasticsearch", Elasticsearch.class)).isPresent();
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
        assertThat(gestalt.getConfigOptional("elasticsearch", Elasticsearch.class)).isPresent();
    }
}
