package org.github.gestalt.config.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
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
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(WeldJunit5Extension.class)
class ConfigInjectionTest {
    @WeldSetup
    WeldInitiator weld = WeldInitiator.from(GestaltConfigExtension.class, ConfigBean.class)
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
        configs.put("color.enum", "RED");
        configs.put("org.github.gestalt.config.cdi.ConfigInjectionTest.ConfigBean.user", "jill");



        GestaltBuilder builder = new GestaltBuilder();
        Gestalt gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .build();
        gestalt.loadConfigs();

        GestaltConfigProvider.registerGestalt(gestalt);
    }

    @AfterAll
    static void cleanup() {
        GestaltConfigProvider.unRegisterGestalt();
    }

    @Test
    void inject() {
        assertEquals("steve", configBean.getMyPropUser());
        assertEquals(1234, configBean.getExpansion());
        assertEquals(1234, configBean.getExpansion2());
        assertEquals(1234L, configBean.getExpansionL());
        assertEquals(12345.678F, configBean.getSecret());
        assertEquals(5678901234567890D, configBean.getMyPropProfile());
        assertEquals(false, configBean.getMyPropProfileEnabled());
        assertEquals(false, configBean.isMyPropProfileEnabledBoolean());
        assertEquals(true, configBean.isMyPropProfileEnabledDefault());
        assertEquals((short) 23, configBean.getShortId());
        assertEquals((byte) 65, configBean.getByteId());
        assertEquals('Q', configBean.getCharId());
        assertEquals('Q', configBean.getCharId2());
        assertEquals(Map.of("user", "steve", "id", "5678901234567890", "enabled", "false"),
            configBean.getMyPropMap());
        assertEquals(Map.of("data1", List.of(1, 2, 3, 4, 5), "data2", List.of(6, 7, 8, 9, 0)),
            configBean.getMapOfInt());

        assertEquals(5, configBean.getArrayOfInt().length);
        assertEquals(1, configBean.getArrayOfInt()[0]);
        assertEquals(2, configBean.getArrayOfInt()[1]);

        assertEquals(List.of(1, 2, 3, 4, 5), configBean.getListOfInt());
        assertEquals(Set.of(1, 2, 3, 4, 5), configBean.getSetOfInt());

        assertEquals(Optional.of(false), configBean.getMyPropProfileEnabledOpt());

        assertEquals(Optional.empty(), configBean.getEmptyOpt());

        assertEquals("steve", configBean.getSupplierMyProp().get());
        assertEquals("steve", configBean.getSupplierMyProvider().get());
        assertEquals(Color.RED, configBean.getColorEnum());
        assertEquals(Color.RED, configBean.getColorEnumDefaultExists());
        assertEquals(Color.GREEN, configBean.getColorEnumDefault());

        assertEquals(true, configBean.getDefaultVal());
        assertEquals(false, configBean.getMyPropProfileEnabledWithDefault());

        assertEquals("jill", configBean.getUser());
    }

    enum Color {
        RED,
        GREEN,
        BLUE
    }

    @ApplicationScoped
    static class ConfigBean {

        @Inject
        @InjectConfig(path = "my.prop.user")
        String myPropUser;

        @Inject
        @InjectConfig(path = "expansion")
        Long expansionL;

        @Inject
        @InjectConfig(path = "expansion")
        Integer expansion;

        @Inject
        @InjectConfig(path = "expansion")
        int expansion2;

        @Inject
        @InjectConfig(path = "secret")
        Float secret;

        @Inject
        @InjectConfig(path = "my.prop.id")
        Double myPropProfile;

        @Inject
        @InjectConfig(path = "my.prop.enabled")
        Boolean myPropProfileEnabled;

        @Inject
        @InjectConfig(path = "my.prop.enabled")
        boolean myPropProfileEnabledBoolean;

        @Inject
        @InjectConfig(path = "does.not.exist", defaultValue = "true")
        boolean myPropProfileEnabledDefault;


        @Inject
        @InjectConfig(path = "shortId")
        Short shortId;

        @Inject
        @InjectConfig(path = "byteId")
        Byte byteId;

        @Inject
        @InjectConfig(path = "charId")
        Character charId;

        @Inject
        @InjectConfig(path = "charId")
        char charId2;

        @Inject
        @InjectConfig(path = "my.prop")
        Map<String, String> myPropMap;

        @Inject
        @InjectConfig(path = "map")
        Map<String, List<Integer>> mapOfInt;

        @Inject
        @InjectConfig(path = "map.data1")
        List<Integer> listOfInt;

        @Inject
        @InjectConfig(path = "map.data1")
        Integer[] arrayOfInt;

        @Inject
        @InjectConfig(path = "map.data1")
        Set<Integer> setOfInt;

        @Inject
        @InjectConfig(path = "my.prop.enabled")
        Optional<Boolean> myPropProfileEnabledOpt;

        @Inject
        @InjectConfig(path = "my.prop.not.exist")
        Optional<Boolean> emptyOpt;

        @Inject
        @InjectConfig(path = "my.prop.user")
        Supplier<String> supplierMyProp;

        @Inject
        @InjectConfig(path = "color.enum")
        Color colorEnum;

        @Inject
        @InjectConfig(path = "color.enum", defaultValue = "GREEN")
        Color colorEnumDefaultExists;

        @Inject
        @InjectConfig(path = "does.not.exist", defaultValue = "GREEN")
        Color colorEnumDefault;

        @Inject
        @InjectConfig(path = "my.prop.user")
        Provider<String> supplierMyProvider;

        @Inject
        @InjectConfig(path = "my.prop.not.exist", defaultValue = "true")
        Boolean defaultVal;

        @Inject
        @InjectConfig(path = "my.prop.enabled", defaultValue = "true")
        Boolean myPropProfileEnabledWithDefault;

        @Inject
        @InjectConfig()
        String user;

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

        public int getExpansion2() {
            return expansion2;
        }

        public Float getSecret() {
            return secret;
        }

        public Double getMyPropProfile() {
            return myPropProfile;
        }

        public Boolean getMyPropProfileEnabled() { // NOPMD
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

        public char getCharId2() {
            return charId2;
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

        public Integer[] getArrayOfInt() {
            return arrayOfInt;
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

        public Color getColorEnum() {
            return colorEnum;
        }

        public Color getColorEnumDefaultExists() {
            return colorEnumDefaultExists;
        }

        public Color getColorEnumDefault() {
            return colorEnumDefault;
        }

        public Provider<String> getSupplierMyProvider() {
            return supplierMyProvider;
        }

        public Boolean getDefaultVal() { // NOPMD
            return defaultVal;
        }

        public Boolean getMyPropProfileEnabledWithDefault() { // NOPMD
            return myPropProfileEnabledWithDefault;
        }

        public String getUser() {
            return user;
        }

        public boolean isMyPropProfileEnabledBoolean() {
            return myPropProfileEnabledBoolean;
        }

        public boolean isMyPropProfileEnabledDefault() {
            return myPropProfileEnabledDefault;
        }
    }
}
