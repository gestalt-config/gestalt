package org.github.gestalt.config.kotlin.koin

import org.github.gestalt.config.Gestalt
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.kodein.di.DirectDIAware
import org.kodein.di.instance
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> DirectDIAware.gestalt(path: String, gestaltTag: Any? = null): T {
    val gestalt = this.directDI.instance<Gestalt>(gestaltTag)
    return gestalt.getConfig(path, KTypeCapture.of<T>(typeOf<T>())) as T
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> DirectDIAware.gestaltDefault(path: String, default: T, gestaltTag: Any? = null): T {
    val gestalt = this.directDI.instance<Gestalt>(gestaltTag)
    return gestalt.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>())) as T
}
