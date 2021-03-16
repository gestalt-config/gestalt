package org.github.gestalt.config.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
import java.util.*;

/**
 * Loads from a yaml files from multiple sources, such as a file.
 *
 * @author Colin Redmond
 */
public class YamlLoader implements ConfigLoader {

    private final ObjectMapper objectMapper;

    public YamlLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public YamlLoader() {
        objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
    }

    @Override
    public String name() {
        return "YamlLoader";
    }

    @Override
    public boolean accepts(String format) {
        return "yml".equals(format) || "yaml".equals(format);
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
    public ValidateOf<ConfigNode> loadSource(ConfigSource source) throws GestaltException {

        if (source.hasStream()) {
            try {
                JsonNode jsonNode = objectMapper.readTree(source.loadStream());
                if (jsonNode == null || jsonNode.isNull()) {
                    throw new GestaltException("Exception loading source: " + source.name() + " no yaml found");
                }

                return buildConfigTree("", jsonNode);
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
