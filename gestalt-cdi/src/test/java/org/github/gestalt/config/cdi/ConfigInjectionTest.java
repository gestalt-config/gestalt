package org.github.gestalt.config.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltConfigurationException;
import org.github.gestalt.config.source.MapConfigSource;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(WeldJunit5Extension.class)
class ConfigInjectionTest {
    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(ConfigExtension.class, ConfigBean.class)
                                      .addBeans()
                                      .activate(ApplicationScoped.class)
                                      .inject(this)
                                      .build();

    @Inject
    ConfigBean configBean;

    @BeforeAll
    static void beforeAll() throws GestaltConfigurationException {
        Map<String, String> configs = new HashMap<>();
        configs.put("my.prop", "1234");
        configs.put("expansion", "1234");
        configs.put("secret", "12345678");
        configs.put("my.prop.profile", "5678");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new MapConfigSource(configs))
            .build();

        ConfigProvider.registerGestalt(gestalt);
    }

    @Test
    void inject() {
        assertEquals("1234", configBean.getMyProp());
        assertEquals("1234", configBean.getExpansion());
        assertEquals("12345678", configBean.getSecret());
        assertEquals("5678", configBean.getMyPropProfile());
    }

    @ApplicationScoped
    static class ConfigBean {

        @Inject
        @GestaltConfig(path = "my.prop")
        String myProp;
        @Inject
        @GestaltConfig(path = "expansion")
        String expansion;
        @Inject
        @GestaltConfig(path = "secret")
        String secret;
        @Inject
        @GestaltConfig(path = "my.prop.profile")
        String myPropProfile;


        public ConfigBean() {
        }

        String getMyProp() {
            return myProp;
        }

        String getExpansion() {
            return expansion;
        }

        String getSecret() {
            return secret;
        }

        String getMyPropProfile() {
            return myPropProfile;
        }

    }
}
