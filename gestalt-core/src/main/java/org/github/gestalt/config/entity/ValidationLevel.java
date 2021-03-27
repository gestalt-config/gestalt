package org.github.gestalt.config.entity;

/**
 * Level of the error, we can recover from warnings but may want to fail on errors.
 *
 * @author Colin Redmond
 */
public enum ValidationLevel {
    FATAL,  // An unrecoverable error
    ERROR,  // An error but recoverable
    WARN,   // Recoverable warning
    INFO,   //
    DEBUG   //
}
