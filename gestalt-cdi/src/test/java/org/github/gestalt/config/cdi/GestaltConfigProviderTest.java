package org.github.gestalt.config.cdi;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class GestaltConfigProviderTest {

    @Test
    public void doubleRegister() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("my.prop.user", "steve");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();
        gestalt.loadConfigs();

        GestaltConfigProvider.registerGestalt(gestalt);

        var ex = Assertions.assertThrows(GestaltConfigException.class, () -> GestaltConfigProvider.registerGestalt(gestalt));

        Assertions.assertEquals("Gestalt has already been registered", ex.getMessage());
        GestaltConfigProvider.unRegisterGestalt();
    }

    @Test
    public void doubleUnRegister() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("my.prop.user", "steve");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();
        gestalt.loadConfigs();

        GestaltConfigProvider.registerGestalt(gestalt);

        GestaltConfigProvider.unRegisterGestalt();
        GestaltConfigProvider.unRegisterGestalt();
    }
}
