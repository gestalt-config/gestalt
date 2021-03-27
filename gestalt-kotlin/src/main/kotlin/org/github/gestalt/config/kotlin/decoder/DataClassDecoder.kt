package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.decoder.Decoder
import org.github.gestalt.config.decoder.DecoderService
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.kotlin.entity.DataClassCanNotBeConstructed
import org.github.gestalt.config.kotlin.entity.DataClassHasNoConstructor
import org.github.gestalt.config.kotlin.entity.DataClassMissingRequiredMember
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.node.MapNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.utils.ValidateOf
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Kotlin Data Decoder.
 * Builds a Kotlin Data class by creating a map of parameters. If there are any missing required parameters it fails.
 * Required parameters are ones that don't have a default and are not nullable. An exception will be thrown in this case.
 *
 * <p>If all members are optional and we have no parameters we will try and create the class with the default empty constructor.
 *
 * @author Colin Redmond
 */
class DataClassDecoder : Decoder<Any> {
    override fun name(): String {
        return "DataClass"
    }

    override fun priority(): Priority {
        return Priority.MEDIUM
    }

    override fun matches(klass: TypeCapture<*>): Boolean {
        if (klass is KTypeCapture<*>) {
            val classifier = klass.kType.classifier

            if (classifier is KClass<*>) {
                return classifier.isData
            }
        }

        return false
    }

    override fun decode(path: String, node: ConfigNode, type: TypeCapture<*>, decoderService: DecoderService): ValidateOf<Any> {
        if (node !is MapNode) {
            return ValidateOf.inValid(ValidationError.DecodingExpectedLeafNodeType(path, node, name()))
        }

        if (type is KTypeCapture<*>) {
            val classifier = type.kType.classifier

            if (classifier is KClass<*> && classifier.isData) {
                if (classifier.constructors.isEmpty()) {
                    return ValidateOf.inValid(DataClassHasNoConstructor(path, classifier.simpleName ?: ""))
                }

                val constructor = classifier.constructors.first()

                val errors: MutableList<ValidationError> = mutableListOf()
                val missingMembers: MutableList<String> = mutableListOf()
                val parameters = constructor.parameters
                    .associateWith {
                        val nextPath = if (path.isNotEmpty()) "$path.${it.name}" else it.name

                        val configNode = decoderService.getNextNode(nextPath, it.name, node)
                        errors.addAll(configNode.errors)
                        var results: Any? = null
                        when {
                            !it.isOptional && !configNode.hasResults() -> {
                                missingMembers.add(it.name ?: "")
                            }
                            configNode.hasResults() -> {
                                val parameter = decoderService.decodeNode(nextPath, configNode.results(), KTypeCapture.of<Any>(it.type))
                                if (parameter.hasErrors()) {
                                    errors.addAll(parameter.errors)
                                }

                                if (!parameter.hasResults()) {
                                    errors.add(
                                        ValidationError.NoResultsFoundForNode(nextPath, it.type.classifier.toString(), "data decoding")
                                    )
                                } else {
                                    results = parameter.results()
                                }
                            }
                        }
                        results
                    }
                    .filterValues { it != null }

                return when {
                    missingMembers.isNotEmpty() -> {
                        errors.add(DataClassMissingRequiredMember(path, classifier.simpleName ?: "", missingMembers))
                        ValidateOf.inValid(errors)
                    }
                    parameters.isNotEmpty() -> {
                        ValidateOf.validateOf(constructor.callBy(parameters), errors)
                    }
                    else -> {
                        try {
                            // so try and use the default constructor, it may provide all the default args
                            // if not it will fail constructing.
                            ValidateOf.validateOf(classifier.createInstance(), errors)
                        } catch (e: IllegalArgumentException) {
                            errors.add(DataClassCanNotBeConstructed(path, classifier.simpleName ?: ""))
                            ValidateOf.inValid(errors)
                        }
                    }
                }
            }
        }
        return ValidateOf.inValid(DataClassCanNotBeConstructed(path, ""))
    }
}
