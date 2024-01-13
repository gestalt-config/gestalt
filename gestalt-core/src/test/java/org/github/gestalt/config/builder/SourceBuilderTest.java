package org.github.gestalt.config.builder;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reload.ConfigReloadStrategy;
import org.github.gestalt.config.reload.ManualConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.tag.Tag;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("resource")
public class SourceBuilderTest {

    private TestSourceBuilder sourceBuilder;

    @BeforeEach
    public void setUp() {
        sourceBuilder = new TestSourceBuilder();
    }

    @Test
    public void testSourceBuilder() {
        sourceBuilder = sourceBuilder.setSource("abc=def")
            .setTags(Tags.environment("dev"));

        assertEquals("abc=def", sourceBuilder.getSource());
        assertEquals(Tags.environment("dev"), sourceBuilder.getTags());
    }

    @Test
    public void testBuilderConfigSourcePackageCreation() throws GestaltException, IOException {
        var source = new StringConfigSource("abc=def", "properties", Tags.environment("dev"));
        ConfigSourcePackage configSourcePackage = new ConfigSourcePackage(source, List.of());

        var built = sourceBuilder.setSource("abc=def")
            .setTags(Tags.environment("dev"))
            .build();
        assertEquals(new String(configSourcePackage.getConfigSource().loadStream().readAllBytes(), Charset.defaultCharset()),
            new String(built.getConfigSource().loadStream().readAllBytes(), Charset.defaultCharset()));
        assertEquals(configSourcePackage.getConfigReloadStrategies(), built.getConfigReloadStrategies());
    }

    @Test
    public void testConfigSourcePackageCreation() throws GestaltException {
        var source = new StringConfigSource("abc=def", "properties");
        ConfigSourcePackage configSourcePackage = new ConfigSourcePackage(source, List.of());
        assertEquals(source, configSourcePackage.getConfigSource());
        assertEquals(0, configSourcePackage.getConfigReloadStrategies().size());
    }

    @Test
    public void setSourceShouldThrowExceptionOnNullSource() {
        GestaltException e = assertThrows(GestaltException.class, () -> sourceBuilder.build());

        assertEquals("The string provided was null", e.getMessage());
    }

    @Test
    public void setTagsShouldSetTags() {
        Tags tags = Tags.environment("tag1");
        sourceBuilder.setTags(tags);
        assertEquals(tags, sourceBuilder.getTags());
    }

    @Test
    public void setTagsShouldThrowExceptionOnNullTags() {
        assertThrows(NullPointerException.class, () -> sourceBuilder.setTags(null));
    }

    @Test
    public void addTagShouldAddTag() {
        Tag tag = Tag.of("env", "tag1");
        sourceBuilder.addTag(tag);
        Tags tags = sourceBuilder.getTags();
        assertNotNull(tags);
        assertTrue(tags.getTags().contains(tag));
    }

    @Test
    public void addTagShouldThrowExceptionOnNullTag() {
        assertThrows(NullPointerException.class, () -> sourceBuilder.addTag(null));
    }

    @Test
    public void addConfigReloadStrategyBuilderShouldAddStrategyBuilder() {
        ManualConfigReloadStrategy reload = new ManualConfigReloadStrategy();
        sourceBuilder.addConfigReloadStrategy(reload);
        List<ConfigReloadStrategy> strategyBuilders = sourceBuilder.getConfigReloadStrategies();
        assertNotNull(strategyBuilders);
        assertEquals(1, strategyBuilders.size());
        assertTrue(strategyBuilders.contains(reload));
    }

    @Test
    public void addConfigReloadStrategyBuilderShouldThrowExceptionOnNullStrategyBuilder() {
        assertThrows(NullPointerException.class, () -> sourceBuilder.addConfigReloadStrategy(null));
    }

    @Test
    public void buildShouldCreateConfigSourcePackage() throws GestaltException {
        sourceBuilder.setSource("abc=def");
        ConfigSourcePackage result = sourceBuilder.build();
        assertNotNull(result);
    }

    private static class TestSourceBuilder extends SourceBuilder<TestSourceBuilder, StringConfigSource> {

        String text;

        public String getSource() {
            return text;
        }

        public TestSourceBuilder setSource(String source) {
            this.text = source;
            return this;
        }

        @Override
        public ConfigSourcePackage build() throws GestaltException {
            return buildPackage(new StringConfigSource(text, "properties", tags));
        }
    }
}


