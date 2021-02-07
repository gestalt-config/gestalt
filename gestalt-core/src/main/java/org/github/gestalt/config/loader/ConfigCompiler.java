package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.exceptions.ConfigurationException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.Pair;
import org.github.gestalt.config.utils.ValidateOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Static utility functions to analyze and build a config node.
 *
 * @author Colin Redmond
 */
public final class ConfigCompiler {

    private static final Logger logger = LoggerFactory.getLogger(ConfigCompiler.class.getName());

    private ConfigCompiler() {
    }

    /**
     * Uses the SentenceLexer to tokenize the configs.
     * Then validates the tokens for any errors.
     * If everything is ok it will send the tokens to the parser.
     *
     * @param lexer      the SentenceLexer used to tokenize the configs.
     * @param parser     ConfigParser to parse the tokens into a config node.
     * @param sourceName name of the source.
     * @param configs    the configuration to parse.
     * @return the ValidateOf of the config node with the results or errors.
     * @throws ConfigurationException on any exceptions.
     */
    public static ValidateOf<ConfigNode> analyze(SentenceLexer lexer,
                                                 ConfigParser parser,
                                                 String sourceName,
                                                 List<Pair<String, String>> configs) throws ConfigurationException {

        return analyze(false, lexer, parser, sourceName, configs);
    }

    /**
     * Uses the SentenceLexer to tokenize the configs.
     * Then validates the tokens for any errors.
     * If everything is ok it will send the tokens to the parser.
     *
     * @param treatErrorsAsWarnings if we want to treat errors as warnings.
     * @param lexer                 the SentenceLexer used to tokenize the configs.
     * @param parser                ConfigParser to parse the tokens into a config node.
     * @param sourceName            name of the source.
     * @param configs               the configuration to parse.
     * @return the ValidateOf of the config node with the results or errors.
     * @throws ConfigurationException on any exceptions.
     */
    public static ValidateOf<ConfigNode> analyze(boolean treatErrorsAsWarnings,
                                                 SentenceLexer lexer,
                                                 ConfigParser parser,
                                                 String sourceName,
                                                 List<Pair<String, String>> configs) throws ConfigurationException {
        List<Pair<ValidateOf<List<Token>>, String>> validatedTokens = configs.stream()
            .map(prop -> new Pair<>(lexer.scan(prop.getFirst()), prop.getSecond()))
            .collect(Collectors.toList());

        Map<ValidationLevel, List<ValidationError>> validationErrors = validatedTokens
            .stream()
            .filter(validatedToken -> validatedToken.getFirst().hasErrors())
            .map(validatedToken -> validatedToken.getFirst().getErrors())
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(ValidationError::level));

        if (! validationErrors.isEmpty()) {
            List<ValidationError> errorMessage = validationErrors.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            if (! treatErrorsAsWarnings && validationErrors.containsKey(ValidationLevel.ERROR)) {
                throw new ConfigurationException("Exception loading config source " + sourceName, errorMessage);
            } else {
                String errors = errorMessage.stream()
                    .map(error ->
                        "level: " + error.level() + ", message: " + error.description()).collect(Collectors.joining("\n - "));
                logger.warn("Warnings loading config source: {} errors: {}", sourceName, errors);
            }
        }

        List<Pair<List<Token>, ConfigValue>> validTokens = validatedTokens
            .stream()
            .filter(validatedToken -> ! validatedToken.getFirst().hasErrors() &&
                validatedToken.getFirst().hasResults() &&
                validatedToken.getFirst().results().size() > 0)
            .map(validatedToken ->
                new Pair<>(validatedToken.getFirst().results(), new ConfigValue(validatedToken.getSecond())))
            .collect(Collectors.toList());

        return parser.parse(validTokens);
    }
}
