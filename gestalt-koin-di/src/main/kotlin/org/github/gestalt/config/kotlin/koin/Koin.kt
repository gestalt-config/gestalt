package org.github.gestalt.config.kotlin.koin

import org.github.gestalt.config.Gestalt
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> Scope.gestalt(
    path: String,
    default: T? = null,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T {
    val gestalt = this.get<Gestalt>(qualifier, parameters)
    return if (default != null) {
        gestalt.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>())) as T
    } else {
        gestalt.getConfig(path, KTypeCapture.of<T>(typeOf<T>())) as T
    }
}
