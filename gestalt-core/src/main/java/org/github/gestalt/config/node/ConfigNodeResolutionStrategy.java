package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Allows users to override how we select the roots to find nodes to merge based on the tags.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface ConfigNodeResolutionStrategy {

    /**
     * Given the roots and the tags we are searching for, return the config node roots to search.
     * The resolution strategy should also include a fallback to the default Tags.Of() if desired.
     * The order is important, as we will search for the nodes in order and merge them with
     * later nodes merged over top of the previous.
     *
     * @param roots roots to search.
     *              DO NOT MODIFY THE ROOTS.
     *              The roots are protected by a read lock here, not a write lock.
     *              Modifying the roots here would not be thread safe.
     * @param tags the tags to search for.
     * @return list of roots to search.
     */
    List<GResultOf<ConfigNode>> rootsToSearch(LinkedHashMap<Tags, ConfigNode> roots, Tags tags);
}
