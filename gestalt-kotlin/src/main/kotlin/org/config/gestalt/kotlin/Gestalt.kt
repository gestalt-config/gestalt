package org.config.gestalt.kotlin

import org.config.gestalt.Gestalt
import org.config.gestalt.builder.GestaltBuilder
import org.config.gestalt.kotlin.decoder.*
import org.config.gestalt.kotlin.reflect.KTypeCapture
import kotlin.reflect.typeOf

/**
 * reified function to get a config, that automatically gets the generic type.
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> Gestalt.getConfig(path: String): T = this.getConfig(path, KTypeCapture.of<T>(typeOf<T>())) as T

/**
 * reified function to get a config with a default, that automatically gets the generic type.
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> Gestalt.getConfig(path: String, default: T): T = this.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>())) as T

/**
 * Extension function for the GestaltBuilder that adds add kotlin decoders, and the default java ones.
 */
fun GestaltBuilder.addDefaultDecodersAndKotlin(): GestaltBuilder {
    val decoders = listOf(
        BooleanDecoder(), ByteDecoder(), CharDecoder(), DataClassDecoder(), DoubleDecoder(), FloatDecoder(), IntegerDecoder(),
        LongDecoder(), ShortDecoder(), StringDecoder()
    )

    return this.addDecoders(decoders).addDefaultDecoders()
}
