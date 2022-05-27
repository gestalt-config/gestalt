package org.github.gestalt.config.kotlin.reflect

import org.github.gestalt.config.reflect.TypeCapture
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

/**
 * Kotlin specific TypeCapture
 */
class KTypeCapture<T> private constructor(val kType: KType) : TypeCapture<Any>(kType.javaType) {

    companion object {
        fun <T> of(kType: KType): KTypeCapture<T> {
            return KTypeCapture(kType)
        }
    }

    fun isAssignableFrom(classType: KClass<*>): Boolean {
        val classifier = kType.classifier
        if (classifier is KClass<*>) {
            return classifier.isSuperclassOf(classType)
        }
        return rawType.isAssignableFrom(buildRawType(classType.java))
    }
}

inline fun <reified T> kTypeCaptureOf() = KTypeCapture.of<T>(typeOf<T>())
