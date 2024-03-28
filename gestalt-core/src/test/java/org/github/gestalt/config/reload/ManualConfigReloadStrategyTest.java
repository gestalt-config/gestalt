package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.StringConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


class ManualConfigReloadStrategyTest {

    @Test
    public void testManualStrategy() throws GestaltException {
        AtomicInteger reloadCount = new AtomicInteger(0);
        StringConfigSource source = new StringConfigSource("abc=def", "properties");
        ManualConfigReloadStrategy reloadStrategy = new ManualConfigReloadStrategy();

        reloadStrategy.setSource(new ConfigSourcePackage(source, List.of(reloadStrategy), Tags.of()));

        ConfigReloadListener reloadListener = (it) -> reloadCount.getAndAdd(1);

        reloadStrategy.registerListener(reloadListener);

        reloadStrategy.reload();

        Assertions.assertEquals(1, reloadCount.get());
    }

    @Test
    public void testManualStrategyWithConstructor() throws GestaltException {
        AtomicInteger reloadCount = new AtomicInteger(0);
        StringConfigSource source = new StringConfigSource("abc=def", "properties");
        ManualConfigReloadStrategy reloadStrategy = new ManualConfigReloadStrategy(new ConfigSourcePackage(source, List.of(), Tags.of()));

        ConfigReloadListener reloadListener = (it) -> reloadCount.getAndAdd(1);

        reloadStrategy.registerListener(reloadListener);

        reloadStrategy.reload();

        Assertions.assertEquals(1, reloadCount.get());
    }

    @Test
    public void testManualStrategyWiNoSource() throws GestaltException {
        AtomicInteger reloadCount = new AtomicInteger(0);
        ManualConfigReloadStrategy reloadStrategy = new ManualConfigReloadStrategy(null);

        ConfigReloadListener reloadListener = (it) -> reloadCount.getAndAdd(1);

        reloadStrategy.registerListener(reloadListener);

        reloadStrategy.reload();

        Assertions.assertEquals(0, reloadCount.get());
    }
}
