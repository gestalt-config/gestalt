package org.github.gestalt.config.source.factory;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service that takes in the Config Source Parameters, extracts the source type, finds the factory for the source and builds it.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class ConfigSourceFactoryManager implements ConfigSourceFactoryService {

    public static final String SOURCE = "source";

    private final List<ConfigSourceFactory> configSourceFactories;

    public ConfigSourceFactoryManager(List<ConfigSourceFactory> configSourceFactories) {
        this.configSourceFactories = new ArrayList<>(configSourceFactories);
    }

    @Override
    public void addConfigSourceFactories(List<ConfigSourceFactory> configSourceFactories) {
        this.configSourceFactories.addAll(configSourceFactories);
    }

    @Override
    public GResultOf<ConfigSource> build(Map<String, String> parameters) {
        var source = parameters.entrySet().stream()
            .filter(entry -> SOURCE.equalsIgnoreCase(entry.getKey()))
            .findFirst();

        if (source.isEmpty()) {
            return GResultOf.errors(new ValidationError.ConfigSourceFactoryNoSource(parameters));
        }

        String sourceName = source.get().getValue();
        Optional<ConfigSourceFactory> factory = configSourceFactories.stream().filter(it -> it.supportsSource(sourceName)).findFirst();

        if (factory.isEmpty()) {
            return GResultOf.errors(new ValidationError.ConfigSourceFactoryNotFound(sourceName));
        }

        // Build a map without the source, to pass into the factory.
        var factoryParameters = parameters.entrySet().stream()
            .filter(entry -> !SOURCE.equalsIgnoreCase(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return factory.get().build(factoryParameters);
    }
}
