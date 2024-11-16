package org.github.gestalt.config.processor.config;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.utils.GResultOf;

import java.util.List;

/**
 * Manages the list of ConfigNodeProcessor and applies them to a specific node.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface ConfigNodeProcessorService {
    /**
     * Add a list of ConfigNodeProcessor.
     *
     * @param configNodeProcessorsToAdd a list of ConfigNodeProcessor
     */
    void addConfigNodeProcessors(List<ConfigNodeProcessor> configNodeProcessorsToAdd);

    /**
     * Add a list of RunTimeConfigNodeProcessor.
     *
     * @param runTimeConfigNodeProcessor list of RunTimeConfigNodeProcessor
     */
    void addRuntimeConfigNodeProcessor(List<RunTimeConfigNodeProcessor> runTimeConfigNodeProcessor);

    /**
     * Apply the list of ConfigNodeProcessor to a specific node on a path.
     *
     * @param path the current path
     * @param node the node we want to process
     * @return the result node that is either the same or modified.
     */
    GResultOf<ConfigNode> processConfigNodes(String path, ConfigNode node);

    /**
     * Apply the list of runTimeConfigNodeProcessor to a specific node on a path while getting the configuration.
     *
     * @param path the current path
     * @param node the node we want to process
     * @return the result node that is either the same or modified.
     */
    GResultOf<ConfigNode> runTimeProcessConfigNodes(String path, ConfigNode node);
}
