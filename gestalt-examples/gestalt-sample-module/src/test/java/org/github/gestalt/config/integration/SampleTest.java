package org.github.gestalt.config.integration;

import io.github.jopenlibs.vault.VaultException;
import org.github.gestalt.config.exceptions.GestaltException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SampleTest {

    @Test
    public void testAll() throws VaultException, IOException, GestaltException {
        MainClass.main(null);
    }
}
