package org.github.gestalt.config.post.process;

import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.utils.GResultOf;

/**
 * Interface for the Post Processing of Config nodes. This will be run against every node in the tree.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface PostProcessor {

    /**
     * run the post process the current node. You need to return a node, so if your post processor does nothing to the node
     * return the original node.
     *
     * @param path        current path
     * @param currentNode current node to post process.
     * @return the node after running though the post processor.
     */
    GResultOf<ConfigNode> process(String path, ConfigNode currentNode);

    /**
     * Apply the PostProcessorConfig to the Post Processor. Needed when building via the ServiceLoader
     * It is a default method as most Post Processor don't need to apply configs.
     *
     * @param config GestaltConfig to update the Post Processor
     */
    default void applyConfig(PostProcessorConfig config) {
    }
}
