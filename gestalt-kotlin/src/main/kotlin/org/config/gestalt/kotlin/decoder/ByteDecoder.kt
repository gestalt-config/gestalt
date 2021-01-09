package org.config.gestalt.kotlin.decoder

import org.config.gestalt.decoder.LeafDecoder
import org.config.gestalt.decoder.Priority
import org.config.gestalt.entity.ValidationError
import org.config.gestalt.kotlin.reflect.KTypeCapture
import org.config.gestalt.node.ConfigNode
import org.config.gestalt.reflect.TypeCapture
import org.config.gestalt.utils.ValidateOf
import java.nio.charset.Charset

class ByteDecoder : LeafDecoder<Byte>() {
    override fun name(): String {
        return "Byte"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun matches(klass: TypeCapture<*>): Boolean {
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
