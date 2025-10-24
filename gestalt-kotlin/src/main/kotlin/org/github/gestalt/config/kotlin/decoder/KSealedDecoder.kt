package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.Decoder
import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.entity.ValidationLevel
import org.github.gestalt.config.kotlin.entity.SealedClassCanNotBeConstructed
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.GResultOf
import kotlin.math.abs
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

@Suppress("UNCHECKED_CAST")
class KSealedDecoder : Decoder<Any> {

    override fun priority(): Priority = Priority.MEDIUM

    override fun name(): String = "kSealed"

    override fun canDecode(path: String, tags: Tags, node: ConfigNode?, klass: TypeCapture<*>): Boolean {
        if (klass is KTypeCapture<*>) {
            val classifier = klass.kType.classifier

            if (classifier is KClass<*>) {
                return classifier.isSealed
            }
        }

        return false
    }

    @Suppress("MagicNumber", "ReturnCount")
    override fun decode(
        path: String,
        tags: Tags,
        node: ConfigNode?,
        klass: TypeCapture<*>,
        decoderContext: DecoderContext
    ): GResultOf<Any> {
        if (klass is KTypeCapture<*>) {
            val classifier = klass.kType.classifier
            val decoderService = decoderContext.decoderService

            if (classifier is KClass<*> && classifier.isSealed) {

                val candidates: List<KClass<*>> = classifier.sealedSubclasses
                if (candidates.isEmpty()) {
                    return GResultOf.errors(ValidationError.NoPermittedClassesInSealedClass(klass.name, path))
                }

                var best: GResultOf<Any>? = null
                var bestScore = Int.MAX_VALUE

                for (cand in candidates) {
                    val candidateDecoded =
                        decoderService
                            .decodeNode(path, tags, node, KTypeCapture.of<Any>(cand.createType()), decoderContext) as GResultOf<Any>

                    if (!candidateDecoded.hasResults() && bestScore < Int.MAX_VALUE) {
                        best = candidateDecoded
                        continue
                    } else if (!candidateDecoded.hasResults()) {
                        continue
                    }

                    val errorScore = candidateDecoded.errors.map { err ->
                        when (err.level()) {
                            ValidationLevel.ERROR -> 5
                            ValidationLevel.MISSING_VALUE -> 4
                            ValidationLevel.MISSING_OPTIONAL_VALUE -> 3
                            ValidationLevel.WARN -> 2
                            ValidationLevel.DEBUG -> 1
                            else -> 0
                        }
                    }.sum()

                    var fieldCount = 0
                    var currentClass: Class<*>? = candidateDecoded.results().javaClass
                    while (currentClass != null) {
                        fieldCount += currentClass.declaredFields.size
                        currentClass = currentClass.superclass
                    }

                    val score = errorScore + abs((node?.size() ?: 0) - fieldCount) * 2

                    if (score < bestScore) {
                        best = candidateDecoded
                        bestScore = score
                    }
                }

                return best ?: GResultOf.errors(ValidationError.NoPermittedClassesInSealedClass(klass.name, path))
            }
        }
        return GResultOf.errors(SealedClassCanNotBeConstructed(klass.name, path))
    }
}
