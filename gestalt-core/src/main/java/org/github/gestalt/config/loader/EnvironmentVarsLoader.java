package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.parser.MapConfigParser;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.EnvironmentConfigSource;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;

import java.util.List;
import java.util.Map;

/**
 * Loads Environment Variables from EnvironmentConfigSource.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class EnvironmentVarsLoader implements ConfigLoader {

    private final ConfigParser parser;
    private final SentenceLexer lexer;

    /**
     * Construct a default Environment Variables Loader using the default path lexer for "_" separated paths.
     */
    public EnvironmentVarsLoader() {
        this(new PathLexer("_"), new MapConfigParser());
    }


    /**
     * Constructor for Environment Variables Loader.
     *
     * @param lexer  how to create the tokens for the variables.
     * @param parser parser for Environment Variables
     */
    public EnvironmentVarsLoader(SentenceLexer lexer, ConfigParser parser) {
        this.lexer = lexer;
        this.parser = parser;
    }

    @Override
    public String name() {
        return "EnvironmentVarsLoader";
    }

    @Override
    public boolean accepts(String format) {
        return EnvironmentConfigSource.ENV_VARS.equals(format);
    }

    @Override
    public GResultOf<List<ConfigNodeContainer>> loadSource(ConfigSource source) throws GestaltException {
        List<Pair<String, String>> configs;
        if (source.hasList()) {
            configs = source.loadList();
        } else {
            throw new GestaltException("Config source: " + source.name() + " does not have a list to load.");
        }

        if (configs.isEmpty()) {
            return GResultOf.result(List.of(new ConfigNodeContainer(new MapNode(Map.of()), source)));
        }

        GResultOf<ConfigNode> loadedNode = ConfigCompiler.analyze(source.failOnErrors(), lexer, parser, configs);

        return loadedNode.mapWithError((result) -> List.of(new ConfigNodeContainer(result, source)));
    }
}
