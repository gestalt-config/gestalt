package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.parser.MapConfigParser;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.EnvironmentConfigSource;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.List;

/**
 * Loads Environment Variables from EnvironmentConfigSource.
 *
 * @author Colin Redmond
 */
public class EnvironmentVarsLoader implements ConfigLoader {

    private final ConfigParser parser;
    private final SentenceLexer lexer;
    private boolean treatErrorsAsWarnings;

    /**
     * Construct a default Environment Variables Loader using the default path lexer for "_" separated paths.
     * treatErrorsAsWarnings is false as well.
     */
    public EnvironmentVarsLoader() {
        this(false, new PathLexer("_"), new MapConfigParser(false));
    }

    /**
     * Construct a default Environment Variables Loader using the default path lexer for "_" separated paths.
     *
     * @param treatErrorsAsWarnings if we should treat warnings as errors.
     */
    public EnvironmentVarsLoader(boolean treatErrorsAsWarnings) {
        this(treatErrorsAsWarnings, new PathLexer("_"), new MapConfigParser(treatErrorsAsWarnings));
    }

    /**
     * Constructor for Environment Variables Loader.
     *
     * @param treatErrorsAsWarnings if we should treat warnings as errors.
     * @param lexer how to create the tokens for the variables.
     * @param parser parser for Environment Variables
     */
    public EnvironmentVarsLoader(boolean treatErrorsAsWarnings, SentenceLexer lexer, ConfigParser parser) {
        this.lexer = lexer;
        this.parser = parser;
        this.treatErrorsAsWarnings = treatErrorsAsWarnings;
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
    public void applyConfig(GestaltConfig config) {
        treatErrorsAsWarnings = config.isEnvVarsTreatErrorsAsWarnings();
        if (parser instanceof MapConfigParser) {
            ((MapConfigParser) parser).setTreatErrorsAsWarnings(treatErrorsAsWarnings);
        }
    }

    @Override
    public ValidateOf<ConfigNode> loadSource(ConfigSource source) throws GestaltException {
        List<Pair<String, String>> configs;
        if (source.hasList()) {
            configs = source.loadList();
        } else {
            throw new GestaltException("Config source: " + source.name() + " does not have a list to load.");
        }

        ValidateOf<ConfigNode> results = ConfigCompiler.analyze(treatErrorsAsWarnings, lexer, parser, source.name(), configs);

        if (treatErrorsAsWarnings && results.hasErrors()) {
            results.getErrors().forEach(error -> error.setLevel(ValidationLevel.WARN));
        }

        return results;
    }
}
