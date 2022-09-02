package org.github.gestalt.config.kotlin

import org.github.gestalt.config.Gestalt
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.tag.Tags
import kotlin.reflect.typeOf

/**
 * reified function to get a config, that automatically gets the generic type.
 */
inline fun <reified T> Gestalt.getConfig(path: String): T = this.getConfig(path, KTypeCapture.of<T>(typeOf<T>())) as T

/**
 * reified function to get a config with a default, that automatically gets the generic type.
 */
inline fun <reified T> Gestalt.getConfig(path: String, default: T): T = this.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>())) as T

/**
 * reified function to get a config, that automatically gets the generic type with Tags.
 */
inline fun <reified T> Gestalt.getConfig(path: String, tags: Tags): T = this.getConfig(path, KTypeCapture.of<T>(typeOf<T>()), tags) as T

/**
 * reified function to get a config with a default, that automatically gets the generic type with Tags.
 */
inline fun <reified T> Gestalt.getConfig(path: String, default: T, tags: Tags): T =
    this.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>()), tags) as T
