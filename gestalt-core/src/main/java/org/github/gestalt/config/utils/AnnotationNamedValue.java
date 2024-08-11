package org.github.gestalt.config.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AnnotationNamedValue {

    public static String getNamedValue(Annotation namedAnnotation) {
        try {
            Method valueMethod =  namedAnnotation.annotationType().getMethod("value");
            return (String) valueMethod.invoke(namedAnnotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return "";
        }
    }
}
