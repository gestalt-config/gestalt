package org.github.gestalt.config.cdi;

import jakarta.inject.Inject;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(WeldJunit5Extension.class)
class OptionalInjectionTest {
    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(GestaltConfigExtension.class, OptionalInjectionTest.class)
        .addBeans()
        .inject(this)
        .build();

    @Inject
    @InjectConfigs(prefix = "optional.int.value")
    OptionalInt optionalInt;
    @Inject
    @InjectConfigs(prefix = "optional.long.value")
    OptionalLong optionalLong;
    @Inject
    @InjectConfigs(prefix = "optional.double.value")
    OptionalDouble optionalDouble;
    @Inject
    @InjectConfigs(prefix = "optional.double.not.value")
    OptionalDouble optionalDoubleEmpty;

    @BeforeAll
    static void beforeAll() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("optional.int.value", "1");
        configs.put("optional.long.value", "2");
        configs.put("optional.double.value", "3.3");

        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();
        gestalt.loadConfigs();

        GestaltConfigProvider.registerGestalt(gestalt);
    }

    @AfterAll
    static void afterAll() {
        GestaltConfigProvider.unRegisterGestalt();
    }

    @Test
    void optionalIntInjection() {
        assertTrue(optionalInt.isPresent());
        assertEquals(1, optionalInt.getAsInt());

        assertTrue(optionalLong.isPresent());
        assertEquals(2, optionalLong.getAsLong());

        assertTrue(optionalDouble.isPresent());
        assertEquals(3.3, optionalDouble.getAsDouble(), 0);

        assertTrue(optionalDoubleEmpty.isEmpty());
    }
}
