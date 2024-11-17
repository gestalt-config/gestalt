package org.github.gestalt.config.integration;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UseCasesTest {
    @Test
    public void testDist100RunTimeSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        // we want 10% of traffic to be true
        customMap.put("newFeature.enabled", "#{dist100:10:true,false}");

        Map<String, String> customTest1Map = new HashMap<>();
        customTest1Map.put("newFeature.enabled", "true");

        Map<String, String> customTest2Map = new HashMap<>();
        customTest2Map.put("newFeature.enabled", "false");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customTest1Map).setTags(Tags.of("group", "test1")).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customTest2Map).setTags(Tags.of("group", "test2")).build())

            .build();

        gestalt.loadConfigs();

        int enabled = 0;
        int disabled = 0;
        int passes = 100;
        while (passes-- > 0) {
            Boolean featEnabled = gestalt.getConfig("newFeature.enabled", TypeCapture.of(Boolean.class));
            if (featEnabled) {
                enabled++;
            } else {
                disabled++;
            }

            Boolean enabledTest1 = gestalt.getConfig("newFeature.enabled", TypeCapture.of(Boolean.class),Tags.of("group", "test1"));
            Assertions.assertTrue(enabledTest1);

            Boolean enabledTest2 = gestalt.getConfig("newFeature.enabled", TypeCapture.of(Boolean.class),Tags.of("group", "test2"));
            Assertions.assertFalse(enabledTest2);
        }

        // we expect that we should get less than 15 % enabled
        Assertions.assertTrue(enabled < 15);
        // we expect that we should get more than 85 % enabled
        Assertions.assertTrue(disabled > 85);
    }
}
