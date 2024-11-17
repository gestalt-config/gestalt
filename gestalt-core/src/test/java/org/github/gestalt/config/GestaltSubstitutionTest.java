package org.github.gestalt.config;

import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class GestaltSubstitutionTest {

    @Test
    public void testSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello ${place} it is ${weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello world it is sunny today", message);
    }

    @Test
    public void testRunTimeSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello #{place} it is #{weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello world it is sunny today", message);
    }

    @Test
    public void testRunTimeSubstitutionMultiRun() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "#{random:int}");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        Integer random1 = gestalt.getConfig("message", TypeCapture.of(Integer.class));
        Integer random2 = gestalt.getConfig("message", TypeCapture.of(Integer.class));
        Integer random3 = gestalt.getConfig("message", TypeCapture.of(Integer.class));

        Assertions.assertNotEquals(random1, random2);
        Assertions.assertNotEquals(random1, random3);
    }

    @Test
    public void testNestedSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("variable", "place");
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello ${${variable}} it is ${weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello world it is sunny today", message);
    }

    @Test
    public void testNestedRunTimeSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("variable", "place");
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello #{#{variable}} it is #{weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello world it is sunny today", message);
    }

    @Test
    public void testEscapedSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello \\${place} it is ${weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello ${place} it is sunny today", message);
    }

    @Test
    public void testEscapedRunTimeSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello \\#{place} it is #{weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello #{place} it is sunny today", message);
    }

    @Test
    public void testMixedNestedSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("variable", "place");
        customMap.put("place", "world");
        customMap.put("weather", "sunny");
        customMap.put("message", "hello #{${variable}} it is ${weather} today");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        String message = gestalt.getConfig("message", TypeCapture.of(String.class));

        Assertions.assertEquals("hello world it is sunny today", message);
    }

    @Test
    public void testDist100RunTimeSubstitution() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        customMap.put("message", "#{dist100:50:red,blue}");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        String message1 = gestalt.getConfig("message", TypeCapture.of(String.class));

        boolean different = false;
        int passes = 10;
        while (!different && passes-- > 0) {
            String message2 = gestalt.getConfig("message", TypeCapture.of(String.class));

            different = !message2.equals(message1);
        }
    }

    @Test
    public void testRedistributions() throws GestaltException {
        Map<String, String> customMap = new HashMap<>();
        // we want 10% of traffic to be true
        customMap.put("color", "#{dist100:10:red,30:green,70:blue,75:pink,yellow}");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
                .addSource(MapConfigSourceBuilder.builder().setCustomConfig(customMap).build())
                .build();

        gestalt.loadConfigs();

        int red = 0;
        int green = 0;
        int blue = 0;
        int yellow = 0;
        int pink = 0;
        int passes = 1000;
        while (passes-- > 0) {
            String color = gestalt.getConfig("color", TypeCapture.of(String.class));

            if ("red".equals(color)) {
                red++;
            } else if ("green".equals(color)) {
                green++;
            } else if ("blue".equals(color)) {
                blue++;
            } else if ("pink".equals(color)) {
                pink++;
            } else if ("yellow".equals(color)) {
                yellow++;
            }
        }
        red = red / 10;
        green = green / 10;
        blue = blue / 10;
        yellow = yellow / 10;
        pink = pink / 10;

        Assertions.assertTrue(red > 5 && red < 15);
        Assertions.assertTrue(green > 15 && green < 25);
        Assertions.assertTrue(blue > 35 && blue < 45);
        Assertions.assertTrue(pink > 3 && pink < 7);
        Assertions.assertTrue(yellow > 20 && yellow < 30);
    }
}
