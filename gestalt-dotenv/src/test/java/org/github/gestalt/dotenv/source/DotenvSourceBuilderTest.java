package org.github.gestalt.dotenv.source;

import io.github.cdimascio.dotenv.Dotenv;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reload.ConfigReloadStrategy;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.tag.Tag;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DotenvSourceBuilderTest {

    @Test
    void buildWithDotenv() throws GestaltException {
        Dotenv dotenv = mock(Dotenv.class);

        DotenvSourceBuilder builder = DotenvSourceBuilder.builder();
        builder.setDotenv(dotenv);

        ConfigSourcePackage pkg = builder.build();

        assertNotNull(pkg);
        assertNotNull(pkg.getConfigSource());
        assertInstanceOf(DotenvConfigSource.class, pkg.getConfigSource());
    }

    @Test
    void buildWithFilterAndFormat() throws GestaltException {
        Dotenv dotenv = mock(Dotenv.class);
        Dotenv.Filter filter = mock(Dotenv.Filter.class);

        DotenvSourceBuilder builder = DotenvSourceBuilder.builder();
        builder.setDotenv(dotenv).setFilter(filter).setFormat("properties");

        ConfigSourcePackage pkg = builder.build();

        assertNotNull(pkg);
        DotenvConfigSource source = (DotenvConfigSource) pkg.getConfigSource();
        assertEquals("properties", source.format());
    }

    @Test
    void tagsAndReloadStrategyAreApplied() throws GestaltException {
        Dotenv dotenv = mock(Dotenv.class);
        DotenvSourceBuilder builder = DotenvSourceBuilder.builder();
        builder.setDotenv(dotenv);

        builder.setTags(Tags.of(Tag.of("k", "v")));
        ConfigReloadStrategy strategy = mock(ConfigReloadStrategy.class);
        builder.addConfigReloadStrategy(strategy);

        ConfigSourcePackage pkg = builder.build();

        assertNotNull(pkg.getConfigSource());
        assertTrue(pkg.getConfigReloadStrategies().contains(strategy));
        assertTrue(pkg.getTags().isSubsetOf(Tags.of(Tag.of("k", "v"))));
    }
}

