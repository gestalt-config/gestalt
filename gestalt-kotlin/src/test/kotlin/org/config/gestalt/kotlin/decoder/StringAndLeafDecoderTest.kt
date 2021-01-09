package org.config.gestalt.kotlin.decoder

import org.config.gestalt.decoder.DecoderRegistry
import org.config.gestalt.entity.ValidationLevel
import org.config.gestalt.exceptions.GestaltException
import org.config.gestalt.kotlin.reflect.kTypeCaptureOf
import org.config.gestalt.lexer.SentenceLexer
import org.config.gestalt.node.ConfigNodeService
import org.config.gestalt.node.LeafNode
import org.config.gestalt.node.MapNode
import org.config.gestalt.reflect.TypeCapture
import org.config.gestalt.utils.ValidateOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

internal class StringAndLeafDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
    }

    @Test
    fun name() {
        val decoder = StringDecoder()
        Assertions.assertEquals("String", decoder.name())
    }

    @Test
    fun matches() {
        val decoder = StringDecoder()
        Assertions.assertTrue(decoder.matches(kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.matches(object : TypeCapture<String?>() {}))
        Assertions.assertFalse(decoder.matches(TypeCapture.of(String::class.java)))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.matches(kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decode() {
        val stringDecoder = StringDecoder()
        val validate: ValidateOf<String> = stringDecoder.decode(
            "db.user", LeafNode("test"), TypeCapture.of(
                String::class.java
            ),
            DecoderRegistry(listOf(stringDecoder), configNodeService, lexer)
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertEquals("test", validate.results())
        Assertions.assertEquals(0, validate.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun invalidLeafNode() {
        val stringDecoder = StringDecoder()
        val validate: ValidateOf<String> = stringDecoder.decode(
            "db.user", LeafNode(null), TypeCapture.of(
                String::class.java
            ),
            DecoderRegistry(listOf(stringDecoder), configNodeService, lexer)
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, validate.errors[0].level())
        Assertions.assertEquals(
            "Leaf on path: db.user, missing value, LeafNode{value='null'} attempting to decode String",
            validate.errors[0].description()
        )
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeInvalidNode() {
        val stringDecoder = StringDecoder()
        val validate: ValidateOf<String> = stringDecoder.decode(
            "db.user", MapNode(HashMap()), TypeCapture.of(
                String::class.java
            ),
            DecoderRegistry(listOf(stringDecoder), configNodeService, lexer)
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, validate.errors[0].level())
        Assertions.assertEquals(
            "Expected a leaf on path: db.user, received node type, received: MapNode{mapNode={}} " +
                "attempting to decode String",
            validate.errors[0].description()
        )
    }
}
