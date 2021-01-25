package org.config.gestalt.kotlin.decoder

import org.config.gestalt.decoder.LeafDecoder
import org.config.gestalt.decoder.Priority
import org.config.gestalt.kotlin.reflect.KTypeCapture
import org.config.gestalt.node.ConfigNode
import org.config.gestalt.reflect.TypeCapture
import org.config.gestalt.utils.ValidateOf

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
