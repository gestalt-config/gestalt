package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.parser.MapConfigParser;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.utils.ValidateOf;
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
        ValidateOf<List<ConfigNodeContainer>> validateOfResults = mapConfigLoader.loadSource(new MapConfigSource(data));
        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertFalse(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.results().size());
        Assertions.assertEquals(2, validateOfResults.results().get(0).getConfigNode().size());

        Assertions.assertEquals("value",
            validateOfResults.results().get(0).getConfigNode().getKey("test").get().getValue().get());
        Assertions.assertEquals("redis",
            validateOfResults.results().get(0).getConfigNode().getKey("db").get().getKey("name").get().getValue().get());
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
        ValidateOf<List<ConfigNodeContainer>> validateOfResults = mapConfigLoader.loadSource(new MapConfigSource(data));

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element test@3 for path: test@3",
            validateOfResults.getErrors().get(0).description());

    }

    private static class MapConfigSourceWarn extends MapConfigSource {

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
        ValidateOf<List<ConfigNodeContainer>> validateOfResults = mapConfigLoader.loadSource(new MapConfigSourceWarn(data, false));

        Assertions.assertTrue(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Unable to tokenize element test@3 for path: test@3",
            validateOfResults.getErrors().get(0).description());

        Assertions.assertEquals(1, validateOfResults.results().size());
        Assertions.assertFalse(validateOfResults.results().get(0).getConfigNode().getKey("test").isPresent());
        Assertions.assertEquals("redis", validateOfResults.results().get(0).getConfigNode().getKey("db").get().getKey("name")
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
        ValidateOf<List<ConfigNodeContainer>> validateOfResults = mapConfigLoader.loadSource(new MapConfigSource(data));

        Assertions.assertFalse(validateOfResults.hasResults());
        Assertions.assertTrue(validateOfResults.hasErrors());

        Assertions.assertEquals(1, validateOfResults.getErrors().size());
        Assertions.assertEquals("Mismatched path lengths received for path: db, this could be because a node is both a leaf " +
            "and an object", validateOfResults.getErrors().get(0).description());
    }
}
