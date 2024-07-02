package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Will Search for any roots that are a subset of the tags provided with a fallback of the default root.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class SubsetTagsWithDefaultTagResolutionStrategy implements ConfigNodeTagResolutionStrategy {

    /**
     * Will Search for any roots that are a subset of the tags provided with a fallback of the default root.
     *
     * @param roots roots to search.
     * @param tags  the tags we wish to search for.
     * @return list of roots to search for.
     */
    @Override
    @SuppressWarnings("NonApiType")
    public List<GResultOf<ConfigNode>> rootsToSearch(LinkedHashMap<Tags, ConfigNode> roots, Tags tags) {
        List<GResultOf<ConfigNode>> rootsToSearch = new ArrayList<>();

        for (var entry : roots.entrySet()) {
            if (Tags.of().equals(entry.getKey()) || entry.getKey().isSubsetOf(tags)) {
                rootsToSearch.add(GResultOf.result(entry.getValue()));
            }
        }

        return rootsToSearch;
    }
}
