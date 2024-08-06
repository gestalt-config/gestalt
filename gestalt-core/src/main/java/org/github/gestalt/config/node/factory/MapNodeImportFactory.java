package org.github.gestalt.config.node.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.loader.ConfigLoaderService;
import org.github.gestalt.config.loader.ConfigLoaderUtils;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;
import java.util.Map;

/**
 * Factory for creating a Map Config Node from parameters.
 * This class is not registered with the META-INF services, so it is not loaded by the service loader.
 * If you want to use this class you need to do it manually in the GestaltBuilder
 * Gestalt gestalt = new GestaltBuilder()
 * .addConfigSourceFactory(new MapNodeImportFactory("mapNode1",
 * MapConfigSourceBuilder.builder().setCustomConfig(configs2).build().getConfigSource()))
 *
 * <p>The source must match the name of the source in your import statement.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class MapNodeImportFactory implements ConfigNodeFactory {

    private final String source;
    private final Map<String, String> configMap;

    private ConfigLoaderService configLoaderService;

    public MapNodeImportFactory(String source, Map<String, String> configMap) {
        this.source = source;
        this.configMap = configMap;
    }

    @Override
    public void applyConfig(ConfigNodeFactoryConfig config) {
        this.configLoaderService = config.getConfigLoaderService();
    }

    @Override
    public Boolean supportsType(String type) {
        return type.equals(source);
    }

    @Override
    public GResultOf<List<ConfigNode>> build(Map<String, String> parameters) {
        try {
            ConfigSourcePackage mapConfigSource = MapConfigSourceBuilder.builder().setCustomConfig(configMap).build();
            return ConfigLoaderUtils.convertSourceToNodes(mapConfigSource.getConfigSource(), configLoaderService);

        } catch (GestaltException e) {
            return GResultOf.errors(new ValidationError.ConfigNodeImportException(e));
        }
    }
}
