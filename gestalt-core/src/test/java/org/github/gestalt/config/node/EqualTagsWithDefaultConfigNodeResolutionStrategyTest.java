package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

class EqualTagsWithDefaultConfigNodeResolutionStrategyTest {

    @Test
    void rootsToSearch() {

        var resolution = new EqualTagsWithDefaultConfigNodeResolutionStrategy();

        var roots = new LinkedHashMap<Tags, ConfigNode>();
        roots.put(Tags.of(), new LeafNode("default"));
        roots.put(Tags.environment("dev"), new LeafNode("dev"));
        roots.put(Tags.environment("prod"), new LeafNode("prod"));
        var foundNodes = resolution.rootsToSearch(roots, Tags.of());

        Assertions.assertEquals(1, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());

        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        // search for dev
        foundNodes = resolution.rootsToSearch(roots, Tags.environment("dev"));

        Assertions.assertEquals(2, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());

        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        Assertions.assertFalse(foundNodes.get(1).hasErrors());
        Assertions.assertTrue(foundNodes.get(1).hasResults());
        Assertions.assertEquals(new LeafNode("dev"), foundNodes.get(1).results());

        // search for prod
        foundNodes = resolution.rootsToSearch(roots, Tags.environment("prod"));

        Assertions.assertEquals(2, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());

        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        Assertions.assertFalse(foundNodes.get(1).hasErrors());
        Assertions.assertTrue(foundNodes.get(1).hasResults());
        Assertions.assertEquals(new LeafNode("prod"), foundNodes.get(1).results());

        // search for non existant
        foundNodes = resolution.rootsToSearch(roots, Tags.environment("test"));

        Assertions.assertEquals(1, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());

        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());
    }

    @Test
    void rootsToSearchNoDefault() {

        var resolution = new EqualTagsWithDefaultConfigNodeResolutionStrategy();

        var roots = new LinkedHashMap<Tags, ConfigNode>();
        roots.put(Tags.environment("dev"), new LeafNode("dev"));
        roots.put(Tags.environment("prod"), new LeafNode("prod"));
        var foundNodes = resolution.rootsToSearch(roots, Tags.of());

        Assertions.assertEquals(0, foundNodes.size());

        // search for dev
        foundNodes = resolution.rootsToSearch(roots, Tags.environment("dev"));

        Assertions.assertEquals(1, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());

        Assertions.assertEquals(new LeafNode("dev"), foundNodes.get(0).results());


        // search for prod
        foundNodes = resolution.rootsToSearch(roots, Tags.environment("prod"));

        Assertions.assertEquals(1, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());

        Assertions.assertEquals(new LeafNode("prod"), foundNodes.get(0).results());
    }

    @Test
    void rootsToSearchNone() {
        var resolution = new EqualTagsWithDefaultConfigNodeResolutionStrategy();

        var roots = new LinkedHashMap<Tags, ConfigNode>();
        var foundNodes = resolution.rootsToSearch(roots, Tags.of());

        Assertions.assertEquals(0, foundNodes.size());

        // search for dev
        foundNodes = resolution.rootsToSearch(roots, Tags.environment("dev"));

        Assertions.assertEquals(0, foundNodes.size());
    }
}
