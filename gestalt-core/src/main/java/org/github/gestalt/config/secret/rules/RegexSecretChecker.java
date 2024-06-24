package org.github.gestalt.config.secret.rules;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexSecretChecker implements SecretChecker {
    private final Set<Pattern> secretRegex;

    public RegexSecretChecker(String secretRegex) {
        this.secretRegex = Set.of(Pattern.compile(secretRegex));
    }

    public RegexSecretChecker(Set<String> secretRegex) {
        this.secretRegex = secretRegex.stream().map(Pattern::compile).collect(Collectors.toSet());
    }

    @Override
    public boolean isSecret(String value) {
        return secretRegex.stream().anyMatch(rule -> rule.matcher(value).find());
    }

    @Override
    public void addSecret(String rule) {
        secretRegex.add(Pattern.compile(rule));
    }
}
