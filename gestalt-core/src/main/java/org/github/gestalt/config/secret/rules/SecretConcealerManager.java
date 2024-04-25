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
public class SecretConcealerManager implements SecretConcealer {
    private final Map<String, Pattern> secretRegex;
    private final SecretObfuscator obfuscator;

    public SecretConcealerManager(Set<String> secretRegex, SecretObfuscator obfuscator) {
        this.secretRegex = secretRegex.stream().collect(Collectors.toMap(Function.identity(), Pattern::compile));
        this.obfuscator = obfuscator;
    }

    public SecretObfuscator getMask() {
        return obfuscator;
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
    @Override
    public String concealSecret(String path, String value) {
        if (secretRegex.values().stream().anyMatch(rule -> rule.matcher(path).find())) {
            return obfuscator.obfuscator(value);
        } else {
            return value;
        }
    }
}
