package org.config.gestalt.kotlin.decoder

import org.config.gestalt.decoder.LeafDecoder
import org.config.gestalt.decoder.Priority
import org.config.gestalt.kotlin.reflect.KTypeCapture
import org.config.gestalt.node.ConfigNode
import org.config.gestalt.reflect.TypeCapture
import org.config.gestalt.utils.ValidateOf

/**
 * Kotlin Boolean Decoder.
 *
 * @author Colin Redmond
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
