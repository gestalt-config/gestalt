package org.config.gestalt.kotlin

import org.config.gestalt.Gestalt
import org.config.gestalt.builder.GestaltBuilder
import org.config.gestalt.kotlin.decoder.*
import org.config.gestalt.kotlin.reflect.KTypeCapture
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> Gestalt.getConfig(path: String): T = this.getConfig(path, KTypeCapture.of<T>(typeOf<T>())) as T

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T> Gestalt.getConfig(path: String, default: T): T = this.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>())) as T

fun GestaltBuilder.addDefaultDecodersAndKotlin(): GestaltBuilder {
    val decoders = listOf(
        BooleanDecoder(), ByteDecoder(), CharDecoder(), DataClassDecoder(), DoubleDecoder(), FloatDecoder(), IntegerDecoder(),
        LongDecoder(), ShortDecoder(), StringDecoder()
    )

    return this.addDecoders(decoders).addDefaultDecoders()
}
