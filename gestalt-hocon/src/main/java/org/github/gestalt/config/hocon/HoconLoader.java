package org.github.gestalt.config.hocon;

import com.typesafe.config.*;
import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.loader.ConfigLoader;
import org.github.gestalt.config.node.ArrayNode;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.node.LeafNode;
import org.github.gestalt.config.node.MapNode;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.utils.PathUtil;
import org.github.gestalt.config.utils.GResultOf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loads from a hocon files from multiple sources, such as a file.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class HoconLoader implements ConfigLoader {

    private final ConfigParseOptions configParseOptions;

    /**
     * Default constructor for HoconLoader that uses the default ConfigParseOptions.
     */
    public HoconLoader() {
        configParseOptions = ConfigParseOptions.defaults();
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
     * @param sourcePackage source we want to load with this config loader.
     * @return GResultOf config node or errors.
     * @throws GestaltException any errors.
     */
    @Override
    public GResultOf<List<ConfigNodeContainer>> loadSource(ConfigSourcePackage sourcePackage) throws GestaltException {

        var source = sourcePackage.getConfigSource();
        if (source.hasStream()) {
            try (InputStream is = source.loadStream()) {
                Config config = ConfigFactory.parseReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8), configParseOptions);
                config = config.resolve();
                if (config == null) {
                    throw new GestaltException("Exception loading source: " + source.name() + " no hocon found");
                }

                GResultOf<ConfigNode> node = buildConfigTree("", config.root());
                return node.mapWithError(result -> List.of(new ConfigNodeContainer(result, source, sourcePackage.getTags())));
            } catch (ConfigException | NullPointerException | IOException e) {
                throw new GestaltException("Exception loading source: " + source.name(), e);
            }
        } else {
            throw new GestaltException("HOCON Config source: " + source.name() + " does not have a stream to load.");
        }
    }

    private GResultOf<ConfigNode> buildConfigTree(String path, ConfigValue configObject) {
        switch (configObject.valueType()) {
            case LIST:
                return buildArrayConfigTree(path, (ConfigList) configObject);

            case OBJECT:
                return buildObjectConfigTree(path, (ConfigObject) configObject);

            case STRING:
            case BOOLEAN:
            case NUMBER:
                return GResultOf.result(new LeafNode(configObject.unwrapped().toString()));

            case NULL:
                return GResultOf.errors(new ValidationError.NoResultsFoundForPath(path));
            default:
                return GResultOf.errors(new ValidationError.UnknownNodeTypeDuringLoad(path, configObject.valueType().name()));
        }
    }

    private String normalizeSentence(String sentence) {
        return sentence.toLowerCase(Locale.getDefault());
    }

    private GResultOf<ConfigNode> buildArrayConfigTree(String path, ConfigList configList) {
        List<ValidationError> errors = new ArrayList<>();
        List<ConfigNode> array = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(0);
        configList.forEach(it -> {
            String currentPath = PathUtil.pathForIndex(path, index.getAndIncrement());

            GResultOf<ConfigNode> node = buildConfigTree(currentPath, it);
            errors.addAll(node.getErrors());
            if (!node.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForPath(currentPath));
            } else {
                array.add(node.results());
            }
        });
        ConfigNode arrayNode = new ArrayNode(array);
        return GResultOf.resultOf(arrayNode, errors);
    }

    private GResultOf<ConfigNode> buildObjectConfigTree(String path, ConfigObject configObject) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, ConfigNode> mapNode = new LinkedHashMap<>();

        configObject.forEach((key, value) -> {
            String newPath = normalizeSentence(key);
            String currentPath = PathUtil.pathForKey(path, key);

            GResultOf<ConfigNode> node = buildConfigTree(currentPath, value);
            errors.addAll(node.getErrors());
            if (!node.hasResults()) {
                errors.add(new ValidationError.NoResultsFoundForPath(currentPath));
            } else {
                mapNode.put(newPath, node.results());
            }
        });

        ConfigNode mapConfigNode = new MapNode(mapNode);
        return GResultOf.resultOf(mapConfigNode, errors);
    }
}
