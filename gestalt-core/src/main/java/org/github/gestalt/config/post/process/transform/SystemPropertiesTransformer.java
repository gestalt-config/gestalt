package org.github.gestalt.config.post.process.transform;

import java.util.Optional;

/**
 * Allows you to inject System Properties into leaf values that match ${envVar:key},
 * where the key is used to lookup into the Environment Variables.
 *
 * @author Colin Redmond
 */
public class SystemPropertiesTransformer implements Transformer {
    public SystemPropertiesTransformer() {
    }

    @Override
    public String name() {
        return "sys";
    }

    @Override
    public Optional<String> process(String path, String key) {
        if(!System.getProperties().containsKey(key)) {
            return Optional.empty();
        } else {
            return Optional.of(System.getProperties().get(key).toString());
        }
    }
}
