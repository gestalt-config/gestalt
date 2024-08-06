package org.github.gestalt.config;

import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.node.factory.MapNodeImportFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

public class GestaltImportProcessorTest {

    @BeforeAll
    public static void beforeAll() {
        try (InputStream is = GestaltObservationsTest.class.getClassLoader().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            // dont care
        }
    }

    @Test
    public void testImportDefault() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$import", "source=mapNode1");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("b", "b changed");
        configs2.put("c", "c");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode1", configs2))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testImportOver() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$import:1", "source=mapNode1");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("b", "b changed");
        configs2.put("c", "c");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode1", configs2))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testImportMulti() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$import:-1", "source=mapNode1");
        configs.put("$import:1", "source=mapNode2");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("b", "b changed");
        configs2.put("c", "c");

        Map<String, String> configs3 = new HashMap<>();
        configs3.put("c", "c changed");
        configs3.put("d", "d");


        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode1", configs2))
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode2", configs3))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c changed", gestalt.getConfig("c", String.class));
        Assertions.assertEquals("d", gestalt.getConfig("d", String.class));
    }

    @Test
    public void testImportSubPath() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("sub.$import:1", "source = mapNode1");
        configs.put("sub.a", "a");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("b", "b changed");
        configs2.put("c", "c");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode1", configs2))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("a", gestalt.getConfig("sub.a", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("sub.c", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("sub.b", String.class));
    }

    @Test
    public void testImportNested() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$import", "source=mapNode1");

        Map<String, String> configs2 = new HashMap<>();
        configs2.put("b", "b changed");
        configs2.put("c", "c");
        configs2.put("$import:1", "source=mapNode2");

        Map<String, String> configs3 = new HashMap<>();
        configs3.put("c", "c changed");
        configs3.put("d", "d");


        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode1", configs2))
            .addConfigSourceFactory(new MapNodeImportFactory("mapNode2", configs3))
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c changed", gestalt.getConfig("c", String.class));
        Assertions.assertEquals("d", gestalt.getConfig("d", String.class));
    }

    @Test
    public void testImportNode() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("path.b", "b changed");
        configs.put("path.c", "c");
        configs.put("$import:1", "source=node , path = path ");


        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testImportNodeUnder() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("path.b", "b changed");
        configs.put("path.c", "c");
        configs.put("$import:-1", "source=node,path=path");


        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testImportClasspath() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$import:1", "source=classPath,resource=import.properties");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testImportNodeClasspath() throws GestaltException {

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$import:-1", "source=classPath,resource=import.properties");

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testImportFile() throws GestaltException {

        // Load the default property files from resources.
        URL fileNode = GestaltImportProcessorTest.class.getClassLoader().getResource("import.properties");
        File devFile = new File(fileNode.getFile());

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$import:1", "source=file,file=" + devFile.getAbsolutePath());

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b changed", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }

    @Test
    public void testImportNodeFile() throws GestaltException {


        // Load the default property files from resources.
        URL fileNode = GestaltImportProcessorTest.class.getClassLoader().getResource("import.properties");
        File devFile = new File(fileNode.getFile());

        Map<String, String> configs = new HashMap<>();
        configs.put("a", "a");
        configs.put("b", "b");
        configs.put("$import:-1", "source=file,path=" + devFile.getAbsolutePath());

        Gestalt gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();

        gestalt.loadConfigs();

        Assertions.assertEquals("a", gestalt.getConfig("a", String.class));
        Assertions.assertEquals("b", gestalt.getConfig("b", String.class));
        Assertions.assertEquals("c", gestalt.getConfig("c", String.class));
    }
}
