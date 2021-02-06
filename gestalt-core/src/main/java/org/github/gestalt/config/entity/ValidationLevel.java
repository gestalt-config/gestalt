package org.github.gestalt.config.entity;

/**
 * Level of the error, we can recover from warnings but may want to fail on errors.
 *
 * @author Colin Redmond
 */
public enum ValidationLevel {
    ERROR,
    WARN
}
