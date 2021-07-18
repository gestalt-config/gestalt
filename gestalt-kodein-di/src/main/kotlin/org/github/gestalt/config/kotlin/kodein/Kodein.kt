package org.github.gestalt.config.kotlin.kodein

import org.github.gestalt.config.Gestalt
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.kodein.di.DirectDIAware
import org.kodein.di.instance
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> DirectDIAware.gestalt(
    path: String,
    default: T? = null,
    gestaltTag: Any? = null
): T {
    val gestalt = this.directDI.instance<Gestalt>(gestaltTag)
    return if (default != null) {
        gestalt.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>())) as T
    } else {
        gestalt.getConfig(path, KTypeCapture.of<T>(typeOf<T>())) as T
    }
}
