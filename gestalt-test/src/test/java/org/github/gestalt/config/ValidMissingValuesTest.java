package org.github.gestalt.config;

import org.github.gestalt.config.annotations.Config;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.test.classes.DBInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ValidMissingValuesTest {

    public record MandatoryValue(String someValue, String mandatory) {}
    public record ContainsMandatoryMap(String someValue, Map<String, MandatoryValue> mandatoryMap){}
    public record OptionalValue(String someValue, Optional<String> optional){}
    public record WithDefault(String someValue, @Config(defaultVal = "myDefault") String withDefault){}

    public record DBInfoRecord(int port, String uri, String password) {}
    public record DBInfoOptionalRecord(Optional<Integer> port, Optional<String> uri, Optional<String> password) {}

    private Gestalt gestalt;

    @BeforeEach
    public void beforeEach() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("someValue", "someRandomValue");
        // "mandatory" is missing by intention
        configs.put("mandatoryMap.foo.someValue", "someRandomValue");

        gestalt = new GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())

            /*
            If none of those options is set, the second test fails, but all others are successful;
            if at least one is set, the first two tests are successful, but the last two are failing.
            */
            .setTreatMissingValuesAsErrors(true)
            .setTreatWarningsAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)

            .build();

        gestalt.loadConfigs();
    }

    @Test
    public void FailIfMandatoryIsMissing() throws GestaltException {
        Assertions.assertThrows(
            GestaltException.class,
            () -> gestalt.getConfig("", MandatoryValue.class),
            "Missing mandatory value should throw."
        );
    }

    @Test
    public void FailIfMandatoryIsMissingInMap() throws GestaltException {
        // by default returns null-map-value 'ContainsMandatoryMap[someValue=someRandomValue, mandatoryMap={foo=null}]'
        Assertions.assertThrows(
            GestaltException.class,
            () -> gestalt.getConfig("", ContainsMandatoryMap.class),
            "Missing mandatory value should throw instead of resulting in null-map-value."
        );
    }

    @Test
    public void AllowOptionalForMissingKeys() throws GestaltException {
        OptionalValue optionalValue = gestalt.getConfig("", OptionalValue.class);
        Assertions.assertTrue(optionalValue.optional.isEmpty(), "Optional should be a valid default and not throw");
    }

    @Test
    public void AllowDefaultForMissingKeys() throws GestaltException {
        WithDefault withDefault = gestalt.getConfig("", WithDefault.class);
        Assertions.assertEquals("myDefault", withDefault.withDefault(), "Default value should be valid for missing key and not throw");
    }


}
