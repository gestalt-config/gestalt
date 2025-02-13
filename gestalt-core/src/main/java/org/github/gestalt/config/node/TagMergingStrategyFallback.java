package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;

/**
 * Accepts the tags provided or fallback to the defaults.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
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
