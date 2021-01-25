package org.config.gestalt.kotlin.decoder

import org.config.gestalt.decoder.LeafDecoder
import org.config.gestalt.decoder.Priority
import org.config.gestalt.entity.ValidationError
import org.config.gestalt.kotlin.reflect.KTypeCapture
import org.config.gestalt.node.ConfigNode
import org.config.gestalt.reflect.TypeCapture
import org.config.gestalt.utils.StringUtils
import org.config.gestalt.utils.ValidateOf

/**
 * Kotlin Short Decoder.
 *
 * @author Colin Redmond
 */
class ShortDecoder : LeafDecoder<Short>() {
    override fun name(): String {
        return "kShort"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun matches(klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(Short::class)
        } else {
            false
        }
    }

    override fun leafDecode(path: String?, node: ConfigNode): ValidateOf<Short> {
        val results: ValidateOf<Short>
        val value = node.value.orElse("")
        results = if (StringUtils.isInteger(value)) {
            try {
                val intVal = value.toShort()
                ValidateOf.valid(intVal)
            } catch (e: NumberFormatException) {
                ValidateOf.inValid(ValidationError.DecodingNumberFormatException(path, node, name()))
            }
        } else {
            ValidateOf.inValid(ValidationError.DecodingNumberParsing(path, node, name()))
        }
        return results
    }
}
