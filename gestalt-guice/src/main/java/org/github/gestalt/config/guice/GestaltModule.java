package org.github.gestalt.config.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.github.gestalt.config.Gestalt;

/**
 * Example of a module you can use to inject an instance of Gestalt
 * and bind the typeListener needed to inject using annotation @InjectConfig.
 */
public class GestaltModule extends AbstractModule {
    private final Gestalt gestalt;

    public GestaltModule(Gestalt gestalt) {
        this.gestalt = gestalt;
    }

    @Override
    protected void configure() {
        bindListener(Matchers.any(), new GestaltConfigTypeListener(gestalt));
        bind(Gestalt.class).toInstance(gestalt);
    }
}
