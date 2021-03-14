package org.github.gestalt.config.post.process;

import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.utils.ValidateOf;

public interface PostProcessor {
    ValidateOf<ConfigNode> process(String path, ConfigNode currentNode);

    /**
     * Apply the GestaltConfig to the Post Processor. Needed when building via the ServiceLoader
     * It is a default method as most Post Processor don't need to apply configs.
     *
     * @param config GestaltConfig to update the Post Processor
     */
    default void applyConfig(GestaltConfig config) {
    }
}
