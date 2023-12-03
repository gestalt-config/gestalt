package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.LeafDecoder
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.StringUtils
import org.github.gestalt.config.utils.ValidateOf

/**
 * Kotlin Float Decoder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
class FloatDecoder : LeafDecoder<Float>() {
    override fun name(): String {
        return "kFloat"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun canDecode(path: String, tags: Tags, configNode:ConfigNode?, klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(Float::class)
        } else {
            false
        }
    }

    override fun leafDecode(path: String?, node: ConfigNode): ValidateOf<Float> {
        val results: ValidateOf<Float>
        val value = node.value.orElse("")
        results = if (StringUtils.isReal(value)) {
            try {
                val floatVal = value.toFloat()
                ValidateOf.valid(floatVal)
            } catch (e: NumberFormatException) {
                ValidateOf.inValid(ValidationError.DecodingNumberFormatException(path, node, name()))
            }
        } else {
            ValidateOf.inValid(ValidationError.DecodingNumberParsing(path, node, name()))
        }
        return results
    }
}
