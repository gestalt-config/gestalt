package org.github.gestalt.config.loader;

import org.github.gestalt.config.entity.ConfigNodeContainer;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.node.ConfigNode;
import org.github.gestalt.config.source.ConfigSource;
import org.github.gestalt.config.source.ConfigSourcePackage;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;

import java.util.ArrayList;
import java.util.List;

public final class ConfigLoaderUtils {

    private ConfigLoaderUtils() {

    }

    public static GResultOf<List<ConfigNode>> convertSourceToNodes(ConfigSource source, ConfigLoaderService configLoaderService) {
        List<ValidationError> errors = new ArrayList<>();
        List<ConfigNode> configNodes = new ArrayList<>();
        try {
            // find the config loader for this source's format.
            ConfigLoader configLoader = configLoaderService.getLoader(source.format());
            // use the loader to load the source and generate config nodes.
            var loadedSource = configLoader.loadSource(new ConfigSourcePackage(source, List.of(), Tags.of()));

            errors.addAll(loadedSource.getErrors());
            if (loadedSource.hasResults()) {
                for (ConfigNodeContainer node : loadedSource.results()) {
                    configNodes.add(node.getConfigNode());
                }
            }
        } catch (GestaltException ex) {
            errors.add(new ValidationError.ConfigNodeImportException(ex));
            return GResultOf.errors(errors);
        }

        return GResultOf.resultOf(configNodes, errors);
    }
}
