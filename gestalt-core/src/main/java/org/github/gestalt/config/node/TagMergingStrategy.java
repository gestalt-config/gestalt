package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;

/**
 * Merges the tags provided with the request and the default tags, then returns the results.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public interface TagMergingStrategy {

    /**
     * Merges the tags provided with the request and the default tags, then returns the results.
     *
     * @param provided the tags provided with the request, can be null if not provided
     * @param defaultTags the default tags registered with Gestalt
     * @return the merged results
     */
    Tags mergeTags(Tags provided, Tags defaultTags);
}
