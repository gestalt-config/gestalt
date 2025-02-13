package org.github.gestalt.config.node.factory;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;
import java.util.Map;

/**
 * Service that takes in the Config Node Parameters, extracts the node type, finds the factory for the node and builds it.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface ConfigNodeFactoryService {

    /**
     * Add a list of configSourceFactories to the factory service.
     *
     * @param configSourceFactories The list of configSourceFactories
     */
    void addConfigSourceFactories(List<ConfigNodeFactory> configSourceFactories);

    /**
     * Takes in a map of parameters to then use a builder to generate a Config Source.
     * One of the parameters needs to be source. The source is the name of the sources such as source=file.
     * If a source is not provided this will return an error.
     *
     * @param parameters parameters used to define a configSource, such as file location, or url
     * @return Config Source
     */
    GResultOf<List<ConfigNode>> build(Map<String, String> parameters);
}
