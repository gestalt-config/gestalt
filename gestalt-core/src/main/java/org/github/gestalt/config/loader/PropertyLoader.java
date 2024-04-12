package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.parser.MapConfigParser;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.SystemPropertiesConfigSource;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Loads from a property files from multiple sources, such as a file.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class PropertyLoader implements ConfigLoader {

    private ConfigParser parser;
    private SentenceLexer lexer;
    private final boolean isDefault;

    /**
     * Construct a default property loader using the default path lexer for "." separated paths.
     */
    public PropertyLoader() {
        this(new PathLexer(), new MapConfigParser(), true);
    }

    /**
     * Construct a property loader providing a lexer and a config parser.
     *
     * @param lexer  SentenceLexer to create tokens for the path.
     * @param parser Parser for the property files
     */
    public PropertyLoader(SentenceLexer lexer, ConfigParser parser) {
        this(lexer, parser, false);
    }

    private PropertyLoader(SentenceLexer lexer, ConfigParser parser, boolean isDefault) {
        Objects.requireNonNull(lexer, "PropertyLoader SentenceLexer should not be null");
        Objects.requireNonNull(parser, "PropertyLoader ConfigParser should not be null");

        this.lexer = lexer;
        this.parser = parser;
        this.isDefault = isDefault;
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        // for the Yaml ConfigLoader we will use the lexer in the following priorities
        // 1. the constructor
        // 2. the module config
        // 3. the Gestalt Configuration
        var moduleConfig = config.getModuleConfig(PropertyLoaderModuleConfig.class);
        if (isDefault) {
            if (moduleConfig != null && moduleConfig.getLexer() != null) {
                lexer = moduleConfig.getLexer();
            } else {
                lexer = config.getSentenceLexer();
            }
        }

        if (isDefault && moduleConfig != null && moduleConfig.getConfigParse() != null) {
            parser = moduleConfig.getConfigParse();
        }
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
     * @param sourcePackage source we want to load with this config loader.
     * @return GResultOf config node or errors.
     * @throws GestaltException any errors.
     */
    @Override
    public GResultOf<List<ConfigNodeContainer>> loadSource(ConfigSourcePackage sourcePackage) throws GestaltException {
        Properties properties = new Properties();
        var source = sourcePackage.getConfigSource();
        if (source.hasStream()) {
            try (InputStream is = source.loadStream()) {
                properties.load(is);
            } catch (IOException | NullPointerException e) {
                throw new GestaltException("Exception loading source: " + source.name(), e);
            }
        } else {
            throw new GestaltException("Config source: " + source.name() + " does not have a stream to load.");
        }

        if (properties.isEmpty()) {
            return GResultOf.result(List.of(new ConfigNodeContainer(new MapNode(Map.of()), source, sourcePackage.getTags())));
        }

        List<Pair<String, String>> configs = properties.entrySet()
            .stream()
            .map(prop -> new Pair<>((String) prop.getKey(), (String) prop.getValue()))
            .collect(Collectors.toList());

        GResultOf<ConfigNode> loadedNode = ConfigCompiler.analyze(source.failOnErrors(), lexer, parser, configs);

        return loadedNode.mapWithError((result) -> List.of(new ConfigNodeContainer(result, source, sourcePackage.getTags())));
    }
}
