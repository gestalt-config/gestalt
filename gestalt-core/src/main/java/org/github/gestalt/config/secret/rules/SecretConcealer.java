package org.github.gestalt.config.secret.rules;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SecretConcealer {
    private final Map<String, Pattern> secretRegex;
    private final String mask;

    public SecretConcealer(Set<String> secretRegex, String mask) {
        this.secretRegex = secretRegex.stream().collect(Collectors.toMap(Function.identity(), Pattern::compile));
        this.mask = mask;
    }

    public String getMask() {
        return mask;
    }

    public void addSecretRule(String rule) {
        secretRegex.put(rule, Pattern.compile(rule));
    }

    public String concealSecret(String path, String value) {
        if (secretRegex.values().stream().anyMatch(rule -> rule.matcher(path).find())) {
            return mask;
        } else {
            return value;
        }
    }
}
