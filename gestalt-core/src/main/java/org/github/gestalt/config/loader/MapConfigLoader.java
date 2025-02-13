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
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loads an in memory map from MapConfigSource.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class MapConfigLoader implements ConfigLoader {

    private ConfigParser parser;
    private SentenceLexer lexer;
    private final boolean isDefault;

    /**
     * Construct a default Map Config loader using the default path lexer for "." separated paths.
     */
    public MapConfigLoader() {
        this(new PathLexer(), new MapConfigParser(), true);
    }

    /**
     * Construct a Map Config loader providing a lexer and a config parser.
     *
     * @param lexer  SentenceLexer to create tokens for the path.
     * @param parser Parser for the Map Config files
     */
    public MapConfigLoader(SentenceLexer lexer, ConfigParser parser) {
        this(lexer, parser, false);
    }

    private MapConfigLoader(SentenceLexer lexer, ConfigParser parser, boolean isDefault) {
        Objects.requireNonNull(lexer, "MapConfigLoader SentenceLexer should not be null");
        Objects.requireNonNull(parser, "MapConfigLoader ConfigParser should not be null");

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
        var moduleConfig = config.getModuleConfig(MapConfigLoaderModuleConfig.class);
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
        return "MapConfigLoader";
    }

    @Override
    public boolean accepts(String format) {
        return MapConfigSource.MAP_CONFIG.equals(format);
    }

    @Override
    public GResultOf<List<ConfigNodeContainer>> loadSource(ConfigSourcePackage sourcePackage) throws GestaltException {
        List<Pair<String, String>> configs;
        var source = sourcePackage.getConfigSource();
        if (source.hasList()) {
            configs = source.loadList();
        } else {
            throw new GestaltException("Config source: " + source.name() + " does not have a list to load.");
        }

        if (configs.isEmpty()) {
            return GResultOf.result(List.of(new ConfigNodeContainer(new MapNode(Map.of()), source, sourcePackage.getTags())));
        }

        GResultOf<ConfigNode> loadedNode = ConfigCompiler.analyze(source.failOnErrors(), lexer, parser, configs);

        return loadedNode.mapWithError((result) -> List.of(new ConfigNodeContainer(result, source, sourcePackage.getTags())));
    }
}
