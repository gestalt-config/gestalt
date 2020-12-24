package org.config.gestalt.loader;

import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.PathLexer;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.parser.ConfigParser;
import org.config.gestalt.parser.MapConfigParser;
import org.config.gestalt.source.ConfigSource;
import org.config.gestalt.utils.Pair;
import org.config.gestalt.utils.ValidateOf;
import org.config.gestalt.source.EnvironmentConfigSource;

import java.util.List;

public class EnvironmentVarsLoader implements ConfigLoader {

    private final ConfigParser parser;
    private final SentenceLexer lexer;

    public EnvironmentVarsLoader() {
        this(new PathLexer("_"), new MapConfigParser());
    }

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
