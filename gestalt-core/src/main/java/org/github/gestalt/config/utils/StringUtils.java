package org.github.gestalt.config.utils;

/**
 * String utils.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class StringUtils {
    private StringUtils() {

    }

    /**
     * Returns true if a string is an integer.
     *
     * @param value the string to test if it is a integer
     * @return true if this string represents an integer.
     */
    public static boolean isInteger(String value) {
        return isInteger(value, 10);
    }

    /**
     * Returns true if a string is an integer.
     *
     * @param value the string to test if it is a integer
     * @param radix the radix to test with.
     * @return true if this string represents an integer.
     */
    public static boolean isInteger(String value, int radix) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        int length = value.length();

        // the negative sign can only be the first character.
        // If the first character is '-' then skip to the next character.
        int startAt = 0;
        if (value.charAt(0) == '-') {
            startAt++;
        }

        // if the length is less than where we start this is not a number.
        // this is to cover the case of "-"
        if (length <= startAt) {
            return false;
        }

        for (int i = startAt; i < length; i++) {
            if (Character.digit(value.charAt(i), radix) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if a string is a real.
     *
     * @param value the string to test if it is a real
     * @return true if this string represents an real number.
     */
    public static boolean isReal(String value) {
        return isReal(value, 10);
    }

    /**
     * Returns true if a string is a real.
     *
     * @param value the string to test if it is a real
     * @param radix the radix to test with.
     * @return true if this string represents an real number.
     */
    public static boolean isReal(String value, int radix) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        boolean hasDecimal = false;

        int length = value.length();

        // the negative sign can only be the first character.
        // If the first character is '-' then skip to the next character.
        int startAt = 0;
        if (value.charAt(0) == '-') {
            startAt++;
        }

        // if the current valid character is a decimal, mark the decimal as seen and skip to the next character.
        if (length > startAt && value.charAt(startAt) == '.') {
            startAt++;
            hasDecimal = true;
        }

        // if the length is less than where we start then this is not a number.
        // this is to cover the case of "-", ".", and "-."
        if (length <= startAt) {
            return false;
        }

        for (int i = startAt; i < length; i++) {
            if (value.charAt(i) == '.' && !hasDecimal) {
                hasDecimal = true;
            } else if (Character.digit(value.charAt(i), radix) < 0) {
                return false;
            }
        }
        return true;
    }
}
