package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tag;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

class SubsetTagsWithDefaultConfigNodeResolutionStrategyTest {

    @Test
    void rootsToSearch() {

        var resolution = new SubsetTagsWithDefaultConfigNodeResolutionStrategy();

        var roots = new LinkedHashMap<Tags, ConfigNode>();
        roots.put(Tags.of(), new LeafNode("default"));
        roots.put(Tags.profiles("dev"), new LeafNode("dev"));
        roots.put(Tags.profiles("prod"), new LeafNode("prod"));
        roots.put(Tags.profiles("stage"), new LeafNode("stage"));
        var foundNodes = resolution.rootsToSearch(roots, Tags.of());

        Assertions.assertEquals(1, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());

        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        // search for dev
        foundNodes = resolution.rootsToSearch(roots, Tags.profiles("dev"));

        Assertions.assertEquals(2, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());
        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        Assertions.assertFalse(foundNodes.get(1).hasErrors());
        Assertions.assertTrue(foundNodes.get(1).hasResults());
        Assertions.assertEquals(new LeafNode("dev"), foundNodes.get(1).results());

        // search for dev
        foundNodes = resolution.rootsToSearch(roots, Tags.profiles("test"));

        Assertions.assertEquals(1, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());
        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        // search for dev, prod and test
        foundNodes = resolution.rootsToSearch(roots,
            Tags.of(Tag.of("profile", "dev"), Tag.of("profile", "prod"), Tag.of("profile", "test")));

        Assertions.assertEquals(3, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());
        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        Assertions.assertFalse(foundNodes.get(1).hasErrors());
        Assertions.assertTrue(foundNodes.get(1).hasResults());
        Assertions.assertEquals(new LeafNode("dev"), foundNodes.get(1).results());

        Assertions.assertFalse(foundNodes.get(2).hasErrors());
        Assertions.assertTrue(foundNodes.get(2).hasResults());
        Assertions.assertEquals(new LeafNode("prod"), foundNodes.get(2).results());

        // search for dev, prod and test order shouldnt matter
        foundNodes = resolution.rootsToSearch(roots,
            Tags.of(Tag.of("profile", "prod"), Tag.of("profile", "test"), Tag.of("profile", "dev")));

        Assertions.assertEquals(3, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());
        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        Assertions.assertFalse(foundNodes.get(1).hasErrors());
        Assertions.assertTrue(foundNodes.get(1).hasResults());
        Assertions.assertEquals(new LeafNode("dev"), foundNodes.get(1).results());

        Assertions.assertFalse(foundNodes.get(2).hasErrors());
        Assertions.assertTrue(foundNodes.get(2).hasResults());
        Assertions.assertEquals(new LeafNode("prod"), foundNodes.get(2).results());
    }

    @Test
    void rootsToSearchMultiTag() {

        var resolution = new SubsetTagsWithDefaultConfigNodeResolutionStrategy();

        var roots = new LinkedHashMap<Tags, ConfigNode>();
        roots.put(Tags.of(), new LeafNode("default"));
        roots.put(Tags.of(Tag.environment("dev"), Tag.profile("booking")), new LeafNode("dev-booking"));
        roots.put(Tags.of(Tag.environment("prod"), Tag.profile("booking")), new LeafNode("prod-booking"));
        roots.put(Tags.of(Tag.environment("prod"), Tag.profile("inventory")), new LeafNode("prod-inventory"));
        roots.put(Tags.of(Tag.environment("stage"), Tag.profile("booking")), new LeafNode("stage-booking"));

        // search for default
        var foundNodes = resolution.rootsToSearch(roots, Tags.of());

        Assertions.assertEquals(1, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());

        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        // search for dev
        foundNodes = resolution.rootsToSearch(roots, Tags.environment("dev"));

        Assertions.assertEquals(1, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());

        // search for dev and booking
        foundNodes = resolution.rootsToSearch(roots, Tags.of(Tag.environment("dev"), Tag.profile("booking")));

        Assertions.assertEquals(2, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());
        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        Assertions.assertFalse(foundNodes.get(1).hasErrors());
        Assertions.assertTrue(foundNodes.get(1).hasResults());
        Assertions.assertEquals(new LeafNode("dev-booking"), foundNodes.get(1).results());

        // search for dev, prod and booking
        foundNodes = resolution.rootsToSearch(roots,
            Tags.of(Tag.environment("dev"), Tag.environment("prod"), Tag.profile("booking")));

        Assertions.assertEquals(3, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());
        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());

        Assertions.assertFalse(foundNodes.get(1).hasErrors());
        Assertions.assertTrue(foundNodes.get(1).hasResults());
        Assertions.assertEquals(new LeafNode("dev-booking"), foundNodes.get(1).results());

        Assertions.assertFalse(foundNodes.get(2).hasErrors());
        Assertions.assertTrue(foundNodes.get(2).hasResults());
        Assertions.assertEquals(new LeafNode("prod-booking"), foundNodes.get(2).results());

        // search for dev, prod and booking
        foundNodes = resolution.rootsToSearch(roots,
            Tags.of(Tag.environment("dev"), Tag.environment("prod"), Tag.profile("inventory")));

        Assertions.assertEquals(2, foundNodes.size());
        Assertions.assertFalse(foundNodes.get(0).hasErrors());
        Assertions.assertTrue(foundNodes.get(0).hasResults());
        Assertions.assertEquals(new LeafNode("default"), foundNodes.get(0).results());


        Assertions.assertFalse(foundNodes.get(1).hasErrors());
        Assertions.assertTrue(foundNodes.get(1).hasResults());
        Assertions.assertEquals(new LeafNode("prod-inventory"), foundNodes.get(1).results());
    }
}
