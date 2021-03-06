package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.LeafDecoder
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.utils.ValidateOf

/**
 * Kotlin String Decoder.
 *
 * @author Colin Redmond
 */
class StringDecoder : LeafDecoder<String>() {
    override fun name(): String {
        return "kString"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun matches(klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(String::class)
        } else {
            false
        }
    }

    override fun leafDecode(path: String, node: ConfigNode): ValidateOf<String> {
        return ValidateOf.valid((node.value.orElse("")) as String)
    }
}
