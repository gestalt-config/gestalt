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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        var source = new StringConfigSource("abc=def", "properties");
        ConfigSourcePackage configSourcePackage = new ConfigSourcePackage(source, List.of(), Tags.environment("dev"));

        var built = sourceBuilder.setSource("abc=def")
            .setTags(Tags.environment("dev"))
            .build();
        assertEquals(new String(configSourcePackage.getConfigSource().loadStream().readAllBytes(), Charset.defaultCharset()),
            new String(built.getConfigSource().loadStream().readAllBytes(), Charset.defaultCharset()));
        assertEquals(configSourcePackage.getConfigReloadStrategies(), built.getConfigReloadStrategies());
        assertEquals(configSourcePackage.getTags(), built.getTags());
    }

    @Test
    @SuppressWarnings("removal")
    public void testBuilderConfigSourceTagsOnSource() throws GestaltException, IOException {
        var source = new StringConfigSource("abc=def", "properties", Tags.environment("dev"));
        ConfigSourcePackage configSourcePackage = new ConfigSourcePackage(source, List.of(), Tags.of());

        var built = sourceBuilder.setSource("abc=def")
            .setTags(Tags.environment("dev"))
            .build();
        assertEquals(new String(configSourcePackage.getConfigSource().loadStream().readAllBytes(), Charset.defaultCharset()),
            new String(built.getConfigSource().loadStream().readAllBytes(), Charset.defaultCharset()));
        assertEquals(configSourcePackage.getConfigReloadStrategies(), built.getConfigReloadStrategies());
        assertEquals(configSourcePackage.getTags(), built.getTags());
    }

    @Test
    @SuppressWarnings("removal")
    public void testBuilderConfigSourceTagsOnBoth() throws GestaltException, IOException {
        var source = new StringConfigSource("abc=def", "properties", Tags.environment("dev"));
        ConfigSourcePackage configSourcePackage = new ConfigSourcePackage(source, List.of(), Tags.profile("test"));

        var built = sourceBuilder.setSource("abc=def")
            .setTags(Tags.environment("dev"))
            .addTags(Tags.profile("test"))
            .build();
        assertEquals(new String(configSourcePackage.getConfigSource().loadStream().readAllBytes(), Charset.defaultCharset()),
            new String(built.getConfigSource().loadStream().readAllBytes(), Charset.defaultCharset()));
        assertEquals(configSourcePackage.getConfigReloadStrategies(), built.getConfigReloadStrategies());
        assertEquals(configSourcePackage.getTags(), built.getTags());
    }

    @Test
    public void testConfigSourcePackageCreation() throws GestaltException {
        var source = new StringConfigSource("abc=def", "properties");
        ConfigSourcePackage configSourcePackage = new ConfigSourcePackage(source, List.of(), Tags.of());
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
    public void addTagShouldNotModify() {
        Tag tag = Tag.of("env", "tag1");
        sourceBuilder.addTag(tag);
        Tags tags = sourceBuilder.getTags();
        assertNotNull(tags);
        assertTrue(tags.getTags().contains(tag));
        assertEquals(0, Tags.of().getTags().size());
    }

    @Test
    public void addTagShouldAddTags() throws GestaltException {
        Tags tag = Tags.of("env", "tag1");
        sourceBuilder.addTags(tag);
        Tags tags = sourceBuilder.getTags();
        assertNotNull(tags);
        assertTrue(tags.getTags().containsAll(tag.getTags()));
    }

    @Test
    public void addTagsShouldNotModify() throws GestaltException {
        Tags tag = Tags.of("env", "tag1");
        sourceBuilder.addTags(tag);
        Tags tags = sourceBuilder.getTags();
        assertNotNull(tags);
        assertTrue(tags.getTags().containsAll(tag.getTags()));

        assertEquals(0, Tags.of().getTags().size());
    }

    @Test
    public void add2TagShouldAddTag() {
        Tag tag = Tag.of("env", "tag1");
        sourceBuilder.addTag(tag);
        Tags tags = sourceBuilder.getTags();
        assertNotNull(tags);
        assertTrue(tags.getTags().contains(tag));

        var sourceBuilder2 = new TestSourceBuilder();
        Tag tag2 = Tag.of("env", "tag2");
        sourceBuilder2.addTag(tag2);
        Tags tags2 = sourceBuilder2.getTags();
        assertNotNull(tags2);
        assertTrue(tags2.getTags().contains(tag2));
    }

    @Test
    public void add2TagShouldAddTags() throws GestaltException {
        Tags tag = Tags.of("env", "tag1");
        sourceBuilder.addTags(tag);
        Tags tags = sourceBuilder.getTags();
        assertNotNull(tags);
        assertTrue(tags.getTags().containsAll(tags.getTags()));

        var sourceBuilder2 = new TestSourceBuilder();
        Tags tag2 = Tags.of("env", "tag2");
        sourceBuilder2.addTags(tag2);
        Tags tags2 = sourceBuilder2.getTags();
        assertNotNull(tags2);
        assertTrue(tags2.getTags().containsAll(tag2.getTags()));
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

    private static final class TestSourceBuilder extends SourceBuilder<TestSourceBuilder, StringConfigSource> {

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
            return buildPackage(new StringConfigSource(text, "properties"));
        }
    }
}


