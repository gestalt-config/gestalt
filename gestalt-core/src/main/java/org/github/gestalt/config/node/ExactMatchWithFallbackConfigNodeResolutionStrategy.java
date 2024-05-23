package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Allows users to override how we find nodes to merge based on the tags.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class ExactMatchWithFallbackConfigNodeResolutionStrategy implements ConfigNodeResolutionStrategy {

    /**
     * Given the roots and the tags we are searching for, return the roots to search.
     * The order is important, as we will search for the nodes in order and merge them with
     * later nodes merged over top of the previous.
     *
     * @param roots roots to search.
     * @param tags the tags we wish to search for.
     * @return list of roots to search for.
     */
    public List<GResultOf<ConfigNode>> rootsToSearch(LinkedHashMap<Tags, ConfigNode> roots, Tags tags) {
        List<GResultOf<ConfigNode>> rootsToSearch = new ArrayList<>();

        // if the roots contain the empty tags, add the empty tags as a fallback.
        if (roots.containsKey(Tags.of())) {
            rootsToSearch.add(GResultOf.result(roots.get(Tags.of())));
        }

        // if the tags aren't the empty tags, and if this root contains the tags, add them to the roots to search.
        if (!Tags.of().equals(tags) && roots.containsKey(tags)) {
            rootsToSearch.add(GResultOf.result(roots.get(tags)));
        }


        return rootsToSearch;
    }
}
