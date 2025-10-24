package org.github.gestalt.config.kotlin

import org.github.gestalt.config.Gestalt
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.tag.Tags
import kotlin.reflect.typeOf

/**
 * reified function to get a config, that automatically gets the generic type.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
 */
inline fun <reified T> Gestalt.getConfig(path: String, tags: Tags = Tags.of()): T {
    val type = typeOf<T>()
    return if (!type.isMarkedNullable) {
        this.getConfig(path, KTypeCapture.of<T>(type), tags) as T
    } else {
        val optional = this.getConfigOptional(path, KTypeCapture.of<T>(type), tags)
        if (optional.isPresent) {
            optional.get() as T
        } else {
            return null as T
        }
    }
}


/**
 * reified function to get a config with a default, that automatically gets the generic type.
 */
inline fun <reified T> Gestalt.getConfig(path: String, default: T, tags: Tags = Tags.of()): T =
    this.getConfig(path, default, KTypeCapture.of<T>(typeOf<T>()), tags) as T

inline fun <reified T> Gestalt.getConfigResult(path: String, tags: Tags = Tags.of()) =
    this.getConfigResult(path, KTypeCapture.of<T>(typeOf<T>()), tags)
