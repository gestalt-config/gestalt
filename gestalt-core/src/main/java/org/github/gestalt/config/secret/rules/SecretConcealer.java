package org.github.gestalt.config.secret.rules;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains all the rules on how to conceal a secret, then apply them to a value.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
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

    /**
     * returns the value that is concealed if it is a secret. Otherwise, returns the value.
     *
     * @param path path of the value
     * @param value value we are checking if we need to conceal.
     * @return the value that is concealed if it is a secret.
     */
    public String concealSecret(String path, String value) {
        if (secretRegex.values().stream().anyMatch(rule -> rule.matcher(path).find())) {
            return mask;
        } else {
            return value;
        }
    }
}
