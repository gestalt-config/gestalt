package org.github.gestalt.dotenv.errors;

import org.github.gestalt.config.entity.ValidationError;
import org.github.gestalt.config.entity.ValidationLevel;

/**
 * Errors for Dotenv Module.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2026.
 */
public class DotenvErrors {

    /**
     * While trying to get a configuration, was unable to find a value.
     */
    public static class NoDotenvPropertyFoundPostProcess extends ValidationError {
        private final String path;
        private final String property;

        public NoDotenvPropertyFoundPostProcess(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "No Dotenv Property found for: " + property + ", on path: " + path + " during post process";
        }
    }

    public static class DotenvNotConfiguredPostProcess extends ValidationError {
        private final String path;
        private final String property;

        public DotenvNotConfiguredPostProcess(String path, String property) {
            super(ValidationLevel.ERROR);
            this.path = path;
            this.property = property;
        }

        @Override
        public String description() {
            return "Dotenv was not configured and added as a Gestalt Module with Gestalt.addModuleConfig for " +
                "Property found for: " + property + ", on path: " + path + " during post process";
        }
    }
}
