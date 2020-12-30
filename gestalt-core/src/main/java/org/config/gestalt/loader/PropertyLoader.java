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

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertyLoader implements ConfigLoader {

    private final ConfigParser parser;
    private final SentenceLexer lexer;

    public PropertyLoader() {
        this(new PathLexer("\\."), new MapConfigParser());
    }

    public PropertyLoader(SentenceLexer lexer, ConfigParser parser) {
        this.lexer = lexer;
        this.parser = parser;
    }

    @Override
    public String name() {
        return "PropertyLoader";
    }

    @Override
    public boolean accepts(String format) {
        return "properties".equals(format) || "props".equals(format);
    }

    @Override
    public ValidateOf<ConfigNode> loadSource(ConfigSource source) throws GestaltException {
        Properties properties = new Properties();
        if (source.hasStream()) {
            try {
                properties.load(source.loadStream());
            } catch (IOException | NullPointerException e) {
                throw new GestaltException("Exception loading source: " + source.name(), e);
            }
        } else {
            throw new GestaltException("Config source: " + source.name() + " does not have a stream to load.");
        }

        List<Pair<String, String>> configs = properties.entrySet()
            .stream()
            .map(prop -> new Pair<>((String) prop.getKey(), (String) prop.getValue()))
            .collect(Collectors.toList());

        return ConfigCompiler.analyze(lexer, parser, source.name(), configs);
    }
}
