package org.github.gestalt.config.cdi;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.MapConfigSource;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(WeldJunit5Extension.class)
class GestaltConfigsInjectionTest {
    @WeldSetup
    WeldInitiator weld = WeldInitiator
        .from(GestaltConfigExtension.class, GestaltConfigsInjectionTest.class, Server.class)
        .inject(this)
        .build();

    @Inject
    @GestaltConfigs
    Server server;
    @Inject
    @GestaltConfigs(prefix = "cloud")
    Server serverCloud;

    @Inject
    Gestalt gestalt;

    @BeforeAll
    static void beforeAll() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("server.theHost", "localhost");
        configs.put("server.port", "8080");
        configs.put("cloud.theHost", "cloud");
        configs.put("cloud.port", "9090");
        configs.put("cloud.array", "2,3");
        configs.put("cloud.list", "3,4");
        configs.put("cloud.set", "4,5");
        configs.put("cloud.optionalArray", "2,3");
        configs.put("cloud.optionalList", "3,4");
        configs.put("cloud.optionalSet", "4,5");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new MapConfigSource(configs))
            .build();
        gestalt.loadConfigs();

        GestaltConfigProvider.registerGestalt(gestalt);
    }

    @AfterAll
    static void cleanup() {
        GestaltConfigProvider.unRegisterGestalt();
    }

    @Test
    void configProperties() {
        assertNotNull(server);
        assertEquals("localhost", server.theHost);
        assertEquals(8080, server.port);
        assertEquals(1, server.array.length);
        assertEquals(1, server.list.size());
        assertEquals(1, server.set.size());

        assertNotNull(serverCloud);
        assertEquals("cloud", serverCloud.theHost);
        assertEquals(9090, serverCloud.port);
        assertEquals(2, serverCloud.array.length);
        assertEquals(2, serverCloud.list.size());
        assertEquals(2, serverCloud.set.size());
    }

    @Test
    void select() {
        Server server = CDI.current().select(Server.class, GestaltConfigs.Literal.of("")).get();
        assertNotNull(server);
        assertEquals("localhost", server.theHost);
        assertEquals(8080, server.port);
        assertEquals(1, server.array.length);
        assertEquals(1, server.list.size());
        assertEquals(1, server.set.size());

        Server cloud = CDI.current().select(Server.class, GestaltConfigs.Literal.of("cloud")).get();
        assertNotNull(cloud);
        assertEquals("cloud", cloud.theHost);
        assertEquals(9090, cloud.port);
        assertEquals(2, cloud.array.length);
        assertEquals(2, cloud.list.size());
        assertEquals(2, cloud.set.size());
    }

    @Dependent
    @GestaltConfigs(prefix = "server")
    public static class Server {
        public String theHost;
        public int port;
        @Config(defaultVal = "2")
        public Integer[] array;
        @Config(defaultVal = "3")
        public List<Integer> list;
        @Config(defaultVal = "4")
        public Set<Integer> set;
    }
}
