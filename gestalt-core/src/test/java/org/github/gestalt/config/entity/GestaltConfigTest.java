package org.github.gestalt.config.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GestaltConfigTest {

    @Test
    void getExtension() {
        GestaltConfig config = new GestaltConfig();
        MyModule extension = new MyModule();

        config.registerModuleConfig(extension);

        MyModule test = config.getModuleConfig(MyModule.class);

        Assertions.assertEquals("test", test.name());
        Assertions.assertEquals("myConfig", test.myConfig());
    }

    private static final class MyModule implements GestaltModuleConfig {

        @Override
        public String name() {
            return "test";
        }

        public String myConfig() {
            return "myConfig";
        }
    }

    @SuppressWarnings("removal")
    @Test
    void codeCoverage() {
        GestaltConfig config = new GestaltConfig();
        config.setTreatNullValuesInClassAsErrors(true);
        config.isTreatNullValuesInClassAsErrors();
    }
}
