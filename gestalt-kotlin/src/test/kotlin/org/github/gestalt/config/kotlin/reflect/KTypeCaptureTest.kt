package org.github.gestalt.config.kotlin.reflect

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.reflect.KClass

internal class KTypeCaptureTest {

    @Test
    fun getKlass() {
        val type = kTypeCaptureOf<List<String>>()

        assertTrue(type.hasParameter())
        assertEquals("java.util.List<java.lang.String>", type.name)
        assertEquals(String::class.java, type.firstParameterType!!.rawType)
        assertFalse(type.isAssignableFrom(Int::class.java))
        assertFalse(type.isAssignableFrom(Double::class.java))
        assertTrue(type.isAssignableFrom(MutableList::class.java))
        assertFalse(type.isAssignableFrom(DataClass::class.java))
        assertFalse((type.kType.classifier as KClass<*>).isData)
    }

    data class DataClass(val data1: String, val data2: Int)

    @Test
    fun getKlass2() {
        val type: KTypeCapture<DataClass> = kTypeCaptureOf()

        assertFalse(type.hasParameter())
        assertEquals("org.github.gestalt.config.kotlin.reflect.KTypeCaptureTest\$DataClass", type.name)
        assertFalse(type.isAssignableFrom(Int::class.java))
        assertFalse(type.isAssignableFrom(Double::class.java))
        assertFalse(type.isAssignableFrom(MutableList::class.java))
        assertTrue(type.isAssignableFrom(DataClass::class.java))
        assertTrue((type.kType.classifier as KClass<*>).isData)
    }

    @Test
    fun getKlassJavaInteger() {
        val type: KTypeCapture<Integer> = kTypeCaptureOf()

        assertFalse(type.hasParameter())
        assertEquals("int", type.name)
        assertTrue(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(Double::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertFalse(type.isAssignableFrom(DataClass::class))
        assertTrue(type.isAssignableFrom(Int::class.java))
        assertFalse(type.isAssignableFrom(Double::class.java))
        assertFalse(type.isAssignableFrom(MutableList::class.java))
        assertFalse(type.isAssignableFrom(DataClass::class.java))
        assertFalse((type.kType.classifier as KClass<*>).isData)
    }

    @Test
    fun getKlassString() {
        val type: KTypeCapture<String> = kTypeCaptureOf()

        assertFalse(type.hasParameter())
        assertEquals("java.lang.String", type.name)
        assertTrue(type.isAssignableFrom(String::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(Double::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertFalse(type.isAssignableFrom(DataClass::class))
        assertFalse((type.kType.classifier as KClass<*>).isData)
    }

    @Test
    fun getKlassBoolean() {
        val type: KTypeCapture<Boolean> = kTypeCaptureOf()

        assertFalse(type.hasParameter())
        assertEquals("boolean", type.name)
        assertTrue(type.isAssignableFrom(Boolean::class))
        assertTrue(type.isAssignableFrom(Boolean::class.java))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(Double::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertFalse(type.isAssignableFrom(DataClass::class))
        assertFalse((type.kType.classifier as KClass<*>).isData)
    }

    @Test
    fun isAssignableFromDate() {
        val type = kTypeCaptureOf<Date>()
        assertFalse(type.hasParameter())
        assertNull(type.firstParameterType)
        assertEquals("java.util.Date", type.name)
        assertTrue(type.isAssignableFrom(Date::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
    }

    @Test
    fun isAssignableFromHolder() {
        val type = kTypeCaptureOf<Holder<Int>>()
        assertTrue(type.hasParameter())

        // assertEquals(java.lang.Integer::class, type.firstParameterType.type)
        assertEquals(
            "org.github.gestalt.config.kotlin.reflect.KTypeCaptureTest\$Holder<java.lang.Integer>",
            type.name
        )
        assertTrue(type.isAssignableFrom(Holder::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertTrue(type.isAssignableFrom(Holder::class.java))
        assertFalse(type.isAssignableFrom(Int::class.java))
        assertFalse(type.isAssignableFrom(MutableList::class.java))
    }

    @Test
    fun isAssignableFromHolder2() {
        val type: KTypeCapture<Holder<Int>> = kTypeCaptureOf<Holder<Int>>()
        assertTrue(type.hasParameter())

        // assertEquals(java.lang.Integer::class, type.firstParameterType.type)
        assertEquals(
            "org.github.gestalt.config.kotlin.reflect.KTypeCaptureTest\$Holder<java.lang.Integer>",
            type.name
        )
        assertTrue(type.isAssignableFrom(Holder::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertTrue(type.isAssignableFrom(Holder::class.java))
        assertFalse(type.isAssignableFrom(Int::class.java))
        assertFalse(type.isAssignableFrom(MutableList::class.java))
    }

    @ExperimentalStdlibApi
    @Test
    fun isAssignableFromBaseClass() {
        val type = kTypeCaptureOf<BaseClass>()
        assertFalse(type.hasParameter())

        assertEquals("org.github.gestalt.config.kotlin.reflect.KTypeCaptureTest\$BaseClass", type.name)
        assertTrue(type.isAssignableFrom(BaseClass::class))
        assertTrue(type.isAssignableFrom(InheritedClass::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertFalse(type.isAssignableFrom(Holder::class.java))
        assertFalse(type.isAssignableFrom(Int::class.java))
        assertFalse(type.isAssignableFrom(MutableList::class.java))
    }

    @Test
    fun isAssignableFromInheritedClass() {
        val type = kTypeCaptureOf<InheritedClass>()
        assertFalse(type.hasParameter())

        // assertEquals(java.lang.Integer::class, type.firstParameterType.type)
        assertEquals("org.github.gestalt.config.kotlin.reflect.KTypeCaptureTest\$InheritedClass", type.name)
        assertFalse(type.isAssignableFrom(BaseClass::class))
        assertTrue(type.isAssignableFrom(InheritedClass::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertFalse(type.isAssignableFrom(Holder::class.java))
        assertFalse(type.isAssignableFrom(Int::class.java))
        assertFalse(type.isAssignableFrom(MutableList::class.java))
    }

    @Test
    fun isAssignableArray() {
        val type = kTypeCaptureOf<IntArray>()
        assertTrue(type.isAssignableFrom(IntArray::class))

        assertFalse(type.hasParameter())
        assertNull(type.firstParameterType)
        assertEquals("int[]", type.name)
        assertFalse(type.isAssignableFrom(Holder::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertTrue(type.isAssignableFrom(IntArray::class))
    }

    @Test
    fun isAssignableGenericArray() {
        val type = kTypeCaptureOf<Array<Any>>()
        assertTrue(type.hasParameter())
        assertNotNull(type.firstParameterType)
        assertEquals("java.lang.Object[]", type.name)
        assertFalse(type.isAssignableFrom(Holder::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertTrue(type.isAssignableFrom(Array<Any>::class))
    }

    @Test
    fun isAssignableGenericArrayStar() {
        val type = kTypeCaptureOf<Array<*>>()
        assertTrue(type.hasParameter())
        assertNull(type.firstParameterType)
        assertEquals("java.lang.Object[]", type.name)
        assertFalse(type.isAssignableFrom(Holder::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertFalse(type.isAssignableFrom(MutableList::class))
        assertTrue(type.isAssignableFrom(Array<Any>::class))
    }

    @Test
    fun isAssignableGenericArray2() {
        val type = kTypeCaptureOf<List<Any>>()
        assertTrue(type.hasParameter())
        assertEquals(Any::class.java, type.firstParameterType!!.rawType)
        assertFalse(type.isAssignableFrom(Holder::class))
        assertFalse(type.isAssignableFrom(Int::class))
        assertTrue(type.isAssignableFrom(MutableList::class))
        assertFalse(type.isAssignableFrom(Array<Int>::class))
    }

    @Test
    fun getSecondParameterTypeTest() {
        val type = kTypeCaptureOf<Map<String, Int>>()
        assertTrue(type.hasParameter())
        assertNotNull(type.secondParameterType)
        assertEquals(Int::class.java, type.secondParameterType!!.rawType)
    }

    @Test
    fun getSecondParameterTypeTestNoType() {
        val type = kTypeCaptureOf<String>()
        assertFalse(type.hasParameter())
        assertNull(type.secondParameterType)
    }

    @Test
    fun getParameterTypesTest() {
        val type = kTypeCaptureOf<Map<String, Int>>()
        assertTrue(type.hasParameter())
        val params = type.parameterTypes
        assertNotNull(params)
        assertEquals(2, params!!.size)
        assertEquals(String::class.java, params[0]?.rawType)
        assertEquals(Int::class.java, params[1]?.rawType)
    }

    @Test
    fun getParameterTypesTestNoType() {
        val type = kTypeCaptureOf<String>()
        assertFalse(type.hasParameter())
        assertNull(type.parameterTypes)
    }


    class Holder<T> {
        var value: T? = null
    }

    open class BaseClass {
        var data1: String = "100"
    }

    class InheritedClass : BaseClass() {
        var data2: Int = 100
    }
}
