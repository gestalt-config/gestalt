package org.github.gestalt.config.source;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.SystemWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class EnvironmentConfigSourceTest {

    @Test
    void testDefaultTransformsLoad() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();

        Assertions.assertTrue(envConfig.hasList());
        Assertions.assertNotNull(envConfig.loadList());
        Assertions.assertFalse(envConfig.loadList().isEmpty());
    }

    @Test
    void format() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
        Assertions.assertEquals("envVars", envConfig.format());
    }

    @Test
    void name() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
        Assertions.assertEquals("envVars", envConfig.name());
    }

    @Test
    void idNotEquals() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
        EnvironmentConfigSource envConfig2 = new EnvironmentConfigSource();
        Assertions.assertNotEquals(envConfig2.id(), envConfig.id());
    }

    @Test
    void unsupportedStream() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();

        Assertions.assertFalse(envConfig.hasStream());
        Assertions.assertThrows(GestaltException.class, envConfig::loadStream);
    }

    @Test
    void equals() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
        EnvironmentConfigSource envConfig2 = new EnvironmentConfigSource();
        Assertions.assertEquals(envConfig, envConfig);
        Assertions.assertNotEquals(envConfig, envConfig2);
        Assertions.assertNotEquals(envConfig, null);
        Assertions.assertNotEquals(envConfig, 1L);
    }

    @Test
    void hash() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
        Assertions.assertTrue(envConfig.hashCode() != 0);
    }

    @Test
    void testIgnorePrefix() {
        try (MockedStatic<SystemWrapper> mocked = mockStatic(SystemWrapper.class)) {
            Map<String, String> envVars = Map.of("key1", "value1", "key2", "value2",
                "prefix_key3", "value3", "prefix_key4", "value4", "prefix.key5", "value5");
            mocked.when(SystemWrapper::getEnvVars).thenReturn(envVars);

            EnvironmentConfigSource envConfig = new EnvironmentConfigSource();
            var results = envConfig.loadList();

            var resultKeys = results.stream().map(Pair::getFirst).collect(Collectors.toList());
            var resultValues = results.stream().map(Pair::getSecond).collect(Collectors.toList());

            assertThat(resultKeys).contains("key1", "key2", "prefix_key3", "prefix_key4", "prefix.key5");
            assertThat(resultValues).contains("value1", "value2", "value3", "value4", "value5");
        }
    }

    @Test
    void testHasPrefixCaseSensitive() {
        try (MockedStatic<SystemWrapper> mocked = mockStatic(SystemWrapper.class)) {
            Map<String, String> envVars = Map.of("key1", "value1", "key2", "value2",
                "prefix_key3", "value3", "prefix_key4", "value4", "prefix.key5", "value5", "PREFIX.key6", "value6");
            mocked.when(SystemWrapper::getEnvVars).thenReturn(envVars);

            EnvironmentConfigSource envConfig = new EnvironmentConfigSource("prefix", false, true);
            var results = envConfig.loadList();

            var resultKeys = results.stream().map(Pair::getFirst).collect(Collectors.toList());
            var resultValues = results.stream().map(Pair::getSecond).collect(Collectors.toList());

            assertThat(resultKeys).contains("key3", "key4", "key5");
            assertThat(resultValues).contains("value3", "value4", "value5");
        }
    }

    @Test
    void testHasPrefixCaseInSensitive() {
        try (MockedStatic<SystemWrapper> mocked = mockStatic(SystemWrapper.class)) {
            Map<String, String> envVars = Map.of("key1", "value1", "key2", "value2",
                "prefix_key3", "value3", "prefix_key4", "value4", "prefix.key5", "value5", "PREFIX.key6", "value6");
            mocked.when(SystemWrapper::getEnvVars).thenReturn(envVars);

            EnvironmentConfigSource envConfig = new EnvironmentConfigSource("prefix", true, true);
            var results = envConfig.loadList();

            var resultKeys = results.stream().map(Pair::getFirst).collect(Collectors.toList());
            var resultValues = results.stream().map(Pair::getSecond).collect(Collectors.toList());

            assertThat(resultKeys).contains("key3", "key4", "key5", "key6");
            assertThat(resultValues).contains("value3", "value4", "value5", "value6");
        }
    }

    @Test
    void testHasPrefixKeepPrefix() {
        try (MockedStatic<SystemWrapper> mocked = mockStatic(SystemWrapper.class)) {
            Map<String, String> envVars = Map.of("key1", "value1", "key2", "value2",
                "prefix_key3", "value3", "prefix_key4", "value4");
            mocked.when(SystemWrapper::getEnvVars).thenReturn(envVars);

            EnvironmentConfigSource envConfig = new EnvironmentConfigSource("prefix", false);
            var results = envConfig.loadList();

            var resultKeys = results.stream().map(Pair::getFirst).collect(Collectors.toList());
            var resultValues = results.stream().map(Pair::getSecond).collect(Collectors.toList());

            assertThat(resultKeys).contains("prefix_key3", "prefix_key4");
            assertThat(resultValues).contains("value3", "value4");
        }
    }

    @Test
    void testHasPrefixRemovePrefix() {
        try (MockedStatic<SystemWrapper> mocked = mockStatic(SystemWrapper.class)) {
            Map<String, String> envVars = Map.of("key1", "value1", "key2", "value2",
                "prefix.key3", "value3", "prefix.key4", "value4", "PREFIX.key5", "value5");
            mocked.when(SystemWrapper::getEnvVars).thenReturn(envVars);

            EnvironmentConfigSource envConfig = new EnvironmentConfigSource("prefix");
            var results = envConfig.loadList();

            var resultKeys = results.stream().map(Pair::getFirst).collect(Collectors.toList());
            var resultValues = results.stream().map(Pair::getSecond).collect(Collectors.toList());

            assertThat(resultKeys).contains("key3", "key4");
            assertThat(resultValues).contains("value3", "value4");
        }
    }

    @Test
    void tags() throws GestaltException {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource("", true, false, false, Tags.of("toy", "ball"));
        Assertions.assertEquals(Tags.of("toy", "ball"), envConfig.getTags());
    }

    @Test
    void failOnErrors() {
        EnvironmentConfigSource envConfig = new EnvironmentConfigSource(true);
        Assertions.assertTrue(envConfig.failOnErrors());
    }
}
