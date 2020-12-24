package org.config.gestalt.loader;

import org.config.gestalt.entity.ValidationError;
import org.config.gestalt.exceptions.ConfigurationException;
import org.config.gestalt.lexer.SentenceLexer;
import org.config.gestalt.utils.ValidateOf;
import org.config.gestalt.entity.ConfigValue;
import org.config.gestalt.entity.ValidationLevel;
import org.config.gestalt.node.ConfigNode;
import org.config.gestalt.parser.ConfigParser;
import org.config.gestalt.token.Token;
import org.config.gestalt.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ConfigCompiler {

    private static final Logger logger = LoggerFactory.getLogger(ConfigCompiler.class.getName());

    private ConfigCompiler() {
    }

    public static ValidateOf<ConfigNode> analyze(SentenceLexer lexer,
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

        if (!validationErrors.isEmpty()) {
            List<ValidationError> errorMessage = validationErrors.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            if (validationErrors.containsKey(ValidationLevel.ERROR)) {
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
            .filter(validatedToken -> validatedToken.getFirst().hasResults())
            .map(validatedToken ->
                new Pair<>(validatedToken.getFirst().results(), new ConfigValue(validatedToken.getSecond())))
            .collect(Collectors.toList());

        return parser.parse(validTokens);
    }
}
