package org.github.gestalt.config.decoder;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.source.ClassPathConfigSourceBuilder;
import org.github.gestalt.config.source.FileConfigSourceBuilder;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author Colin Redmond (c) 2024.
 */
public class DecoderOrderTest {

    @Test
    public void sequencedSetAndList() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // Load the default property files from resources.
        URL devFileURL = DecoderOrderTest.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatNullValuesInClassAsErrors(false)
            .build();

        // Load the configurations, this will throw exceptions if there are any errors.
        gestalt.loadConfigs();

        List<Hosts> hosts = gestalt.getConfig("db.hosts", new TypeCapture<>() { });

        Assertions.assertEquals(1234, hosts.get(0).password);
        Assertions.assertEquals(5678, hosts.get(1).password);
        Assertions.assertEquals(9012, hosts.get(2).password);

        ArrayList<Hosts> hostsArray = gestalt.getConfig("db.hosts", new TypeCapture<>() { });

        Assertions.assertEquals(1234, hostsArray.get(0).password);
        Assertions.assertEquals(5678, hostsArray.get(1).password);
        Assertions.assertEquals(9012, hostsArray.get(2).password);

        SequencedSet<Hosts> hostsSet = gestalt.getConfig("db.hosts", new TypeCapture<>() { });

        Assertions.assertEquals(1234, hostsSet.removeFirst().password);
        Assertions.assertEquals(5678, hostsSet.removeFirst().password);
        Assertions.assertEquals(9012, hostsSet.removeFirst().password);

        LinkedHashSet<Hosts> hostsLinkedSet = gestalt.getConfig("db.hosts", new TypeCapture<>() { });

        Assertions.assertEquals(1234, hostsLinkedSet.removeFirst().password);
        Assertions.assertEquals(5678, hostsLinkedSet.removeFirst().password);
        Assertions.assertEquals(9012, hostsLinkedSet.removeFirst().password);

        SequencedCollection<Hosts> hostsCollection = gestalt.getConfig("db.hosts", new TypeCapture<>() { });

        Assertions.assertEquals(1234, hostsCollection.removeFirst().password);
        Assertions.assertEquals(5678, hostsCollection.removeFirst().password);
        Assertions.assertEquals(9012, hostsCollection.removeFirst().password);
    }

    @Test
    public void sequencedMap() throws GestaltException {
        // Create a map of configurations we wish to inject.
        Map<String, String> configs = new HashMap<>();
        configs.put("db.hosts[0].password", "1234");
        configs.put("db.hosts[1].password", "5678");
        configs.put("db.hosts[2].password", "9012");
        configs.put("db.idleTimeout", "123");

        // Load the default property files from resources.
        URL devFileURL = DecoderOrderTest.class.getClassLoader().getResource("dev.properties");
        File devFile = new File(devFileURL.getFile());

        // using the builder to layer on the configuration files.
        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("/default.properties").build())
            .addSource(FileConfigSourceBuilder.builder().setFile(devFile).build())
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatNullValuesInClassAsErrors(false)
            .build();

        // Load the configurations, this will throw exceptions if there are any errors.
        gestalt.loadConfigs();

        Map<String, Integer> httpPoolMap = gestalt.getConfig("http.pool", new TypeCapture<>() { });

        Assertions.assertEquals(50, httpPoolMap.get("maxperroute"));
        Assertions.assertEquals(6000, httpPoolMap.get("validateafterinactivity"));
        Assertions.assertEquals(60000, httpPoolMap.get("keepalivetimeoutms"));
        Assertions.assertEquals(25, httpPoolMap.get("idletimeoutsec"));

        SequencedMap<String, Integer> httpPoolMapSeq = gestalt.getConfig("http.pool", new TypeCapture<>() { });

        Assertions.assertEquals(50, httpPoolMapSeq.get("maxperroute"));
        Assertions.assertEquals(6000, httpPoolMapSeq.get("validateafterinactivity"));
        Assertions.assertEquals(60000, httpPoolMapSeq.get("keepalivetimeoutms"));
        Assertions.assertEquals(25, httpPoolMapSeq.get("idletimeoutsec"));

        LinkedHashMap<String, Integer> httpPoolMapLinkedSeq = gestalt.getConfig("http.pool", new TypeCapture<>() { });

        Assertions.assertEquals(50, httpPoolMapLinkedSeq.get("maxperroute"));
        Assertions.assertEquals(6000, httpPoolMapLinkedSeq.get("validateafterinactivity"));
        Assertions.assertEquals(60000, httpPoolMapLinkedSeq.get("keepalivetimeoutms"));
        Assertions.assertEquals(25, httpPoolMapLinkedSeq.get("idletimeoutsec"));
    }

    public record Hosts(Integer password, String user, String url) {    }

}
