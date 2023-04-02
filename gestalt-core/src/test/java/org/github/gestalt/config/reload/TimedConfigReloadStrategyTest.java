package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.MapConfigSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

class TimedConfigReloadStrategyTest {

    @BeforeAll
    public static void beforeAll() {
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }

    @Test
    public void timedConfigReloadStrategyTest() throws InterruptedException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigSource configSource = new MapConfigSource(configs);
        ConfigListener listener = new ConfigListener();
        TimedConfigReloadStrategy timedConfigReloadStrategy = new TimedConfigReloadStrategy(configSource, Duration.ofMillis(1));

        timedConfigReloadStrategy.registerListener(listener);
        int count = 0;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1);
            count = listener.count;
            if (count >= 1) {
                break;
            }
        }

        Assertions.assertTrue(count >= 1);

        int newCount = 0;
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1);
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
    public void timedConfigReloadStrategyTestException() throws InterruptedException {

        Map<String, String> configs = new HashMap<>();
        configs.put("db.name", "test");
        configs.put("db.port", "3306");
        configs.put("admin[0]", "John");
        configs.put("admin[1]", "Steve");

        ConfigSource configSource = new MapConfigSource(configs);
        ExceptionConfigListener listener = new ExceptionConfigListener();
        TimedConfigReloadStrategy timedConfigReloadStrategy = new TimedConfigReloadStrategy(configSource, Duration.ofMillis(1));

        ConfigListener listener2 = new ConfigListener();
        timedConfigReloadStrategy.registerListener(listener);
        timedConfigReloadStrategy.registerListener(listener2);

        Thread.sleep(10);
    }

    private static class ConfigListener implements ConfigReloadListener {

        public int count = 0;

        @Override
        public void reload(ConfigSource source) {
            count++;
        }
    }

    private static class ExceptionConfigListener implements ConfigReloadListener {

        @Override
        public void reload(ConfigSource source) throws GestaltException {
            throw new GestaltException("test exception for time config reload strategy");
        }
    }
}
