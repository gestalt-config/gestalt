package org.github.gestalt.config.google.builder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class GoogleModuleConfigBuilderTest {

    @Test
    public void testBuilder() {
        var googleModuleConfigBuilder = GoogleModuleConfigBuilder.builder().setProjectId("test");

        Assertions.assertEquals("test", googleModuleConfigBuilder.getProjectId());

        var config =  googleModuleConfigBuilder.build();

        Assertions.assertEquals("test", config.getProjectId());
    }

}
