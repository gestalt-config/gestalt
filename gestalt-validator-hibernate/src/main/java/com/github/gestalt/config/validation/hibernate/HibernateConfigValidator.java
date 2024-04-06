package com.github.gestalt.config.validation.hibernate;

import com.github.gestalt.config.validation.hibernate.config.HibernateModuleConfig;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.github.gestalt.config.entity.GestaltConfig;
import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;
import org.github.gestalt.config.reflect.TypeCapture;
import org.github.gestalt.config.tag.Tags;
import org.github.gestalt.config.utils.GResultOf;
import org.github.gestalt.config.validation.ConfigValidator;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the Gestalt validator for Hibernate. Used to validate configurations.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public final class HibernateConfigValidator implements ConfigValidator {

    private static final System.Logger logger = System.getLogger(HibernateConfigValidator.class.getName());

    private jakarta.validation.Validator validator;

    public HibernateConfigValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Override
    public void applyConfig(GestaltConfig config) {
        HibernateModuleConfig moduleConfig = config.getModuleConfig(HibernateModuleConfig.class);
        if (moduleConfig == null) {
            logger.log(System.Logger.Level.WARNING, "Hibernate Validator will be set to the defaults and use a " +
                "Validation.buildDefaultValidatorFactory() to build a Validator, please register a HibernateModuleConfig with " +
                "gestalt builder to customize the validation.");

            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            validator = factory.getValidator();
        } else {

            validator = moduleConfig.getValidator();
        }
    }

    @Override
    public <T> GResultOf<T> validator(T obj, String path, TypeCapture<T> klass, Tags tags) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);

        if (constraintViolations.isEmpty()) {
            return GResultOf.result(obj);
        } else {
            return GResultOf.errors(constraintViolations.stream()
                .map(ConstraintViolation::getMessage)
                .map(it -> new HibernateValidatorError(path, it))
                .collect(Collectors.toList()));
        }
    }

    public static class HibernateValidatorError extends ValidationError {
        private final String path;
        private final String error;

        public HibernateValidatorError(String path, String error) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.error = error;
        }

        @Override
        public String description() {
            return "Hibernate Validator, on path: " + path + ", error: " + error;
        }
    }
}
