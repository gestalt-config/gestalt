package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.LeafDecoder
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.StringUtils
import org.github.gestalt.config.utils.GResultOf

/**
 * Kotlin Long Decoder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
class LongDecoder : LeafDecoder<Long>() {
    override fun name(): String {
        return "kLong"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun canDecode(path: String, tags: Tags, configNode:ConfigNode?, klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(Long::class)
        } else {
            false
        }
    }

    override fun leafDecode(
        path: String?,
        node: ConfigNode,
        decoderContext: DecoderContext
    ): GResultOf<Long> {
        val results: GResultOf<Long>
        val value = node.value.orElse("")
        results = if (StringUtils.isInteger(value)) {
            try {
                val longVal = value.toLong()
                GResultOf.result(longVal)
            } catch (e: NumberFormatException) {
                GResultOf.errors(ValidationError.DecodingNumberFormatException(path, node, name(), decoderContext))
            }
        } else {
            GResultOf.errors(ValidationError.DecodingNumberParsing(path, node, name()))
        }
        return results
    }
}
