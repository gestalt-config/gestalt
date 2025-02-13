package org.github.gestalt.config.reload;

import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.source.ConfigSourcePackage;

/**
 * Reloads a source when called.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
public final class ManualConfigReloadStrategy extends ConfigReloadStrategy {

    /**
     * Constructor for ManualConfigReloadStrategy with no constructor.
     */
    public ManualConfigReloadStrategy() {
    }

    /**
     * Constructor for ManualConfigReloadStrategy.
     *
     * @param source the config source to reload
     * @deprecated Do not add the source directly, but use the source builders then add the reload strategy to the builder
     *      {@link org.github.gestalt.config.builder.SourceBuilder#addConfigReloadStrategy(ConfigReloadStrategy)}
     */
    @Deprecated(since = "0.26.0", forRemoval = true)
    public ManualConfigReloadStrategy(ConfigSourcePackage source) {
        super(source);
    }

    /**
     * Force a manual reload.
     *
     * @throws GestaltException any exceptions from reloading
     */
    @Override
    public void reload() throws GestaltException {
        super.reload();
    }
}
