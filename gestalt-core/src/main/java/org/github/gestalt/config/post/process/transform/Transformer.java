package org.github.gestalt.config.post.process.transform;

import java.util.Optional;

/**
 * Allows you to add your own custom source for the TransformerPostProcessor.
 * Whenever the TransformerPostProcessor sees a value ${name:key} the transform is selected that matches the same name
 */
public interface Transformer {

    /**
     * the name that will match the ${name:key} the transform is selected that matches the same name.
     *
     * @return the name of the transform
     */
    String name();

    /**
     * When a match is found for ${name:key} the key and the path are passed into the process method.
     * The returned value replaces the whole ${name:key}
     *
     * @param path the current path
     * @param key the key to lookup int this transform.
     * @return the value to replace the ${name:key}
     */
    Optional<String> process(String path, String key);
}
