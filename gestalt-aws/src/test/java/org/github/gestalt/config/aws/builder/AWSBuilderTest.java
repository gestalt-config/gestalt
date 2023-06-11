package org.github.gestalt.config.aws.builder;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AWSBuilderTest {

    @Test
    public void createAWSConfig() {

        AWSBuilder builder = AWSBuilder.builder()
                                       .setRegion("test");

        Assertions.assertEquals("test", builder.getRegion());
        Assertions.assertEquals("test", builder.build().getRegion());
    }

}
