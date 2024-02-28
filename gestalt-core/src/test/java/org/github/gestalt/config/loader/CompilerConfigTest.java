package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.parser.MapConfigParser;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.source.TestMapConfigSource;
import org.github.gestalt.config.utils.GResultOf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CompilerConfigTest {

    @Test
    void loadSourceWithRealDependencies() throws GestaltException {

        // setup the input data, instead of loading from a file.
        Map<String, String> data = new HashMap<>();
        data.put("test", "value");
        data.put("db.name", "redis");

        // create our class to be tested
        MapConfigLoader mapConfigLoader = new MapConfigLoader();

        // run the code under test.
        GResultOf<List<ConfigNodeContainer>> results = mapConfigLoader.loadSource(new MapConfigSource(data));
        Assertions.assertTrue(results.hasResults());
        Assertions.assertFalse(results.hasErrors());

        Assertions.assertEquals(1, results.results().size());
        Assertions.assertEquals(2, results.results().get(0).getConfigNode().size());

        Assertions.assertEquals("value",
            results.results().get(0).getConfigNode().getKey("test").get().getValue().get());
        Assertions.assertEquals("redis",
            results.results().get(0).getConfigNode().getKey("db").get().getKey("name").get().getValue().get());
    }

    @Test
    void loadSourceWithRealDependenciesErrorTokenizing() throws GestaltException {

        // setup the input data, instead of loading from a file.
        Map<String, String> data = new HashMap<>();
        data.put("test@3", "value");
        data.put("db.name", "redis");

        // create our class to be tested
        MapConfigLoader mapConfigLoader
            = new MapConfigLoader(new PathLexer(".", "^((?<name>\\w+)(?<array>\\[(?<index>\\d*)])?)$"), new MapConfigParser());

        // run the code under test.
        GResultOf<List<ConfigNodeContainer>> results = mapConfigLoader.loadSource(new MapConfigSource(data));

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element test@3 for path: test@3",
            results.getErrors().get(0).description());

    }

    @Test
    void loadSourceWithRealDependenciesErrorTokenizingDontFail() throws GestaltException {

        // setup the input data, instead of loading from a file.
        Map<String, String> data = new HashMap<>();
        data.put("test@3", "value");
        data.put("db.name", "redis");

        // create our class to be tested
        MapConfigLoader mapConfigLoader
            = new MapConfigLoader(new PathLexer(".", "^((?<name>\\w+)(?<array>\\[(?<index>\\d*)])?)$"), new MapConfigParser());

        // run the code under test.
        GResultOf<List<ConfigNodeContainer>> results = mapConfigLoader.loadSource(new MapConfigSourceWarn(data, false));

        Assertions.assertTrue(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element test@3 for path: test@3",
            results.getErrors().get(0).description());

        Assertions.assertEquals(1, results.results().size());
        Assertions.assertFalse(results.results().get(0).getConfigNode().getKey("test").isPresent());
        Assertions.assertEquals("redis", results.results().get(0).getConfigNode().getKey("db").get().getKey("name")
            .get().getValue().get());

    }

    @Test
    void loadSourceWithRealDependenciesErrorParsing() throws GestaltException {

        // setup the input data, instead of loading from a file.
        Map<String, String> data = new HashMap<>();
        data.put("test", "value");
        data.put("db", "enabled");
        data.put("db.name", "redis");

        // create our class to be tested
        MapConfigLoader mapConfigLoader = new MapConfigLoader();

        // run the code under test.
        GResultOf<List<ConfigNodeContainer>> results = mapConfigLoader.loadSource(new MapConfigSource(data));

        Assertions.assertFalse(results.hasResults());
        Assertions.assertTrue(results.hasErrors());

        Assertions.assertEquals(1, results.getErrors().size());
        Assertions.assertEquals("Parsing path length errors for path: db, there could be several causes such as: duplicate paths, " +
            "or a node is both a leaf and an object", results.getErrors().get(0).description());
    }

    private static class MapConfigSourceWarn extends TestMapConfigSource {

        private final boolean failOnErrors;

        /**
         * takes a map of configs.
         *
         * @param customConfig map of configs.
         */
        public MapConfigSourceWarn(Map<String, String> customConfig, boolean failOnErrors) {
            super(customConfig);
            this.failOnErrors = failOnErrors;
        }

        @Override
        public boolean failOnErrors() {
            return failOnErrors;
        }
    }
}
