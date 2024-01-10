package org.github.gestalt.config.integration;

import io.github.jopenlibs.vault.VaultException;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.*;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

/**
 * @author Colin Redmond (c) 2023.
 */
public class MainOverride {


    // for this to work you need to set the following VM Options
    // -Dhttp.pool.maxTotal=200 -Dhttp.pool.maxPerRoute=50
    public static void main(String[] args) throws GestaltException {

        // The later ones layer on and over write any values in the previous
        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("default.properties").build())
            .addSource(ClassPathConfigSourceBuilder.builder().setResource("dev.properties").build())
            .addSource(SystemPropertiesConfigSourceBuilder.builder().build())
            .setTreatNullValuesInClassAsErrors(false)
            .build();

        // Load the configurations, this will throw exceptions if there are any errors.
        gestalt.loadConfigs();

        GestaltConfigTest.HttpPool pool = gestalt.getConfig("http.pool", GestaltConfigTest.HttpPool.class);

        Assertions.assertEquals(200, pool.maxTotal);
        Assertions.assertEquals((short) 200, gestalt.getConfig("http.pool.maxTotal", Short.class));
        Assertions.assertEquals(50L, pool.maxPerRoute);
        Assertions.assertEquals(50L, gestalt.getConfig("http.pool.maxPerRoute", Long.class));
        Assertions.assertEquals(6000, pool.validateAfterInactivity);
        Assertions.assertEquals(60000D, pool.keepAliveTimeoutMs);
        Assertions.assertEquals(25, pool.idleTimeoutSec);
        Assertions.assertEquals(33.0F, pool.defaultWait);

    }
}
