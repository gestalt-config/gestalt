package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.LeafDecoder
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.utils.ValidateOf

/**
 * Kotlin Boolean Decoder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
class BooleanDecoder : LeafDecoder<Boolean>() {
    override fun name(): String {
        return "kBoolean"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun matches(klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(Boolean::class)
        } else {
            false
        }
    }

    override fun leafDecode(path: String?, node: ConfigNode): ValidateOf<Boolean> {
        val value = node.value.orElse("")
        return ValidateOf.valid(java.lang.Boolean.parseBoolean(value))
    }
}
