package org.config.gestalt.kotlin.decoder

import org.config.gestalt.decoder.LeafDecoder
import org.config.gestalt.decoder.Priority
import org.config.gestalt.entity.ValidationError
import org.config.gestalt.kotlin.reflect.KTypeCapture
import org.config.gestalt.node.ConfigNode
import org.config.gestalt.reflect.TypeCapture
import org.config.gestalt.utils.ValidateOf

class CharDecoder : LeafDecoder<Char>() {
    override fun name(): String {
        return "kCharacter"
    }

    override fun priority(): Priority {
        return Priority.HIGH
    }

    override fun matches(klass: TypeCapture<*>): Boolean {
        return if (klass is KTypeCapture<*>) {
            klass.isAssignableFrom(Char::class)
        } else {
            false
        }
    }

    override fun leafDecode(path: String?, node: ConfigNode): ValidateOf<Char> {
        var results: Char? = null
        val error: MutableList<ValidationError> = ArrayList()
        val value = node.value.orElse("")
        if (value.isNotEmpty()) {
            results = value[0]
        }
        if (value.length != 1) {
            error.add(ValidationError.DecodingCharWrongSize(path, node))
        }
        return ValidateOf.validateOf(results, error)
    }
}
