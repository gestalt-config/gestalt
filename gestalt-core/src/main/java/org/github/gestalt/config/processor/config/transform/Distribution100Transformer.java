package org.github.gestalt.config.processor.config.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Transformer that when provided an expression in the format int, int(10) or int(10, 10) will replace the value with a random value.
 * For most numeric types it uses Random and the parameters are the origin and bound. For Bytes the parameter is how many bytes to generate.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class Distribution100Transformer implements Transformer {

    private final Random random;

    /**
     * Create a new RandomTransformer with a random seed.
     */
    public Distribution100Transformer() {
        random = new Random();
    }

    /**
     * For creating a random transformer with a seed. Mainly used for testing.
     *
     * @param seed the seed for the random variable.
     */
    public Distribution100Transformer(long seed) {
        random = new Random(seed);
    }

    private GResultOf<List<Threshold>> parseConfig(String path, String config) {
        String[] parts = config.split(",");
        int numDefaults = 0;

        List<ValidationError> errors = new ArrayList<>();
        List<Threshold> thresholds = new ArrayList<>();
        for (String part : parts) {
            if (part.contains(":")) {
                String[] pair = part.split(":");
                int limit = Integer.parseInt(pair[0].trim());
                String value = pair[1].trim();
                thresholds.add(new Threshold(limit, value));
            } else {
                // The last entry without a threshold is the default
                thresholds.add(new Threshold(Integer.MAX_VALUE, part.trim()));
                if (numDefaults++ >= 1) {
                    errors.add(new ValidationError.Dist100DuplicateDefaults(path, config));
                }
            }
        }

        thresholds.sort(Comparator.comparingInt(a -> a.limit));

        return GResultOf.resultOf(thresholds, errors);
    }

    @Override
    public String name() {
        return "dist100";
    }

    @Override
    public GResultOf<String> process(String path, String key, String rawValue) {

        if (key == null) {
            return GResultOf.errors(new ValidationError.InvalidStringSubstitutionPostProcess(path, key, name()));
        }

        // Parse the configuration
        GResultOf<List<Threshold>> thresholds = parseConfig(path, key);
        List<ValidationError> errors = new ArrayList<>(thresholds.getErrors());

        int randomInt = random.nextInt(100) + 1;

        GResultOf<String> outcome = determineOutcome(path, key, randomInt, thresholds.results());

        // add any thresholds errors to the outcome.
        errors.addAll(outcome.getErrors());
        return GResultOf.resultOf(outcome.results(), errors);
    }

    private GResultOf<String> determineOutcome(String path, String key, int number, List<Threshold> thresholds) {
        for (Threshold threshold : thresholds) {
            if (number <= threshold.limit) {
                return GResultOf.result(threshold.value);
            }
        }

        // should not get here because there should always be a default.
        // but added for safety
        return GResultOf.errors(new ValidationError.Dist100NoValueDetermined(path, key, number));
    }

    // Helper class to store thresholds
    static class Threshold {
        int limit;
        String value;

        Threshold(int limit, String value) {
            this.limit = limit;
            this.value = value;
        }
    }
}
