package com.github.gestalt.config.validation.hibernate.builder;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HibernateModuleBuilderTest {

    @Test
    public void builderTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        var validator = factory.getValidator();
        HibernateModuleBuilder builder = HibernateModuleBuilder.builder().setValidator(validator);

        Assertions.assertEquals(validator, builder.getValidator());

        var config = builder.build();
        Assertions.assertEquals(validator, config.getValidator());
        Assertions.assertEquals("hibernate-validator", config.name());
    }

}
