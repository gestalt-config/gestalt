package org.github.gestalt.config.decoder;

import org.github.gestalt.config.GestaltCore;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.ConfigNodeService;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.path.mapper.PathMapper;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.token.ArrayToken;
import org.github.gestalt.config.token.Token;
import org.github.gestalt.config.utils.CollectionUtils;
import org.github.gestalt.config.utils.ValidateOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains all decoders and functionality interact and decode a node.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class DecoderRegistry implements DecoderService {
    private static final Logger logger = LoggerFactory.getLogger(GestaltCore.class.getName());

    private final ConfigNodeService configNodeService;
    private final SentenceLexer lexer;
    private List<Decoder<?>> decoders = new ArrayList<>();
    private List<PathMapper> pathMappers;

    /**
     * Constructor to build Decoder Registry.
     *
     * @param decoders list of all supported decoders
     * @param configNodeService config node service that holds the config nodes.
     * @param lexer sentence lexer to decode
     * @param pathMappers path mappers to test
     * @throws GestaltConfigurationException any configuration exceptions for empty parameters.
     */
    public DecoderRegistry(List<Decoder<?>> decoders,
                           ConfigNodeService configNodeService,
                           SentenceLexer lexer,
                           List<PathMapper> pathMappers)
        throws GestaltConfigurationException {
        if (configNodeService == null) {
            throw new GestaltConfigurationException("ConfigNodeService can not be null");
        }
        this.configNodeService = configNodeService;

        if (lexer == null) {
            throw new GestaltConfigurationException("SentenceLexer can not be null");
        }
        this.lexer = lexer;


        if (pathMappers == null || pathMappers.isEmpty()) {
            throw new GestaltConfigurationException("pathMappers can not be null or empty");
        } else {
            this.pathMappers = CollectionUtils.buildOrderedConfigPriorities(pathMappers, false);
        }

        if (decoders == null || decoders.isEmpty()) {
            throw new GestaltConfigurationException("Decoder list was null or empty");
        } else {
            this.decoders.addAll(decoders);
        }
    }

    @Override
    public void addDecoders(List<Decoder<?>> addDecoders) {
        decoders.addAll(addDecoders);
    }

    @Override
    public List<Decoder<?>> getDecoders() {
        return decoders;
    }

    @Override
    public void setDecoders(List<Decoder<?>> decoders) {
        this.decoders = decoders;
    }

    @Override
    public List<PathMapper> getPathMappers() {
        return pathMappers;
    }

    @Override
    public void setPathMappers(List<PathMapper> pathMappers) {
        this.pathMappers = pathMappers;
    }

    /**
     * get a decode for a specific class.
     *
     * @param klass TypeCapture class to search for a decoder
     * @param <T> the generic type of the class
     * @return a list of decoders that match the class
     */
    @SuppressWarnings("rawtypes")
    protected <T> List<Decoder> getDecoderForClass(TypeCapture<T> klass) {
        return decoders
            .stream()
            .filter(decoder -> decoder.matches(klass))
            .collect(Collectors.toList());
    }


    @Override
    public <T> ValidateOf<T> decodeNode(String path, String configNode, TypeCapture<T> klass) {
        return decodeNode(path, new LeafNode(configNode), klass);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> ValidateOf<T> decodeNode(String path, ConfigNode configNode, TypeCapture<T> klass) {
        List<Decoder> classDecoder = getDecoderForClass(klass);
        classDecoder.sort(Comparator.comparingInt(v -> v.priority().ordinal()));
        if (classDecoder.isEmpty()) {
            return ValidateOf.inValid(new ValidationError.NoDecodersFound(klass.getName()));
        } else if (classDecoder.size() > 1) {
            logger.info("Found multiple decoders for {}, found: {}, using {}: ", klass, classDecoder, classDecoder.get(0));
        }

        return classDecoder.get(0).decode(path, configNode, klass, this);
    }

    @Override
    public ValidateOf<ConfigNode> getNextNode(String path, String nextString, ConfigNode configNode) {
        ValidateOf<ConfigNode> result;
        List<ValidationError> errors = new ArrayList<>();
        for (PathMapper pathMapper : pathMappers) {
            ValidateOf<List<Token>> pathValidateOf = pathMapper.map(path, nextString, lexer);

            // if there are errors, add them to the error list abd do not add the merge results
            if (pathValidateOf.hasErrors()) {
                errors.addAll(pathValidateOf.getErrors());
            }

            if (!pathValidateOf.hasResults()) {
                continue;
            }

            List<Token> nextTokens = pathValidateOf.results();
            result = configNodeService.navigateToNextNode(path, nextTokens, configNode);
            // if there are errors, add them to the error list abd do not add the merge results
            if (result.hasErrors()) {
                errors.addAll(result.getErrors());
            }

            if (result.hasResults()) {
                return result;
            }
        }

        //Will only reach here if there is no result.
        if (!errors.isEmpty()) {
            result = ValidateOf.inValid(errors);
        } else {
            result = ValidateOf.inValid(new ValidationError.NoResultsFoundForNode(path, configNode.getNodeType().getType(), "decoding"));
        }
        return result;
    }

    @Override
    public ValidateOf<ConfigNode> getNextNode(String path, int nextIndex, ConfigNode configNode) {
        Token nextToken = new ArrayToken(nextIndex);

        return configNodeService.navigateToNextNode(path, List.of(nextToken), configNode);
    }
}
