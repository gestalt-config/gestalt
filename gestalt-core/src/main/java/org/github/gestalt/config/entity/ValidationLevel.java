package org.github.gestalt.config.entity;

/**
 * Level of the error, we can recover from warnings but may want to fail on errors.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
public enum ValidationLevel {
    /**
     * Error validation level.
     */
    ERROR,
    /**
     * Warning validation level.
     */
    WARN,
    /**
     * debug validation level.
     */
    DEBUG
}
