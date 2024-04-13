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

internal class LongDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null
    var decoderService: DecoderRegistry? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
        decoderService = DecoderRegistry(
            listOf(LongDecoder()), configNodeService, lexer, listOf(
                StandardPathMapper(),
                DotNotationPathMapper()
            )
        )
    }

    @Test
    fun name() {
        val decoder = LongDecoder()
        Assertions.assertEquals("kLong", decoder.name())
    }

    @Test
    fun canDecode() {
        val decoder = LongDecoder()
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Long>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), object : TypeCapture<Long?>() {}))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), TypeCapture.of(Long::class.java)))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decode() {
        val longDecoder = LongDecoder()
        val result: GResultOf<Long> = longDecoder.decode(
            "db.port", Tags.of(),
            LeafNode("124"),
            TypeCapture.of(
                Long::class.java
            ),
            DecoderContext(decoderService, null, null, PathLexer()),
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())
        Assertions.assertEquals(124L, result.results())
        Assertions.assertEquals(0, result.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun notALong() {
        val longDecoder = LongDecoder()
        val result: GResultOf<Long> = longDecoder.decode(
            "db.port", Tags.of(),
            LeafNode("12s4"),
            TypeCapture.of(
                Long::class.java
            ),
            DecoderContext(decoderService, null, null, PathLexer()),
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertNull(result.results())
        Assertions.assertNotNull(result.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, result.errors[0].level())
        Assertions.assertEquals(
            "Unable to parse a number on Path: db.port, from node: " +
                "LeafNode{value='12s4'} attempting to decode kLong",
            result.errors[0].description()
        )
    }

    @Test
    @Throws(GestaltException::class)
    fun notALongTooLarge() {
        val decoder = LongDecoder()
        val result: GResultOf<Long> = decoder.decode(
            "db.port", Tags.of(),
            LeafNode("12345678901234567890123456789012345678901234567890123456"),
            TypeCapture.of(Long::class.java),
            DecoderContext(decoderService, null, null, PathLexer()),
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertNull(result.results())
        Assertions.assertNotNull(result.errors)
        Assertions.assertEquals(ValidationLevel.ERROR, result.errors[0].level())
        Assertions.assertEquals(
            "Unable to decode a number on path: db.port, from node: " +
                "LeafNode{value='12345678901234567890123456789012345678901234567890123456'} attempting to decode kLong",
            result.errors[0].description()
        )
    }
}
