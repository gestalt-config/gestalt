package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.DecoderRegistry
import org.github.gestalt.config.entity.ValidationLevel
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
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
import kotlin.reflect.typeOf

internal class BooleanDecoderTest {
    var configNodeService: ConfigNodeService? = null
    var lexer: SentenceLexer? = null
    var decoderService: DecoderRegistry? = null

    @BeforeEach
    fun setup() {
        configNodeService = Mockito.mock(ConfigNodeService::class.java)
        lexer = Mockito.mock(SentenceLexer::class.java)
        decoderService = DecoderRegistry(
            listOf(BooleanDecoder()), configNodeService, lexer, listOf(
                StandardPathMapper(),
                DotNotationPathMapper()
            )
        )
    }

    @Test
    fun name() {
        val decoder = BooleanDecoder()
        Assertions.assertEquals("kBoolean", decoder.name())
    }

    @ExperimentalStdlibApi
    @Test
    fun canDecode() {
        val decoder = BooleanDecoder()
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Boolean>()))
        Assertions.assertTrue(decoder.canDecode("", Tags.of(), LeafNode(""), KTypeCapture.of<Boolean>(typeOf<Boolean>())))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), object : TypeCapture<Boolean?>() {}))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), TypeCapture.of(Boolean::class.java)))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Int>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<String>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<Date>()))
        Assertions.assertFalse(decoder.canDecode("", Tags.of(), LeafNode(""), kTypeCaptureOf<List<Byte>>()))
    }

    @Test
    @Throws(GestaltException::class)
    fun decode() {
        val decoder = BooleanDecoder()
        val result: GResultOf<Boolean> = decoder.decode(
            "db.enabled", Tags.of(),
            LeafNode("true"),
            TypeCapture.of(
                Int::class.java
            ),
            DecoderContext(decoderService, null, null),
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())
        Assertions.assertTrue(result.results())
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeFalse() {
        val decoder = BooleanDecoder()
        val result: GResultOf<Boolean> = decoder.decode(
            "db.enabled", Tags.of(),
            LeafNode("false"),
            TypeCapture.of(
                Int::class.java
            ),
            DecoderContext(decoderService, null, null),
        )
        Assertions.assertTrue(result.hasResults())
        Assertions.assertFalse(result.hasErrors())
        Assertions.assertFalse(result.results())
    }

    @Test
    @Throws(GestaltException::class)
    fun decodeFalseNull() {
        val decoder = BooleanDecoder()
        val result: GResultOf<Boolean> = decoder.decode(
            "db.enabled", Tags.of(),
            LeafNode(null),
            TypeCapture.of(
                Int::class.java
            ),
            DecoderContext(decoderService, null, null),
        )
        Assertions.assertFalse(result.hasResults())
        Assertions.assertTrue(result.hasErrors())
        Assertions.assertNull(result.results())
        Assertions.assertNotNull(result.errors)
        Assertions.assertEquals(ValidationLevel.MISSING_VALUE, result.errors[0].level())
        Assertions.assertEquals(
            "Leaf on path: db.enabled, has no value attempting to decode kBoolean",
            result.errors[0].description()
        )
    }
}
