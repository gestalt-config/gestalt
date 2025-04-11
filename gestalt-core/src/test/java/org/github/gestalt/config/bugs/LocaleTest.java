package org.github.gestalt.config.bugs;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class LocaleTest {

    private static final Locale savedLocale = Locale.getDefault();

    public static class TimeValue {

        private final long duration;

        private final TimeUnit timeUnit;

        public TimeValue(String time) {
            TimeValue timeValue = parseTimeValue(time);
            this.duration = timeValue.duration;
            this.timeUnit = timeValue.timeUnit;
        }

        public TimeValue(long duration, TimeUnit timeUnit) {
            this.duration = duration;
            this.timeUnit = timeUnit;
        }

        public static TimeValue parseTimeValue(String sValue) {
            if (sValue == null) {
                return null;
            }
            try {
                String lowerSValue = sValue.toLowerCase(Locale.ROOT).trim();
                long duration = Long.parseLong(lowerSValue.substring(0, lowerSValue.length() - 1));
                TimeUnit unit;
                if (lowerSValue.endsWith("ms")) {
                    // Well, with ms, we need to substring 2 chars
                    duration = Long.parseLong(lowerSValue.substring(0, lowerSValue.length() - 2));
                    unit = TimeUnit.MILLISECONDS;
                } else if (lowerSValue.endsWith("s")) {
                    unit = TimeUnit.SECONDS;
                } else if (lowerSValue.endsWith("m")) {
                    unit = TimeUnit.MINUTES;
                } else if (lowerSValue.endsWith("h")) {
                    unit = TimeUnit.HOURS;
                } else if (lowerSValue.endsWith("d")) {
                    unit = TimeUnit.DAYS;
                } else {
                    throw new IllegalArgumentException("Failed to parse timevalue [" + sValue + "]: unit is missing or unrecognized");
                }
                return new TimeValue(duration, unit);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Failed to parse timevalue [" + sValue + "].");
            }
        }
    }

    public static class Elasticsearch {
        @Config(defaultVal = "5s")
        private TimeValue flushInterval;
    }

    @AfterAll
    public static void resetLocale() {
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

    @Test
    public void testLocaleEn() throws Exception {
        Locale.setDefault(new Locale.Builder().setLanguageTag("en").build());
        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder()
                .addCustomConfig("elasticsearch.index", "foo")
                .build())
            .build();
        gestalt.loadConfigs();
        assertThat(gestalt.getConfigOptional("elasticsearch", Elasticsearch.class)).isPresent();
    }
}
