package org.github.gestalt.config.kotlin

import org.github.gestalt.config.Gestalt
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import kotlin.reflect.typeOf

/**
 * reified function to get a config, that automatically gets the generic type.
 */
inline fun <reified T> Gestalt.getConfig(path: String): T = this.getConfig(path, KTypeCapture.of<T>(typeOf<T>())) as T

/**
 * reified function to get a config with a default, that automatically gets the generic type.
 */
inline fun <reified T> Gestalt.getConfig(path: String, default: T): T = this.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>())) as T
