package org.github.gestalt.config.node;

import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TagMergingStrategyFallbackTest {

    @Test
    public void testCombine() {
        var tagMergingStrategy = new TagMergingStrategyFallback();

        var combinedTags = tagMergingStrategy.mergeTags(Tags.profile("test"), Tags.profiles("alpha", "beta"));

        Assertions.assertEquals(Tags.profiles("test"), combinedTags);
    }

    @Test
    public void testCombineEmpty() {
        var tagMergingStrategy = new TagMergingStrategyFallback();

        var combinedTags = tagMergingStrategy.mergeTags(Tags.of(), Tags.profiles("alpha", "beta"));

        Assertions.assertEquals(Tags.of(), combinedTags);
    }

    @Test
    public void testCombineNull() {
        var tagMergingStrategy = new TagMergingStrategyFallback();

        var combinedTags = tagMergingStrategy.mergeTags(null, Tags.profiles("alpha", "beta"));

        Assertions.assertEquals(Tags.profiles("alpha", "beta"), combinedTags);
    }
}
