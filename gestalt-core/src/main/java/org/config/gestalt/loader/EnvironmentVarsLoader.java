package org.config.gestalt.loader;

import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.exceptions.GestaltException;
import org.config.gestalt.lexer.PathLexer;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.parser.ConfigParser;
import org.config.gestalt.parser.MapConfigParser;
import org.config.gestalt.source.ConfigSource;
import org.config.gestalt.source.EnvironmentConfigSource;
import org.config.gestalt.utils.Pair;
import org.config.gestalt.utils.ValidateOf;

import java.util.List;

public class EnvironmentVarsLoader implements ConfigLoader {

    private final ConfigParser parser;
    private final SentenceLexer lexer;
    private final boolean treatErrorsAsWarnings;

    public EnvironmentVarsLoader() {
        this(false, new PathLexer("_"), new MapConfigParser(false));
    }

    public EnvironmentVarsLoader(boolean treatErrorsAsWarnings) {
        this(treatErrorsAsWarnings, new PathLexer("_"), new MapConfigParser(treatErrorsAsWarnings));
    }

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
