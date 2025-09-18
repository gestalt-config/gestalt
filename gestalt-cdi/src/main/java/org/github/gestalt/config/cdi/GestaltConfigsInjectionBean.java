package org.github.gestalt.config.cdi;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.AnnotationLiteral;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.exceptions.GestaltException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;


/**
 * Based on <a href="https://github.com/smallrye/smallrye-config/tree/3.1.1/cdi">...</a>.
 */
public final class GestaltConfigsInjectionBean<T> implements Bean<T> {
    private final ConfigClassWithPrefix configClassWithPrefix;
    private final Set<Annotation> qualifiers;

    GestaltConfigsInjectionBean(final ConfigClassWithPrefix configClassWithPrefix) {
        this.configClassWithPrefix = configClassWithPrefix;
        this.qualifiers = Collections.singleton(InjectConfigs.Literal.of(configClassWithPrefix.getPrefix()));
    }


    @Override
    @SuppressWarnings({"unchecked"})
    public Class<T> getBeanClass() {
        return (Class<T>) configClassWithPrefix.getKlass();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public T create(final CreationalContext<T> creationalContext) {
        String prefix = configClassWithPrefix.getPrefix();
        if (prefix.isBlank()) {
            prefix = configClassWithPrefix.getKlass().getAnnotation(InjectConfigs.class).prefix();
            if (prefix.isBlank()) {
                prefix = "";
            }
        }

        Gestalt config = GestaltConfigProvider.getGestaltConfig();
        try {
            return config.getConfig(prefix, getBeanClass());
        } catch (GestaltException e) {
            throw new GestaltConfigException("unable to retrieve config for " + prefix, prefix, e);
        }
    }

    @Override
    public void destroy(final T instance, final CreationalContext<T> creationalContext) {

    }

    @Override
    public Set<Type> getTypes() {
        return Collections.singleton(configClassWithPrefix.getKlass());
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + "_" +
            configClassWithPrefix.getKlass().getName() + "_" +
            configClassWithPrefix.getPrefix();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }


    @SuppressWarnings("UnusedNestedClass")
    private static final class InjectConfigsLiteral extends AnnotationLiteral<InjectConfigs> implements InjectConfigs {

        @Override
        public String prefix() {
            return null;
        }
    }
}
