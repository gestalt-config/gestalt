package com.github.gestalt.config.validation.hibernate.config;

import jakarta.validation.Validator;
import org.github.gestalt.config.entity.GestaltModuleConfig;


/**
 * Module config for Hibernate. Provides a jakarta Validator to validate objects.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class HibernateModuleConfig  implements GestaltModuleConfig {

    private final Validator validator;

    /**
     * Create the HibernateModuleConfig.
     *
     * @param validator jakarta Validator
     */
    public HibernateModuleConfig(Validator validator) {
        this.validator = validator;
    }

    @Override
    public String name() {
        return "hibernate-validator";
    }

    /**
     * Get the jakarta Validator.
     *
     * @return jakarta Validator
     */
    public Validator getValidator() {
        return validator;
    }
}
