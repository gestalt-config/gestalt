package org.config.gestalt.kotlin.entity

import org.config.gestalt.entity.ValidationError
import org.config.gestalt.entity.ValidationLevel

class DataClassHasNoConstructor(private val path: String, private val className: String) : ValidationError(ValidationLevel.ERROR) {
    override fun description(): String {
        return "Data Class has no constructor: $className, on path: $path"
    }
}

class DataClassMissingRequiredMember(private val path: String, private val className: String, private val missingMembers: List<String>) :
    ValidationError(ValidationLevel.ERROR) {
    override fun description(): String {
        return "Data Class: $className, can not be constructed. Missing required members $missingMembers, on path: $path"
    }
}

class DataClassCanNotBeConstructed(private val path: String, private val className: String) : ValidationError(ValidationLevel.ERROR) {
    override fun description(): String {
        return "Data Class: $className, can not be constructed on path: $path"
    }
}
