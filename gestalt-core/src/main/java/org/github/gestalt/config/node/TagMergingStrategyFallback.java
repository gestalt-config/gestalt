package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;

public class TagMergingStrategyFallback implements TagMergingStrategy {
    @Override
    public Tags mergeTags(Tags provided, Tags defaultTags) {
        if (provided != null) {
            return provided;
        } else {
            return defaultTags;
        }
    }
}
