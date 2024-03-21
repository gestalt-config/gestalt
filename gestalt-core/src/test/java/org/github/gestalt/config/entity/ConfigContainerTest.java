package org.github.gestalt.config.entity;

import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.decoder.DecoderContext;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

class ConfigContainerTest {

    private DecoderContext decoderContext = Mockito.mock();
    private Gestalt gestalt = Mockito.mock();

    @BeforeEach
    void setup() {
        Mockito.clearInvocations(decoderContext, gestalt);

        Mockito.when(decoderContext.getGestalt()).thenReturn(gestalt);
    }

    @Test
    void getValue() throws GestaltException {

        var configContainer = new ConfigContainer<>("my.path", Tags.environment("dev"), decoderContext, "value1", new TypeCapture<>() {
        });

        Assertions.assertEquals("value1", configContainer.orElseThrow());

        Mockito.verify(gestalt, Mockito.times(1)).registerListener(any());
    }

    @Test
    void getValueOptional() {
        var configContainer = new ConfigContainer<>("my.path", Tags.environment("dev"), decoderContext, "value1", new TypeCapture<>() {
        });

        Assertions.assertEquals(Optional.of("value1"), configContainer.getOptional());
        Mockito.verify(gestalt, Mockito.times(1)).registerListener(any());
    }

    @Test
    void getValueNull() {
        var configContainer = new ConfigContainer<>("my.path", Tags.environment("dev"), decoderContext, null,
            new TypeCapture<ConfigContainer<String>>() {
            });

        var ex = Assertions.assertThrows(GestaltException.class, configContainer::orElseThrow);
        Assertions.assertEquals("No results for config path: my.path, tags: " +
                "Tags{[Tag{key='environment', value='dev'}]}, and " +
                "class: org.github.gestalt.config.entity.ConfigContainer<java.lang.String>",
            ex.getMessage());

        Mockito.verify(gestalt, Mockito.times(1)).registerListener(any());
    }

    @Test
    void reload() throws GestaltException {
        Mockito.when(decoderContext.getGestalt()).thenReturn(gestalt);

        Mockito.when(
            gestalt.getConfigOptional("my.path", TypeCapture.of(String.class), Tags.environment("dev"))
        ).thenReturn(Optional.of("value2"));

        var configContainer = new ConfigContainer<>("my.path", Tags.environment("dev"), decoderContext, "value1",
            new TypeCapture<>() {
            });

        Assertions.assertEquals("value1", configContainer.orElseThrow());

        configContainer.reload();

        Assertions.assertEquals("value2", configContainer.orElseThrow());
        Assertions.assertEquals(Optional.of("value2"), configContainer.getOptional());

        Mockito.verify(gestalt).getConfigOptional("my.path", TypeCapture.of(String.class), Tags.environment("dev"));

        Mockito.verify(gestalt, Mockito.times(1)).registerListener(any());
    }

    @Test
    void reloadEmpty() throws GestaltException {
        decoderContext = Mockito.mock();
        gestalt = Mockito.mock();

        Mockito.when(decoderContext.getGestalt()).thenReturn(gestalt);

        Mockito.when(
            gestalt.getConfigOptional("my.path", TypeCapture.of(String.class), Tags.environment("dev"))
        ).thenReturn(Optional.empty());

        var configContainer = new ConfigContainer<>("my.path", Tags.environment("dev"), decoderContext, "value1",
            new TypeCapture<>() {
            });

        Assertions.assertEquals("value1", configContainer.orElseThrow());

        configContainer.reload();

        var ex = Assertions.assertThrows(GestaltException.class, configContainer::orElseThrow);
        Assertions.assertEquals("No results for config path: my.path, tags: " +
                "Tags{[Tag{key='environment', value='dev'}]}, and " +
                "class: org.github.gestalt.config.entity.ConfigContainer<java.lang.String>",
            ex.getMessage());

        Assertions.assertEquals(Optional.empty(), configContainer.getOptional());

        Mockito.verify(gestalt, Mockito.times(1)).getConfigOptional("my.path", TypeCapture.of(String.class),
            Tags.environment("dev"));

        Mockito.verify(gestalt, Mockito.times(1)).registerListener(any());
    }
}
