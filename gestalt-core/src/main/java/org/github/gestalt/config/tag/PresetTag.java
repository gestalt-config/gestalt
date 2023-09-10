package org.github.gestalt.config.tag;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Colin Redmond (c) 2023.
 */
public class PresetTag {
    public static Tags profile(String value) {
        return Tags.of(Tag.of("profile", value));
    }

    public static Tags profiles(String... value) {
        if (value.length == 1) {
            return profile(value[0]);
        } else {
            return Tags.of(
                Arrays.stream(value)
                    .map(it -> Tag.of("profile", it))
                    .collect(Collectors.toList())
            );
        }
    }

    public static Tags environment(String value) {
        return Tags.of(Tag.of("environment", value));
    }

    public static Tags environments(String... value) {
        if (value.length == 1) {
            return environment(value[0]);
        } else {
            return Tags.of(
                Arrays.stream(value)
                    .map(it -> Tag.of("environment", it))
                    .collect(Collectors.toList())
            );
        }
    }
}
