package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;

/**
 * Merges the tags provided with the request and the default tags, then returns the results.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public class TagMergingStrategyCombine implements TagMergingStrategy {
    @Override
    public Tags mergeTags(Tags provided, Tags defaultTags) {
        if (provided != null && !provided.equals(Tags.of())) {
            return provided.and(defaultTags);
        } else {
            return defaultTags;
        }
    }
}
