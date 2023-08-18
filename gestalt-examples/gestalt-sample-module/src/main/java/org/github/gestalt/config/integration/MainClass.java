package org.github.gestalt.config.integration;

import org.github.gestalt.config.exceptions.GestaltException;

/**
 * @author Colin Redmond (c) 2023.
 */
public class MainClass {

    public static void main(String[] args) throws GestaltException {
        GestaltConfigTest configTest = new GestaltConfigTest();
        configTest.integrationTest();
        configTest.integrationTestNoCache();
        configTest.integrationTestTags();
        configTest.integrationTestEnvVars();
        configTest.integrationTestJson();
        configTest.integrationTestYaml();
        configTest.integrationTestJsonAndYaml();
        configTest.integrationTestHocon();
        configTest.integrationTestToml();
    }
}
