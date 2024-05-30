package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;

public class TagMergingStrategyCombine implements TagMergingStrategy {
    @Override
    public Tags mergeTags(Tags provided, Tags defaultTags) {
        if (provided != null && provided != Tags.of()) {
            return provided.and(defaultTags);
        } else {
            return defaultTags;
        }
    }
}
