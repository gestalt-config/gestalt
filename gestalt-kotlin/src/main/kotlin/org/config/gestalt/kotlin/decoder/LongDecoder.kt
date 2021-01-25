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
 * Kotlin Long Decoder.
 *
 * @author Colin Redmond
 */
class LongDecoder : LeafDecoder<Long>() {
    override fun name(): String {
        return "kLong"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun matches(klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(Long::class)
        } else {
            false
        }
    }

    override fun leafDecode(path: String?, node: ConfigNode): ValidateOf<Long> {
        val results: ValidateOf<Long>
        val value = node.value.orElse("")
        results = if (StringUtils.isInteger(value)) {
            try {
                val longVal = value.toLong()
                ValidateOf.valid(longVal)
            } catch (e: NumberFormatException) {
                ValidateOf.inValid(ValidationError.DecodingNumberFormatException(path, node, name()))
            }
        } else {
            ValidateOf.inValid(ValidationError.DecodingNumberParsing(path, node, name()))
        }
        return results
    }
}
