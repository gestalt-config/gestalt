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
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T {
    val gestalt = this.get<Gestalt>(qualifier, parameters)
    return gestalt.getConfig(path, KTypeCapture.of<T>(typeOf<T>())) as T
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> Scope.gestaltDefault(
    path: String,
    default: T,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T {
    val gestalt = this.get<Gestalt>(qualifier, parameters)
    return gestalt.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>())) as T
}
