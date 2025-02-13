package org.github.gestalt.config.processor.config;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Interface for the Base Config Node Processing. This will be run against every node in the tree after the tree has been compiled.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface BaseConfigNodeProcessor {

    /**
     * run the config node process the current node. You need to return a node, so if your config node processor does nothing to the node
     * return the original node.
     *
     * @param path        current path
     * @param currentNode current node to process.
     * @return the node after running through the processor.
     */
    GResultOf<ConfigNode> process(String path, ConfigNode currentNode);

    /**
     * Apply the ConfigNodeProcessorConfig to the config node Processor. Needed when building via the ServiceLoader
     * It is a default method as most Config Node Processor don't need to apply configs.
     *
     * @param config GestaltConfig to update the Processor
     */
    default void applyConfig(ConfigNodeProcessorConfig config) {
    }
}
