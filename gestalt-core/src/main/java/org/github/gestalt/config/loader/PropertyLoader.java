package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.parser.MapConfigParser;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.SystemPropertiesConfigSource;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Loads from a property files from multiple sources, such as a file.
 *
 * @author Colin Redmond
 */
public class PropertyLoader implements ConfigLoader {

    private final ConfigParser parser;
    private final SentenceLexer lexer;


    /**
     * Construct a default property loader using the default path lexer for "." separated paths.
     */
    public PropertyLoader() {
        this(new PathLexer("."), new MapConfigParser());
    }

    /**
     * Construct a property loader providing a lexer and a config parser.
     *
     * @param lexer SentenceLexer to create tokens for the path.
     * @param parser Parser for the property files
     */
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
        return "properties".equals(format) || "props".equals(format) || SystemPropertiesConfigSource.SYSTEM_PROPERTIES.equals(format);
    }

    /**
     * Loads the source with a stream into a java Properties class.
     * Then convert them to a list of pairs with the path and value.
     * Pass these into the ConfigCompiler to build a config node tree.
     *
     * @param source source we want to load with this config loader.
     * @return ValidateOf config node or errors.
     * @throws GestaltException any errors.
     */
    @Override
    public ValidateOf<List<ConfigNodeContainer>> loadSource(ConfigSource source) throws GestaltException {
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

        ValidateOf<ConfigNode> loadedNode = ConfigCompiler.analyze(source.failOnErrors(), lexer, parser, source.name(), configs);

        List<ValidationError> errors = new ArrayList<>();
        if (loadedNode.hasErrors()) {
            errors.addAll(loadedNode.getErrors());
        }
        if (!loadedNode.hasResults()) {
            return ValidateOf.inValid(errors);
        }

        return ValidateOf.validateOf(List.of(new ConfigNodeContainer(loadedNode.results(), source)), errors);
    }
}
