package org.github.gestalt.config.kotlin.reflect

import org.github.gestalt.config.reflect.TypeCapture
import java.util.stream.Collectors
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf

/**
 * Kotlin specific TypeCapture
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2025.
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

    override fun hasParameter(): Boolean {
        return kType.arguments.isNotEmpty()
    }

    /**
     * Get the TypeCapture of the first generic parameter or null if there is none.
     *
     * @return the TypeCapture of the first generic parameter or null if there is none.
     */
    override fun getFirstParameterType(): TypeCapture<*>? {
        return if (kType.arguments.isNotEmpty() && kType.arguments[0].type != null) {
            of<Any>(kType.arguments[0].type!!)
        } else {
            null
        }
    }

    /**
     * Get the TypeCapture of the second generic parameter or null if there is none.
     *
     * @return the TypeCapture of the second generic parameter or null if there is none.
     */
    override fun getSecondParameterType(): TypeCapture<*>? {
        if (kType.arguments.isNotEmpty() && (kType.arguments.size > 1 && kType.arguments[1].type != null)) {
            return of<Any>(kType.arguments[1].type!!)
        }
        return null
    }

    /**
     * Get all generic parameter types.
     *
     * @return list of all generic parameter types.
     */
    override fun getParameterTypes(): List<TypeCapture<*>?>? {
        return if (kType.arguments.isNotEmpty()) {
            kType.arguments.stream()
                .filter { it.type != null }
                .map { of<Any>(it.type!!) }
                .collect(Collectors.toList())
        } else {
            null
        }
    }
}

inline fun <reified T> kTypeCaptureOf() = KTypeCapture.of<T>(typeOf<T>())
