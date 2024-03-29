package org.github.gestalt.config.cdi;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.AnnotationLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

class MetadataInjectionPoint implements InjectionPoint {
    @Override
    public Type getType() {
        return InjectionPoint.class;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.singleton(new AnnotationLiteral<Default>() {
        });
    }

    @Override
    public Bean<?> getBean() {
        return null;
    }

    @Override
    public Member getMember() {
        return null;
    }

    @Override
    public Annotated getAnnotated() {
        return null;
    }

    @Override
    public boolean isDelegate() {
        return false;
    }

    @Override
    public boolean isTransient() {
        return false;
    }
}
