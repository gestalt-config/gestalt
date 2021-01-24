package org.config.gestalt.kotlin.entity

import org.config.gestalt.entity.ValidationError
import org.config.gestalt.entity.ValidationLevel

/**
 * Kotlin specific Validation Errors
 * @author Colin Redmond
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
