/*
 * Module info definition for gestalt yaml integration
 */
@SuppressWarnings({ "requires-transitive-automatic" })
module org.github.gestalt.validation.hibernate {
    requires org.github.gestalt.core;
    requires transitive jakarta.validation;
    requires transitive org.hibernate.validator;

    exports com.github.gestalt.config.validation.hibernate;
    exports com.github.gestalt.config.validation.hibernate.config;
    exports com.github.gestalt.config.validation.hibernate.builder;

    provides org.github.gestalt.config.validation.ConfigValidator with
        com.github.gestalt.config.validation.hibernate.HibernateConfigValidator;
}
