package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.DecoderRegistry
import org.github.gestalt.config.entity.ValidationLevel
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.reflect.kTypeCaptureOf
import org.github.gestalt.config.lexer.PathLexer
import org.github.gestalt.config.lexer.SentenceLexer
import org.github.gestalt.config.node.ConfigNodeService
import org.github.gestalt.config.node.LeafNode
import org.github.gestalt.config.path.mapper.DotNotationPathMapper
import org.github.gestalt.config.path.mapper.StandardPathMapper
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.GResultOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
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
    fun decodeNumericByte() {
        val decoder = ByteDecoder()

        for (value in listOf("5", "12", "-5", "127", "-128")) {
            val result: GResultOf<Byte> = decoder.decode(
                "db.port", Tags.of(),
                LeafNode(value),
                TypeCapture.of(Byte::class.java),
                DecoderContext(decoderService, null, null, PathLexer()),
            )
            Assertions.assertTrue(result.hasResults(), value)
            Assertions.assertFalse(result.hasErrors(), value)
            Assertions.assertEquals(value.toByte(), result.results(), value)
        }
    }

    @Test
    @Throws(GestaltException::class)
    fun byteOutsideRange() {
        val decoder = ByteDecoder()
        for (value in listOf("128", "-129")) {
            val result: GResultOf<Byte> = decoder.decode(
                "db.port", Tags.of(),
                LeafNode(value),
                TypeCapture.of(Byte::class.java),
                DecoderContext(decoderService, null, null, PathLexer()),
            )
            Assertions.assertFalse(result.hasResults(), value)
            Assertions.assertTrue(result.hasErrors(), value)
            Assertions.assertNull(result.results(), value)
            Assertions.assertNotNull(result.errors, value)
            Assertions.assertEquals(ValidationLevel.ERROR, result.errors[0].level(), value)
            Assertions.assertEquals(
                "Unable to decode a number on path: db.port, from node: LeafNode{value='$value'} attempting to decode kByte",
                result.errors[0].description(),
                value
            )
        }
    }

    @Test
    @Throws(GestaltException::class)
    fun nonNumericByte() {
        val decoder = ByteDecoder()
        val result: GResultOf<Byte> = decoder.decode(
            "db.port", Tags.of(),
            LeafNode("a"),
            TypeCapture.of(Byte::class.java),
            DecoderContext(decoderService, null, null, PathLexer()),
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertNull(result.results())
        Assertions.assertNotNull(result.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, result.errors[0].level())
        Assertions.assertEquals(
            "Unable to parse a number on Path: db.port, from node: LeafNode{value='a'} attempting to decode kByte",
            result.errors[0].description()
        )
    }
}
