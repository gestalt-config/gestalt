package org.github.gestalt.config.source;

import org.github.gestalt.config.builder.SourceBuilder;
import org.github.gestalt.config.exceptions.GestaltException;

/**
 * ConfigSourceBuilder for the Class Path Config Source.
 *
 * <p>Load a config source from a classpath resource using the getResourceAsStream method.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public final class ClassPathConfigSourceBuilder extends SourceBuilder<ClassPathConfigSourceBuilder, ClassPathConfigSource> {

    private String resource;

    /**
     * private constructor, use the builder method.
     */
    private ClassPathConfigSourceBuilder() {

    }

    /**
     * Static function to create the builder.
     *
     * @return the builder
     */
    public static ClassPathConfigSourceBuilder builder() {
        return new ClassPathConfigSourceBuilder();
    }

    /**
     * Get the resource path as a string.
     *
     * @return the resource path as a string
     */
    public String getResource() {
        return resource;
    }

    /**
     * Set the resource path as a string.
     *
     * @param resource the resource path as a string.
     * @return the builder
     */
    public ClassPathConfigSourceBuilder setResource(String resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public ConfigSourcePackage<ClassPathConfigSource> build() throws GestaltException {
        return buildPackage(new ClassPathConfigSource(resource, tags));
    }
}
