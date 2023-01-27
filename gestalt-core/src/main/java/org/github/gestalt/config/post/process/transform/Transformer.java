package org.github.gestalt.config.post.process.transform;

import org.github.gestalt.config.post.process.PostProcessorConfig;
import org.github.gestalt.config.utils.ValidateOf;

/**
 * Allows you to add your own custom source for the TransformerPostProcessor.
 * Whenever the TransformerPostProcessor sees a value ${name:key} the transform is selected that matches the same name
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
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
    ValidateOf<String> process(String path, String key);

    /**
     * Apply the PostProcessorConfig to the Transformer. Needed when building via the ServiceLoader
     * It is a default method as most Transformer don't need to apply configs.
     *
     * @param config GestaltConfig to update the Post Processor
     */
    default void applyConfig(PostProcessorConfig config) {
    }
}
