package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.DecoderRegistry
import org.github.gestalt.config.entity.ValidationLevel
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.reflect.kTypeCaptureOf
import org.github.gestalt.config.lexer.SentenceLexer
import org.github.gestalt.config.node.ConfigNodeService
import org.github.gestalt.config.node.LeafNode
import org.github.gestalt.config.path.mapper.DotNotationPathMapper
import org.github.gestalt.config.path.mapper.StandardPathMapper
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.ValidateOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.nio.charset.Charset
import java.util.*

internal class ByteDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null
    var decoderService: DecoderRegistry? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
        decoderService = DecoderRegistry(
            listOf(ByteDecoder()), configNodeService, lexer, listOf(
                StandardPathMapper(),
                DotNotationPathMapper()
            )
        )
    }

    @Test
    fun name() {
        val decoder = ByteDecoder()
        Assertions.assertEquals("kByte", decoder.name())
    }

    @Test
    fun canDecode() {
        val decoder = ByteDecoder()
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Byte>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), object : TypeCapture<Byte?>() {}))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), TypeCapture.of(Byte::class.java)))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeByte() {
        val decoder = ByteDecoder()
        val validate: ValidateOf<Byte> = decoder.decode(
            "db.port", Tags.of(),
            LeafNode("a"),
            TypeCapture.of(
                Byte::class.java
            ),
            DecoderContext(decoderService, null),
        )
        Assertions.assertTrue(validate.hasResults())
        Assertions.assertFalse(validate.hasErrors())
        Assertions.assertEquals("a".toByteArray(Charset.defaultCharset())[0], validate.results())
        Assertions.assertEquals(0, validate.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun notAByteTooLong() {
        val decoder = ByteDecoder()
        val validate: ValidateOf<Byte> = decoder.decode(
            "db.port", Tags.of(),
            LeafNode("aaa"),
            TypeCapture.of(
                Byte::class.java
            ),
            DecoderContext(decoderService, null),
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.WARN, validate.errors[0].level())
        Assertions.assertEquals(
            "Expected a Byte on path: db.port, decoding node: LeafNode{value='aaa'} received the wrong size",
            validate.errors[0].description()
        )
    }

    @Test
    @Throws(GestaltException::class)
    fun notAByteTooShort() {
        val decoder = ByteDecoder()
        val validate: ValidateOf<Byte> = decoder.decode(
            "db.port", Tags.of(),
            LeafNode(""),
            TypeCapture.of(
                Byte::class.java
            ),
            DecoderContext(decoderService, null),
        )
        Assertions.assertFalse(validate.hasResults())
        Assertions.assertTrue(validate.hasErrors())
        Assertions.assertNull(validate.results())
        Assertions.assertNotNull(validate.errors)
        Assertions.assertEquals(ValidationLevel.WARN, validate.errors[0].level())
        Assertions.assertEquals(
            "Expected a Byte on path: db.port, decoding node: LeafNode{value=''} received the wrong size",
            validate.errors[0].description()
        )
    }
}
