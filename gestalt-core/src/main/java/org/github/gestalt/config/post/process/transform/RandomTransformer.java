package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.utils.ValidateOf;

import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transformer that when provided an expression in the format int, int(10) or int(10, 10) will replace the value with a random value.
 * For most numeric types it uses Random and the parameters are the origin and bound. For Bytes the parameter is how many bytes to generate.
 */
public class RandomTransformer implements Transformer {

    private static final Pattern randomPattern = Pattern.compile(
        "^(?<type>[A-Za-z]+)(\\((?<p1>[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)),?(?<p2>[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+))?\\))?$");
    private final Random random;

    /**
     * Create a new RandomTransformer with a random seed.
     */
    public RandomTransformer() {
        random = new Random();
    }

    /**
     * For creating a random transformer with a seed. Mainly used for testing.
     *
     * @param seed the seed for the random variable.
     */
    public RandomTransformer(long seed) {
        random = new Random(seed);
    }

    private String randomChar() {
        return String.valueOf((char) (random.nextInt(26) + 'a'));
    }

    @Override
    public String name() {
        return "random";
    }

    @Override
    public ValidateOf<String> process(String path, String key) {

        ValidateOf<String> result;

        Matcher matcher = randomPattern.matcher(key.replace(" ", ""));
        if (matcher.find()) {
            String transformName = matcher.group("type");
            String p1 = matcher.group("p1");
            String p2 = matcher.group("p2");

            try {
                switch (transformName.toLowerCase()) {
                    case "byte": {
                        Integer parameter1 = p1 == null || p1.isEmpty() ? null : Integer.parseInt(p1);
                        Integer parameter2 = p2 == null || p2.isEmpty() ? null : Integer.parseInt(p2);

                        byte[] bytes;
                        if (parameter1 != null) {
                            bytes = new byte[parameter1];
                        } else {
                            bytes = new byte[1];
                        }
                        random.nextBytes(bytes);

                        if (parameter2 != null) {
                            result = ValidateOf.validateOf(Base64.getEncoder().encodeToString(bytes),
                                new ValidationError.InvalidNumberOfParametersForRandomExpression(path, key, transformName, 1));
                        } else {
                            result = ValidateOf.valid(Base64.getEncoder().encodeToString(bytes));
                        }
                    }
                    break;

                    case "int": {
                        Integer parameter1 = p1 == null || p1.isEmpty() ? null : Integer.parseInt(p1);
                        Integer parameter2 = p2 == null || p2.isEmpty() ? null : Integer.parseInt(p2);

                        if (parameter1 != null && parameter2 != null) {
                            result = ValidateOf.valid(String.valueOf(random.ints(parameter1, parameter2).findFirst().getAsInt()));
                        } else if (parameter1 != null) {
                            result = ValidateOf.valid(String.valueOf(random.nextInt(parameter1)));
                        } else {
                            result = ValidateOf.valid(String.valueOf(random.nextInt()));
                        }
                    }
                    break;

                    case "long": {
                        Long parameter1 = p1 == null || p1.isEmpty() ? null : Long.parseLong(p1);
                        Long parameter2 = p2 == null || p2.isEmpty() ? null : Long.parseLong(p2);
                        if (parameter1 != null && parameter2 != null) {
                            result = ValidateOf.valid(String.valueOf(random.longs(parameter1, parameter2).findFirst().getAsLong()));
                        } else if (parameter1 != null) {
                            result = ValidateOf.valid(String.valueOf(random.longs(0, parameter1).findFirst().getAsLong()));
                        } else {
                            result = ValidateOf.valid(String.valueOf(random.nextLong()));
                        }
                    }
                    break;

                    case "float": {
                        Float parameter1 = p1 == null || p1.isEmpty() ? null : Float.parseFloat(p1);
                        Float parameter2 = p2 == null || p2.isEmpty() ? null : Float.parseFloat(p2);
                        if (parameter1 != null && parameter2 != null) {
                            result = ValidateOf.valid(String.valueOf(random.nextFloat() * (parameter2 - parameter1) + parameter1));
                        } else if (parameter1 != null) {
                            result = ValidateOf.valid(String.valueOf(random.nextFloat() * parameter1));
                        } else {
                            result = ValidateOf.valid(String.valueOf(random.nextFloat()));
                        }
                    }
                    break;

                    case "double": {
                        Double parameter1 = p1 == null || p1.isEmpty() ? null : Double.parseDouble(p1);
                        Double parameter2 = p2 == null || p2.isEmpty() ? null : Double.parseDouble(p2);
                        if (parameter1 != null && parameter2 != null) {
                            result = ValidateOf.valid(String.valueOf(random.doubles(parameter1, parameter2).findFirst().getAsDouble()));
                        } else if (parameter1 != null) {
                            result = ValidateOf.valid(String.valueOf(random.doubles(0, parameter1).findFirst().getAsDouble()));
                        } else {
                            result = ValidateOf.valid(String.valueOf(random.nextDouble()));
                        }
                    }
                    break;

                    case "boolean": {
                        boolean value = random.nextBoolean();
                        String strResult = String.valueOf(value);
                        if (p1 != null || p2 != null) {
                            result = ValidateOf.validateOf(strResult,
                                new ValidationError.InvalidNumberOfParametersForRandomExpression(path, key, transformName, 0));
                        } else {
                            result = ValidateOf.valid(strResult);
                        }
                    }
                    break;

                    case "string": {
                        Integer parameter1 = p1 == null || p1.isEmpty() ? null : Integer.parseInt(p1);
                        if (parameter1 == null || p2 != null) {
                            result = ValidateOf.inValid(
                                new ValidationError.InvalidNumberOfParametersForRandomExpressionError(path, key, transformName, 1));
                        } else {
                            StringBuilder sb = new StringBuilder(parameter1);
                            for (int i = 0; i < parameter1; i++) {
                                sb.append(randomChar());
                            }
                            result = ValidateOf.valid(sb.toString());
                        }
                    }
                    break;

                    case "char": {
                        String strResult = randomChar();
                        if (p1 != null || p2 != null) {
                            result = ValidateOf.validateOf(strResult,
                                new ValidationError.InvalidNumberOfParametersForRandomExpression(path, key, transformName, 0));
                        } else {
                            result = ValidateOf.valid(strResult);
                        }
                    }
                    break;

                    case "uuid": {
                        String strResult = String.valueOf(UUID.randomUUID());
                        if (p1 != null || p2 != null) {
                            result = ValidateOf.validateOf(strResult,
                                new ValidationError.InvalidNumberOfParametersForRandomExpression(path, key, transformName, 0));
                        } else {
                            result = ValidateOf.valid(strResult);
                        }
                    }
                    break;
                    default:
                        result = ValidateOf.inValid(new ValidationError.UnsupportedRandomPostProcess(path, key));
                }
            } catch (IllegalArgumentException e) {
                result = ValidateOf.inValid(new ValidationError.UnableToParseRandomParameter(path, key, transformName, p1, p2));
            }
        } else {
            result = ValidateOf.inValid(new ValidationError.UnableToParseRandomExpression(path, key));
        }

        return result;
    }
}