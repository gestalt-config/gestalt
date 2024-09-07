package org.github.gestalt.config.cdi;

import jakarta.inject.Inject;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;


public class ValidateInjectionTest {

    @BeforeAll
    static void beforeAll() throws GestaltException {
        Map<String, String> configs = new HashMap<>();
        configs.put("empty.property", "");
        configs.put("integer.invalid", "aaa");

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
        InnerTestClassCondition.reset();
    }

    @Test
    void missingProperty() {
        InnerTestClassCondition.isDisabled = false;
        GestaltConfigException exception = (GestaltConfigException) getException(MissingPropertyTest.class);
        assertThat(exception).hasMessage("unable to retrieve config for missing.property");
    }

    @Test
    void missingPrefix() {
        InnerTestClassCondition.isDisabled = false;
        IllegalArgumentException  exception = (IllegalArgumentException) getException(MissingPrefix.class);
        assertThat(exception).hasMessageStartingWith(
                "WELD-001408: Unsatisfied dependencies for type String with qualifiers @InjectConfigs\n" +
                "  at injection point [BackedAnnotatedField] @Inject @InjectConfigs " +
                        "org.github.gestalt.config.cdi.ValidateInjectionTest$MissingPrefix.missingProp");

        assertThat(exception.getCause()).isInstanceOf(DeploymentException.class);
    }

    @Test
    void badDataTypes() {
        InnerTestClassCondition.isDisabled = false;
        GestaltConfigException exception = (GestaltConfigException) getException(WrongDataType.class);
        assertThat(exception).hasMessageStartingWith("unable to retrieve config for integer.invalid");

        assertThat(exception.getCause()).isInstanceOf(GestaltException.class);
        assertThat(exception.getCause())
                .hasMessageStartingWith(
                        "Failed getting config path: integer.invalid, for class: java.lang.Integer\n" +
                " - level: ERROR, message: Unable to parse a number on Path: integer.invalid, from node: " +
                                "LeafNode{value='aaa'} attempting to decode Integer");
    }


    private <T> Throwable getException(Class<T> clazz) {
        LauncherDiscoveryRequest request = request().selectors(selectClass(clazz)).build();

        // Get the Launcher instance
        Launcher launcher = LauncherFactory.create();

        // Register a listener to generate a summary after the test run
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        // Run the tests
        launcher.execute(request);

        // Retrieve and print the test summary
        TestExecutionSummary summary = listener.getSummary();

        assertThat(summary.getFailures()).hasSize(1);

        return summary.getFailures().get(0).getException();
    }

    @DisabledIf("org.github.gestalt.config.cdi.InnerTestClassCondition#isDisabled")
    @ExtendWith(WeldJunit5Extension.class)
    public static class MissingPropertyTest {
        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(GestaltConfigExtension.class, MissingPropertyTest.class)
                .addBeans()
                .inject(this)
                .build();

        @Inject
        @InjectConfigs(prefix = "missing.property")
        String missingProp;

        @Test
        void fail() {
        }
    }

    @DisabledIf("org.github.gestalt.config.cdi.InnerTestClassCondition#isDisabled")
    @ExtendWith(WeldJunit5Extension.class)
    public static class MissingPrefix {
        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(GestaltConfigExtension.class, MissingPropertyTest.class)
                .addBeans()
                .inject(this)
                .build();

        @Inject
        @InjectConfigs
        String missingProp;

        @Test
        void fail() {
        }
    }

    @DisabledIf("org.github.gestalt.config.cdi.InnerTestClassCondition#isDisabled")
    @ExtendWith(WeldJunit5Extension.class)
    public static class WrongDataType {
        @WeldSetup
        WeldInitiator weld = WeldInitiator.from(GestaltConfigExtension.class, WrongDataType.class)
                .addBeans()
                .inject(this)
                .build();

        @Inject
        @InjectConfigs(prefix = "integer.invalid")
        Integer badIndexedProp;

        @Test
        void fail() {
        }
    }
}
