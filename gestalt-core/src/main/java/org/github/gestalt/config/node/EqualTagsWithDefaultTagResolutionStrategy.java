package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Will Search two config node roots, the one that is an equal match to the tags and the root with no tags.
 * Then return the config node roots to be searched. Only return the roots if they exist.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class EqualTagsWithDefaultTagResolutionStrategy implements ConfigNodeTagResolutionStrategy {

    /**
     * Will Search two config node roots, the one that exactly matches the tags and the root with no tags.
     * Only return the roots if they exist.
     *
     * @param roots roots to search.
     * @param tags the tags we wish to search for.
     * @return list of roots to search for.
     */
    @Override
    @SuppressWarnings("NonApiType")
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
