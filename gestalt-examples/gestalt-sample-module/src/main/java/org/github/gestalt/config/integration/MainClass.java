package org.github.gestalt.config.integration;

import io.github.jopenlibs.vault.VaultException;
import org.github.gestalt.config.exceptions.GestaltException;

import java.io.IOException;

/**
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
public class MainClass {

    public static void main(String[] args) throws GestaltException, IOException, VaultException {
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
        configTest.integrationGitTest();
        configTest.integrationTestPostProcessorEnvironment();
        configTest.integrationTestPostProcessorSystem();
        configTest.integrationTestPostProcessorNode();
        configTest.integrationTestCamelCase();
        configTest.testObservations();
        configTest.testValidationOk();
        configTest.testValidationError();
        configTest.temporaryNode();
        configTest.encryptedPassword();
        configTest.encryptedPasswordAndTemporaryNode();
        configTest.testImportSubPath();
        configTest.testImportNested();
        configTest.testImportNode();
        configTest.testImportNodeClasspath();
        configTest.testImportFile();

        if( Boolean.parseBoolean(System.getenv("LOCAL_TEST"))) {
            configTest.integrationTestGoogleCloud();
            configTest.integrationTestAws();
            configTest.integrationTestAzure();
        }

        if( Boolean.parseBoolean(System.getenv("GESTALT_VAULT_TEST"))) {
            configTest.integrationTestPostProcessorVault();
        }

        var kotlinTests = new GestaltKotlinTest();
        kotlinTests.integrationTest();
        kotlinTests.integrationTestEnvVars();
        kotlinTests.integrationTestWithTypeOf();

        GestaltSubstitutionTest gestaltSubstitutionTest = new GestaltSubstitutionTest();
        gestaltSubstitutionTest.testEscapedSubstitution();
        gestaltSubstitutionTest.testEscapedRunTimeSubstitution();
        gestaltSubstitutionTest.testNestedSubstitution();
        gestaltSubstitutionTest.testNestedRunTimeSubstitution();
        gestaltSubstitutionTest.testSubstitution();
        gestaltSubstitutionTest.testRunTimeSubstitution();
        gestaltSubstitutionTest.testMixedNestedSubstitution();
        gestaltSubstitutionTest.testDist100RunTimeSubstitution();

        System.out.println("Completed validations");
    }
}
