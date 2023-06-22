package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderRegistry
import org.github.gestalt.config.entity.ValidationLevel
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.reflect.kTypeCaptureOf
import org.github.gestalt.config.lexer.SentenceLexer
import org.github.gestalt.config.node.ConfigNodeService
import org.github.gestalt.config.node.LeafNode
import org.github.gestalt.config.node.MapNode
import org.github.gestalt.config.path.mapper.DotNotationPathMapper
import org.github.gestalt.config.path.mapper.StandardPathMapper
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.utils.ValidateOf
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
        Assertions.assertEquals("kString", decoder.name())
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
            DecoderRegistry(
                listOf(stringDecoder), configNodeService, lexer, listOf(
                    StandardPathMapper(),
                    DotNotationPathMapper()
                )
            )
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertEquals("test", validate.results())
        Assertions.assertEquals(0, validate.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun `invalid Leaf Node`() {
        val stringDecoder = StringDecoder()
        val validate: ValidateOf<String> = stringDecoder.decode(
            "db.user", LeafNode(null), TypeCapture.of(
                String::class.java
            ),
            DecoderRegistry(
                listOf(stringDecoder), configNodeService, lexer, listOf(
                    StandardPathMapper(),
                    DotNotationPathMapper()
                )
            )
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, validate.errors[0].level())
        Assertions.assertEquals(
            "Leaf on path: db.user, has no value attempting to decode kString",
            validate.errors[0].description()
        )
    }

    @Test
    @Throws(GestaltException::class)
    fun `decode Invalid Node`() {
        val stringDecoder = StringDecoder()
        val validate: ValidateOf<String> = stringDecoder.decode(
            "db.user", MapNode(HashMap()), TypeCapture.of(
                String::class.java
            ),
            DecoderRegistry(
                listOf(stringDecoder), configNodeService, lexer, listOf(
                    StandardPathMapper(),
                    DotNotationPathMapper()
                )
            )
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, validate.errors[0].level())
        Assertions.assertEquals(
            "Expected a leaf on path: db.user, received node type: map, attempting to decode kString",
            validate.errors[0].description()
        )
    }
}
