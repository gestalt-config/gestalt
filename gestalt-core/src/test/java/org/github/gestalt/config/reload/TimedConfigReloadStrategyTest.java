package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.LogManager;

class TimedConfigReloadStrategyTest {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = TimedConfigReloadStrategyTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    @SuppressWarnings("removal")
    public void timedConfigReloadStrategyTest() throws InterruptedException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigSource configSource = new MapConfigSource(configs);
        var sourcePackage = new ConfigSourcePackage(configSource, List.of(), Tags.of());
        ConfigListener listener = new ConfigListener();
        TimedConfigReloadStrategy timedConfigReloadStrategy = new TimedConfigReloadStrategy(sourcePackage, Duration.ofMillis(1));

        timedConfigReloadStrategy.registerListener(listener);
        int count = 0;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(10);
            count = listener.count;
            if (count >= 1) {
                break;
            }
        }

        Assertions.assertTrue(count >= 1);

        int newCount = 0;
        for (int i = 0; i < 5; i++) {
            Thread.sleep(10);
            newCount = listener.count;
            if (newCount > count) {
                break;
            }
        }

        Assertions.assertTrue(newCount >= count);

        timedConfigReloadStrategy.removeListener(listener);
        newCount = listener.count;
        Thread.sleep(10);

        Assertions.assertEquals(newCount, listener.count);

    }

    @Test
    public void timedConfigReloadStrategyTest2() throws InterruptedException, GestaltConfigurationException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigSource configSource = new MapConfigSource(configs);
        ConfigListener listener = new ConfigListener();
        TimedConfigReloadStrategy timedConfigReloadStrategy = new TimedConfigReloadStrategy(Duration.ofMillis(1));
        var configSourcePackage = new ConfigSourcePackage(configSource, List.of(timedConfigReloadStrategy), Tags.of());

        timedConfigReloadStrategy.setSource(configSourcePackage);

        timedConfigReloadStrategy.registerListener(listener);
        int count = 0;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(10);
            count = listener.count;
            if (count >= 1) {
                break;
            }
        }

        Assertions.assertTrue(count >= 1);

        int newCount = 0;
        for (int i = 0; i < 5; i++) {
            Thread.sleep(10);
            newCount = listener.count;
            if (newCount > count) {
                break;
            }
        }

        Assertions.assertTrue(newCount >= count);

        timedConfigReloadStrategy.removeListener(listener);
        newCount = listener.count;
        Thread.sleep(10);

        Assertions.assertEquals(newCount, listener.count);

    }

    @Test
    public void timedConfigReloadStrategyTestException() throws InterruptedException, GestaltConfigurationException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigSource configSource = new MapConfigSource(configs);
        ExceptionConfigListener listener = new ExceptionConfigListener();
        TimedConfigReloadStrategy timedConfigReloadStrategy = new TimedConfigReloadStrategy(Duration.ofMillis(1));
        timedConfigReloadStrategy.setSource(
            new ConfigSourcePackage(configSource, List.of(timedConfigReloadStrategy), Tags.of()));

        ConfigListener listener2 = new ConfigListener();
        timedConfigReloadStrategy.registerListener(listener);
        timedConfigReloadStrategy.registerListener(listener2);

        Thread.sleep(10);
    }

    private static class ConfigListener implements ConfigReloadListener {

        public int count = 0;

        @Override
        public void reload(ConfigSourcePackage source) {
            count++;
        }
    }

    private static class ExceptionConfigListener implements ConfigReloadListener {

        @Override
        public void reload(ConfigSourcePackage source) throws GestaltException {
            throw new GestaltException("test exception for time config reload strategy");
        }
    }
}
