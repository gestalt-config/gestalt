package org.github.gestalt.config.hocon;

import com.typesafe.config.*;
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

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loads from a hocon files from multiple sources, such as a file.
 *
 * @author Colin Redmond
 */
public class HoconLoader implements ConfigLoader {

    private ConfigParseOptions configParseOptions = ConfigParseOptions.defaults();

    /**
     * Default constructor for HoconLoader that uses the default ConfigParseOptions.
     */
    public HoconLoader() {

    }

    /**
     * Constructor for HoconLoader that accepts a ConfigParseOptions.
     *
     * @param configParseOptions options for the Hocon parsing
     */
    public HoconLoader(ConfigParseOptions configParseOptions) {
        this.configParseOptions = configParseOptions;
    }


    @Override
    public String name() {
        return "HoconLoader";
    }

    @Override
    public boolean accepts(String format) {
        return "conf".equals(format);
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
                Config config = ConfigFactory.parseReader(
                    new InputStreamReader(source.loadStream(), StandardCharsets.UTF_8), configParseOptions);
                config = config.resolve();
                if (config == null) {
                    throw new GestaltException("Exception loading source: " + source.name() + " no hocon found");
                }

                return buildConfigTree("", config.root());
            } catch (ConfigException | NullPointerException e) {
                throw new GestaltException("Exception loading source: " + source.name(), e);
            }
        } else {
            throw new GestaltException("HOCON Config source: " + source.name() + " does not have a stream to load.");
        }
    }

    private ValidateOf<ConfigNode> buildConfigTree(String path, ConfigValue configObject) {
        switch (configObject.valueType()) {
            case LIST:
                return buildArrayConfigTree(path, (ConfigList) configObject);

            case OBJECT:
                return buildObjectConfigTree(path, (ConfigObject) configObject);

            case STRING:
            case BOOLEAN:
            case NUMBER:
                return ValidateOf.valid(new LeafNode(configObject.unwrapped().toString()));

            case NULL:
                return ValidateOf.inValid(new ValidationError.NoResultsFoundForPath(path));
            default:
                return ValidateOf.inValid(new ValidationError.UnknownNodeTypeDuringLoad(path, configObject.valueType().name()));
        }
    }

    private String normalizeSentence(String sentence) {
        return sentence.toLowerCase();
    }

    private ValidateOf<ConfigNode> buildArrayConfigTree(String path, ConfigList configList) {
        List<ValidationError> errors = new ArrayList<>();
        List<ConfigNode> array = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);
        configList.forEach(it -> {
            String currentPath = PathUtil.pathForIndex(path, index.getAndIncrement());

            ValidateOf<ConfigNode> node = buildConfigTree(currentPath, it);
            errors.addAll(node.getErrors());
            if (!node.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForPath(currentPath));
            } else {
                array.add(node.results());
            }
        });
        ConfigNode arrayNode = new ArrayNode(array);
        return ValidateOf.validateOf(arrayNode, errors);
    }

    private ValidateOf<ConfigNode> buildObjectConfigTree(String path, ConfigObject configObject) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, ConfigNode> mapNode = new HashMap<>();

        configObject.forEach((key, value) -> {
            String newPath = normalizeSentence(key);
            String currentPath = PathUtil.pathForKey(path, key);

            ValidateOf<ConfigNode> node = buildConfigTree(currentPath, value);
            errors.addAll(node.getErrors());
            if (!node.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForPath(currentPath));
            } else {
                mapNode.put(newPath, node.results());
            }
        });

        ConfigNode mapConfigNode = new MapNode(mapNode);
        return ValidateOf.validateOf(mapConfigNode, errors);
    }
}
