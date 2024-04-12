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
import org.github.gestalt.config.source.EnvironmentConfigSource;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Loads Environment Variables from EnvironmentConfigSource.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class EnvironmentVarsLoader implements ConfigLoader {

    private final boolean isDefault;
    private ConfigParser parser;
    private SentenceLexer lexer;


    /**
     * Construct a default Environment Variables Loader using the default path lexer for "_" separated paths.
     */
    public EnvironmentVarsLoader() {
        this(new PathLexer("_"), new MapConfigParser(), true);
    }


    /**
     * Constructor for Environment Variables Loader.
     *
     * @param lexer  how to create the tokens for the variables.
     * @param parser parser for Environment Variables
     */
    public EnvironmentVarsLoader(SentenceLexer lexer, ConfigParser parser) {
        this(lexer, parser, false);
    }

    private EnvironmentVarsLoader(SentenceLexer lexer, ConfigParser parser, boolean isDefault) {
        Objects.requireNonNull(lexer, "EnvironmentVarsLoader SentenceLexer should not be null");
        Objects.requireNonNull(parser, "EnvironmentVarsLoader ConfigParser should not be null");

        this.lexer = lexer;
        this.parser = parser;
        this.isDefault = isDefault;
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        // for the Environment Variables ConfigLoader we do not use the default gestalt config lexer,
        // as Environment Variables tend to follow SCREAMING_SNAKE_CASE instead of dot notation.
        // So we use the constructor lexer and parser if set, otherwise the module config.
        var moduleConfig = config.getModuleConfig(EnvironmentVarsLoaderModuleConfig.class);
        if (moduleConfig != null && isDefault) {
            if (moduleConfig.getLexer() != null) {
                lexer = moduleConfig.getLexer();
            }

            if (moduleConfig.getConfigParse() != null) {
                parser = moduleConfig.getConfigParse();
            }
        }
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
