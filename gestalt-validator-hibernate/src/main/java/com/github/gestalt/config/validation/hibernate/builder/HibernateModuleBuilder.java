package com.github.gestalt.config.validation.hibernate.builder;

import com.github.gestalt.config.validation.hibernate.config.HibernateModuleConfig;
import jakarta.validation.Validator;

/**
 * Builder for creating Vault specific configuration.
 * You can either provide the HibernateModuleConfig and the builder will create the client,
 * or you can provide a Vault client yourself.
 *
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class HibernateModuleBuilder {

    private Validator validator;

    private HibernateModuleBuilder() {

    }

    public static HibernateModuleBuilder builder() {
        return new HibernateModuleBuilder();
    }

    /**
     * Get the jakarta validator.
     *
     * @return the jakarta validator
     */
    public Validator getValidator() {
        return validator;
    }

    /**
     * Set the jakarta validator.
     *
     * @param validator the jakarta validator
     * @return builder
     */
    public HibernateModuleBuilder setValidator(Validator validator) {
        this.validator = validator;
        return this;
    }

    /**
     * Build the HibernateModuleConfig.
     *
     * @return HibernateModuleConfig
     */
    public HibernateModuleConfig build() {
        return new HibernateModuleConfig(validator);
    }
}
