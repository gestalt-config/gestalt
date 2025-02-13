package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigValue;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.parser.ConfigParser;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Static utility functions to analyze and build a config node.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ConfigCompiler {
    private ConfigCompiler() {
    }

    /**
     * Uses the SentenceLexer to tokenize the configs.
     * Then validates the tokens for any errors.
     * If everything is ok it will send the tokens to the parser.
     *
     * @param failOnErrors if we want to fail on errors or try and recover. Results can be unpredictable if it continues
     * @param lexer        the SentenceLexer used to tokenize the configs.
     * @param parser       ConfigParser to parse the tokens into a config node.
     * @param configs      the configuration to parse.
     * @return the GResultOf of the config node with the results or errors.
     */
    public static GResultOf<ConfigNode> analyze(boolean failOnErrors,
                                                SentenceLexer lexer,
                                                ConfigParser parser,
                                                List<Pair<String, String>> configs) {
        List<ValidationError> errorMessage = new ArrayList<>();

        List<Pair<GResultOf<List<Token>>, String>> validatedTokens =
            configs.stream()
                .map(prop -> new Pair<>(lexer.scan(prop.getFirst()), prop.getSecond()))
                .collect(Collectors.toList());

        Map<ValidationLevel, List<ValidationError>> validationErrors = validatedTokens
            .stream()
            .filter(validatedToken -> validatedToken.getFirst().hasErrors())
            .map(validatedToken -> validatedToken.getFirst().getErrors())
            .flatMap(Collection::stream)
            .collect(Collectors.groupingBy(ValidationError::level));

        if (!validationErrors.isEmpty()) {
            errorMessage = validationErrors.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            if (failOnErrors && validationErrors.containsKey(ValidationLevel.ERROR)) {
                return GResultOf.errors(errorMessage);
            }
        }

        List<Pair<List<Token>, ConfigValue>> validTokens = validatedTokens
            .stream()
            .filter(validatedToken -> !validatedToken.getFirst().hasErrors() &&
                validatedToken.getFirst().hasResults() &&
                !validatedToken.getFirst().results().isEmpty())
            .map(validatedToken ->
                new Pair<>(validatedToken.getFirst().results(), new ConfigValue(validatedToken.getSecond())))
            .collect(Collectors.toList());

        GResultOf<ConfigNode> parserResults = parser.parse(lexer, validTokens, failOnErrors);
        errorMessage.addAll(parserResults.getErrors());
        return GResultOf.resultOf(parserResults.results(), errorMessage);
    }
}
