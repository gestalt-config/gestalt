package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.LeafDecoder
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.ValidateOf
import java.nio.charset.Charset

/**
 * Kotlin Byte Decoder.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
class ByteDecoder : LeafDecoder<Byte>() {
    override fun name(): String {
        return "kByte"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun canDecode(path: String, tags: Tags, configNode:ConfigNode?, klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(Byte::class)
        } else {
            false
        }
    }

    override fun leafDecode(path: String?, node: ConfigNode): ValidateOf<Byte> {
        val results: ValidateOf<Byte>
        val value = node.value.orElse("")
        results = if (value.length == 1) {
            ValidateOf.valid(value.toByteArray(Charset.defaultCharset())[0])
        } else {
            ValidateOf.inValid(
                ValidationError.DecodingByteTooLong(
                    path,
                    node
                )
            )
        }
        return results
    }
}
