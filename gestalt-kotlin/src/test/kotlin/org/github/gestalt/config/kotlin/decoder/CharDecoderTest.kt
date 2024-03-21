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
import org.github.gestalt.config.utils.GResultOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.*

internal class CharDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null
    var decoderService: DecoderRegistry? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
        decoderService = DecoderRegistry(
            listOf(CharDecoder()), configNodeService, lexer, listOf(
                StandardPathMapper(),
                DotNotationPathMapper()
            )
        )
    }

    @Test
    fun name() {
        val decoder = CharDecoder()
        Assertions.assertEquals("kCharacter", decoder.name())
    }

    @Test
    fun canDecode() {
        val decoder = CharDecoder()
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Char>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), object : TypeCapture<Char?>() {}))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), TypeCapture.of(Char::class.java)))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeChar() {
        val decoder = CharDecoder()
        val result: GResultOf<Char> = decoder.decode(
            "db.port", Tags.of(),
            LeafNode("a"),
            TypeCapture.of(
                Char::class.java
            ),
            DecoderContext(decoderService, null, null),
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())
        Assertions.assertEquals('a', result.results())
        Assertions.assertEquals(0, result.errors.size)
    }

    @Test
    @Throws(GestaltException::class)
    fun notACharTooLong() {
        val decoder = CharDecoder()
        val result: GResultOf<Char> = decoder.decode(
            "db.port", Tags.of(),
            LeafNode("aaa"),
            TypeCapture.of(
                Char::class.java
            ),
            DecoderContext(decoderService, null, null),
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertEquals('a', result.results())
        Assertions.assertNotNull(result.errors)
        Assertions.assertEquals(ValidationLevel.WARN, result.errors[0].level())
        Assertions.assertEquals(
            "Expected a char on path: db.port, decoding node: LeafNode{value='aaa'} received the wrong size",
            result.errors[0].description()
        )
    }

    @Test
    @Throws(GestaltException::class)
    fun notACharTooShort() {
        val decoder = CharDecoder()
        val result: GResultOf<Char> = decoder.decode(
            "db.port", Tags.of(),
            LeafNode(""),
            TypeCapture.of(
                Char::class.java
            ),
            DecoderContext(decoderService, null, null),
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertNull(result.results())
        Assertions.assertNotNull(result.errors)
        Assertions.assertEquals(
            "Expected a char on path: db.port, decoding node: LeafNode{value=''} received the wrong size",
            result.errors[0].description()
        )
    }
}
