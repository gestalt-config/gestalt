package org.github.gestalt.config.loader;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.parser.MapConfigParser;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.List;

/**
 * Loads an in memory map from MapConfigSource.
 *
 * @author Colin Redmond
 */
public class MapConfigLoader implements ConfigLoader {

    private final ConfigParser parser;
    private final SentenceLexer lexer;

    public MapConfigLoader() {
        this(new PathLexer("\\."), new MapConfigParser());
    }

    public MapConfigLoader(SentenceLexer lexer, ConfigParser parser) {
        this.lexer = lexer;
        this.parser = parser;
    }

    @Override
    public String name() {
        return "MapConfigLoader";
    }

    @Override
    public boolean accepts(String format) {
        return MapConfigSource.MAP_CONFIG.equals(format);
    }

    @Override
    public ValidateOf<ConfigNode> loadSource(ConfigSource source) throws GestaltException {
        List<Pair<String, String>> configs;
        if (source.hasList()) {
            configs = source.loadList();
        } else {
            throw new GestaltException("Config source: " + source.name() + " does not have a list to load.");
        }

        return ConfigCompiler.analyze(lexer, parser, source.name(), configs);
    }
}
