package org.github.gestalt.config.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.MapConfigSource;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    static void beforeAll() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("my.prop.user", "steve");
        configs.put("expansion", "1234");
        configs.put("secret", "12345.678");
        configs.put("my.prop.id", "5678901234567890");
        configs.put("my.prop.enabled", "false");
        configs.put("shortId", "23");
        configs.put("byteId", "A");
        configs.put("charId", "Q");
        configs.put("map.data1", "1,2,3,4,5");
        configs.put("map.data2", "6,7,8,9,0");


        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(new MapConfigSource(configs))
            .build();
        gestalt.loadConfigs();

        ConfigProvider.registerGestalt(gestalt);
    }

    @Test
    void inject() {
        assertEquals("steve", configBean.getMyPropUser());
        assertEquals(1234, configBean.getExpansion());
        assertEquals(1234L, configBean.getExpansionL());
        assertEquals(12345.678F, configBean.getSecret());
        assertEquals(5678901234567890D, configBean.getMyPropProfile());
        assertEquals(false, configBean.getMyPropProfileEnabled());
        assertEquals((short) 23, configBean.getShortId());
        assertEquals((byte) 65, configBean.getByteId());
        assertEquals('Q', configBean.getCharId());
        assertEquals(Map.of("user", "steve", "id", "5678901234567890", "enabled", "false"),
            configBean.getMyPropMap());
        assertEquals(Map.of("data1", List.of(1, 2, 3, 4, 5), "data2", List.of(6, 7, 8, 9, 0)),
            configBean.getMapOfInt());

        assertEquals(List.of(1, 2, 3, 4, 5), configBean.getListOfInt());
        assertEquals(Set.of(1, 2, 3, 4, 5), configBean.getSetOfInt());

        assertEquals(Optional.of(false), configBean.getMyPropProfileEnabledOpt());

        assertEquals(Optional.empty(), configBean.getEmptyOpt());

        assertEquals("steve", configBean.getSupplierMyProp().get());
    }

    @ApplicationScoped
    static class ConfigBean {

        @Inject
        @GestaltConfig(path = "my.prop.user")
        String myPropUser;

        @Inject
        @GestaltConfig(path = "expansion")
        Long expansionL;

        @Inject
        @GestaltConfig(path = "expansion")
        Integer expansion;
        @Inject
        @GestaltConfig(path = "secret")
        Float secret;

        @Inject
        @GestaltConfig(path = "my.prop.id")
        Double myPropProfile;

        @Inject
        @GestaltConfig(path = "my.prop.enabled")
        Boolean myPropProfileEnabled;

        @Inject
        @GestaltConfig(path = "shortId")
        Short shortId;

        @Inject
        @GestaltConfig(path = "byteId")
        Byte byteId;

        @Inject
        @GestaltConfig(path = "charId")
        Character charId;

        @Inject
        @GestaltConfig(path = "my.prop")
        Map<String, String> myPropMap;

        @Inject
        @GestaltConfig(path = "map")
        Map<String, List<Integer>> mapOfInt;

        @Inject
        @GestaltConfig(path = "map.data1")
        List<Integer> listOfInt;

        @Inject
        @GestaltConfig(path = "map.data1")
        Set<Integer> setOfInt;

        @Inject
        @GestaltConfig(path = "my.prop.enabled")
        Optional<Boolean> myPropProfileEnabledOpt;

        @Inject
        @GestaltConfig(path = "my.prop.not.exist")
        Optional<Boolean> emptyOpt;

        @Inject
        @GestaltConfig(path = "my.prop.user")
        Supplier<String> supplierMyProp;

        public ConfigBean() {
        }

        public String getMyPropUser() {
            return myPropUser;
        }

        public Long getExpansionL() {
            return expansionL;
        }

        public Integer getExpansion() {
            return expansion;
        }

        public Float getSecret() {
            return secret;
        }

        public Double getMyPropProfile() {
            return myPropProfile;
        }

        public Boolean getMyPropProfileEnabled() {
            return myPropProfileEnabled;
        }

        public Short getShortId() {
            return shortId;
        }

        public Byte getByteId() {
            return byteId;
        }

        public Character getCharId() {
            return charId;
        }

        public Map<String, String> getMyPropMap() {
            return myPropMap;
        }

        public Map<String, List<Integer>> getMapOfInt() {
            return mapOfInt;
        }

        public List<Integer> getListOfInt() {
            return listOfInt;
        }

        public Set<Integer> getSetOfInt() {
            return setOfInt;
        }

        public Optional<Boolean> getMyPropProfileEnabledOpt() {
            return myPropProfileEnabledOpt;
        }

        public Optional<Boolean> getEmptyOpt() {
            return emptyOpt;
        }

        public Supplier<String> getSupplierMyProp() {
            return supplierMyProp;
        }
    }
}
