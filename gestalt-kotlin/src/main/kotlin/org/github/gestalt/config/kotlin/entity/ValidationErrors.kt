package org.github.gestalt.config.kotlin.entity

import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.entity.ValidationLevel

/**
 * Kotlin specific Validation Errors
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */

/**
 * Data class has no constructor.
 */
class DataClassHasNoConstructor(private val path: String, private val className: String) : ValidationError(ValidationLevel.ERROR) {
    override fun description(): String {
        return "Data Class has no constructor: $className, on path: $path"
    }
}

/**
 * Data class is missing required member
 */
class DataClassMissingRequiredMember(private val path: String, private val className: String, private val missingMembers: List<String>) :
    ValidationError(ValidationLevel.ERROR) {
    override fun description(): String {
        return "Data Class: $className, can not be constructed. Missing required members $missingMembers, on path: $path"
    }
}

/**
 * Data class can not be built
 */
class DataClassCanNotBeConstructed(private val path: String, private val className: String) : ValidationError(ValidationLevel.ERROR) {
    override fun description(): String {
        return "Data Class: $className, can not be constructed on path: $path"
    }
}
