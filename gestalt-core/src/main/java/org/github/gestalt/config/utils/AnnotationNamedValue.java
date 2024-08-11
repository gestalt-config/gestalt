package org.github.gestalt.config.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for getting the value of a "named" annotation using reflection.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class AnnotationNamedValue {

    private AnnotationNamedValue() {

    }

    public static String getNamedValue(Annotation namedAnnotation) {
        try {
            Method valueMethod =  namedAnnotation.annotationType().getMethod("value");
            return (String) valueMethod.invoke(namedAnnotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return "";
        }
    }
}
