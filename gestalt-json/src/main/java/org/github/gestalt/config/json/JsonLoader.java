package org.github.gestalt.config.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.ValidateOf;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Loads from a json files from multiple sources, such as a file.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public class JsonLoader implements ConfigLoader {

    private final ObjectMapper objectMapper;

    /**
     * Constructor for JsonLoader that accepts a ObjectMapper.
     *
     * @param objectMapper for loading json config
     */
    public JsonLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Default constructor for JsonLoader that creates a new ObjectMapper.
     */
    public JsonLoader() {
        objectMapper = new ObjectMapper();
    }

    @Override
    public String name() {
        return "JsonLoader";
    }

    @Override
    public boolean accepts(String format) {
        return "json".equals(format);
    }

    /**
     * Loads the source with a stream into a java Properties class.
     * Then convert them to a list of pairs with the path and value.
     * Pass these into the ConfigCompiler to build a config node tree.
     *
     * @param source source we want to load with this config loader.
     * @return ValidateOf config node or errors.
     * @throws GestaltException any errors.
     */
    @Override
    public ValidateOf<List<ConfigNodeContainer>> loadSource(ConfigSource source) throws GestaltException {

        if (source.hasStream()) {
            try (InputStream is = source.loadStream()) {
                JsonNode jsonNode = objectMapper.readTree(is);
                if (jsonNode == null || jsonNode.isNull()) {
                    throw new GestaltException("Exception loading source: " + source.name() + " no yaml found");
                }

                ValidateOf<ConfigNode> node = buildConfigTree("", jsonNode);

                if (node.hasResults()) {
                    return ValidateOf.validateOf(
                        List.of(new ConfigNodeContainer(node.results(), source)), node.getErrors());
                } else {
                    return ValidateOf.inValid(node.getErrors());
                }
            } catch (IOException | NullPointerException e) {
                throw new GestaltException("Exception loading source: " + source.name(), e);
            }
        } else {
            throw new GestaltException("Config source: " + source.name() + " does not have a stream to load.");
        }
    }

    private ValidateOf<ConfigNode> buildConfigTree(String path, JsonNode jsonNode) {
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
                return ValidateOf.valid(new LeafNode(jsonNode.asText()));

            case MISSING:
            case NULL:
                return ValidateOf.inValid(new ValidationError.NoResultsFoundForPath(path));
            default:
                return ValidateOf.inValid(new ValidationError.UnknownNodeTypeDuringLoad(path, jsonNode.getNodeType().name()));
        }
    }

    private String normalizeSentence(String sentence) {
        return sentence.toLowerCase();
    }

    private ValidateOf<ConfigNode> buildArrayConfigTree(String path, JsonNode jsonNode) {
        List<ValidationError> errors = new ArrayList<>();
        List<ConfigNode> array = new ArrayList<>();
        int arraySize = jsonNode.size();
        for (int i = 0; i < arraySize; i++) {
            String currentPath = PathUtil.pathForIndex(path, i);

            JsonNode arrayNodes = jsonNode.get(i);
            ValidateOf<ConfigNode> node = buildConfigTree(currentPath, arrayNodes);
            errors.addAll(node.getErrors());
            if (!node.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForPath(currentPath));
            } else {
                array.add(node.results());
            }
        }
        ConfigNode arrayNode = new ArrayNode(array);
        return ValidateOf.validateOf(arrayNode, errors);
    }

    private ValidateOf<ConfigNode> buildObjectConfigTree(String path, JsonNode jsonNode) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, ConfigNode> mapNode = new HashMap<>();

        for (Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            String key = normalizeSentence(entry.getKey());
            JsonNode jsonValue = entry.getValue();

            String currentPath = PathUtil.pathForKey(path, key);

            ValidateOf<ConfigNode> node = buildConfigTree(currentPath, jsonValue);
            errors.addAll(node.getErrors());
            if (!node.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForPath(currentPath));
            } else {
                mapNode.put(key, node.results());
            }
        }

        ConfigNode mapConfigNode = new MapNode(mapNode);
        return ValidateOf.validateOf(mapConfigNode, errors);
    }
}
