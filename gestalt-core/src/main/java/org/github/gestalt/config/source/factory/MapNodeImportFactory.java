package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.utils.GResultOf;

import java.util.Map;

/**
 * Factory for creating a Map Config Source from parameters.
 * This class is not registered with the META-INF services, so it is not loaded by the service loader.
 * If you want to use this class you need to do it manually in the GestaltBuilder
 * Gestalt gestalt = new GestaltBuilder()
 *             .addConfigSourceFactory(new MapNodeImportFactory("mapNode1",
 *                 MapConfigSourceBuilder.builder().setCustomConfig(configs2).build().getConfigSource()))
 *
 * <p>The source must match the name of the source in your import statement.
 *
 * <p>Load a config source from a classpath resource using the getResourceAsStream method.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class MapNodeImportFactory implements ConfigSourceFactory {

    private final String source;
    private final ConfigSource configSource;

    public MapNodeImportFactory(String source, ConfigSource configSource) {
        this.source = source;
        this.configSource = configSource;
    }

    @Override
    public Boolean supportsSource(String sourceName) {
        return sourceName.equals(source);
    }

    @Override
    public GResultOf<ConfigSource> build(Map<String, String> parameters) {
        return GResultOf.result(configSource);
    }
}
