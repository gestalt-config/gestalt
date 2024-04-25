package org.github.gestalt.config.secret.rules;

/**
 * Interface to system to conceal secret. For a node the path and value are passed in, and it returns either the value or a masked value.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public interface SecretConcealer {

    /**
     * returns the value that is concealed if it is a secret. Otherwise, returns the value.
     *
     * @param path path of the value
     * @param value value we are checking if we need to conceal.
     * @return the value that is concealed if it is a secret.
     */
    String concealSecret(String path, String value);
}
