package org.github.gestalt.config.toml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.lexer.PathLexer;
import org.github.gestalt.config.lexer.SentenceLexer;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.utils.PathUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Loads from a yaml files from multiple sources, such as a file.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class TomlLoader implements ConfigLoader {

    private final boolean isDefault;
    private ObjectMapper objectMapper;
    private SentenceLexer lexer;

    /**
     * Default constructor for YamlLoader that creates a new ObjectMapper with a YAMLFactory registered to it.
     */
    public TomlLoader() {
        this(new ObjectMapper(new TomlFactory()).findAndRegisterModules(), new PathLexer(), true);
    }

    /**
     * Constructor for YamlLoader that accepts a ObjectMapper.
     *
     * @param objectMapper for loading yaml config, it should have a YAMLFactory registered to it.
     * @param lexer        the lexer to normalize paths.
     */
    public TomlLoader(ObjectMapper objectMapper, SentenceLexer lexer) {
        this(objectMapper, lexer, false);
    }

    private TomlLoader(ObjectMapper objectMapper, SentenceLexer lexer, boolean isDefault) {
        Objects.requireNonNull(lexer, "TomlLoader SentenceLexer should not be null");
        Objects.requireNonNull(objectMapper, "TomlLoader ObjectMapper should not be null");

        this.objectMapper = objectMapper;
        this.lexer = lexer;
        this.isDefault = isDefault;
    }


    @Override
    public void applyConfig(GestaltConfig config) {
        // for the Toml ConfigLoader we will use the default gestalt sentence lexer.
        var moduleConfig = config.getModuleConfig(TomlModuleConfig.class);
        if (isDefault) {
            if (moduleConfig != null && moduleConfig.getLexer() != null) {
                lexer = moduleConfig.getLexer();
            } else {
                lexer = config.getSentenceLexer();
            }
        }

        if (isDefault && moduleConfig != null && moduleConfig.getObjectMapper() != null) {
            objectMapper = moduleConfig.getObjectMapper();
        }
    }

    @Override
    public String name() {
        return "tomlLoader";
    }

    @Override
    public boolean accepts(String format) {
        return "toml".equals(format);
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

        var source = sourcePackage.getConfigSource();
        if (source.hasStream()) {
            try (InputStream is = source.loadStream()) {
                JsonNode jsonNode = objectMapper.readTree(is);
                if (jsonNode == null || jsonNode.isNull()) {
                    throw new GestaltException("Exception loading source: " + source.name() + " no yaml found");
                }

                GResultOf<ConfigNode> node = buildConfigTree("", jsonNode);

                return node.mapWithError(result -> List.of(new ConfigNodeContainer(result, source, sourcePackage.getTags())));
            } catch (IOException | NullPointerException e) {
                throw new GestaltException("Exception loading source: " + source.name(), e);
            }
        } else {
            throw new GestaltException("Config source: " + source.name() + " does not have a stream to load.");
        }
    }

    private GResultOf<ConfigNode> buildConfigTree(String path, JsonNode jsonNode) {
        switch (jsonNode.getNodeType()) {
            case ARRAY:
                return buildArrayConfigTree(path, jsonNode);

            case OBJECT:
            case POJO:
                return buildObjectConfigTree(path, jsonNode);

            case STRING:
            case BINARY:
            case BOOLEAN:
            case NUMBER:
                return GResultOf.result(new LeafNode(jsonNode.asText()));

            case MISSING:
            case NULL:
                return GResultOf.errors(new ValidationError.NoResultsFoundForPath(path));
            default:
                return GResultOf.errors(new ValidationError.UnknownNodeTypeDuringLoad(path, jsonNode.getNodeType().name()));
        }
    }

    private String normalizeSentence(String sentence) {
        return lexer.normalizeSentence(sentence);
    }

    private GResultOf<ConfigNode> buildArrayConfigTree(String path, JsonNode jsonNode) {
        List<ValidationError> errors = new ArrayList<>();
        List<ConfigNode> array = new ArrayList<>();
        int arraySize = jsonNode.size();
        for (int i = 0; i < arraySize; i++) {
            String currentPath = PathUtil.pathForIndex(path, i);

            JsonNode arrayNodes = jsonNode.get(i);
            GResultOf<ConfigNode> node = buildConfigTree(currentPath, arrayNodes);
            errors.addAll(node.getErrors());
            if (!node.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForPath(currentPath));
            } else {
                array.add(node.results());
            }
        }
        ConfigNode arrayNode = new ArrayNode(array);
        return GResultOf.resultOf(arrayNode, errors);
    }

    private GResultOf<ConfigNode> buildObjectConfigTree(String path, JsonNode jsonNode) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, ConfigNode> mapNode = new HashMap<>();

        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            String key = normalizeSentence(entry.getKey());
            JsonNode jsonValue = entry.getValue();

            String currentPath = PathUtil.pathForKey(path, key);

            GResultOf<ConfigNode> node = buildConfigTree(currentPath, jsonValue);
            errors.addAll(node.getErrors());
            if (!node.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForPath(currentPath));
            } else {
                mapNode.put(key, node.results());
            }
        }

        ConfigNode mapConfigNode = new MapNode(mapNode);
        return GResultOf.resultOf(mapConfigNode, errors);
    }
}
