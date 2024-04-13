package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.LeafDecoder
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.GResultOf

/**
 * Kotlin Char Decoder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
class CharDecoder : LeafDecoder<Char>() {
    override fun name(): String {
        return "kCharacter"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun canDecode(path: String, tags: Tags, configNode:ConfigNode?, klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(Char::class)
        } else {
            false
        }
    }

    override fun leafDecode(
        path: String?,
        node: ConfigNode,
        decoderContext: DecoderContext
    ): GResultOf<Char> {
        var results: Char? = null
        val error: MutableList<ValidationError> = ArrayList()
        val value = node.value.orElse("")
        if (value.isNotEmpty()) {
            results = value[0]
        }
        if (value.length != 1) {
            error.add(ValidationError.DecodingCharWrongSize(path, node, decoderContext))
        }
        return GResultOf.resultOf(results, error)
    }
}
