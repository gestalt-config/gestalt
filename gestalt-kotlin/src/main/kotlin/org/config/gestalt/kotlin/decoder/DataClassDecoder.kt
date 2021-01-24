package org.config.gestalt.kotlin.decoder

import org.config.gestalt.decoder.Decoder
import org.config.gestalt.decoder.DecoderService
import org.config.gestalt.decoder.Priority
import org.config.gestalt.entity.ValidationError
import org.config.gestalt.kotlin.entity.DataClassCanNotBeConstructed
import org.config.gestalt.kotlin.entity.DataClassHasNoConstructor
import org.config.gestalt.kotlin.entity.DataClassMissingRequiredMember
import org.config.gestalt.kotlin.reflect.KTypeCapture
import org.config.gestalt.node.ConfigNode
import org.config.gestalt.node.MapNode
import org.config.gestalt.reflect.TypeCapture
import org.config.gestalt.utils.ValidateOf
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * Kotlin Data Decoder.
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
                                    errors.add(ValidationError.NoResultsFoundForDecodingNode(nextPath, it.type.classifier.toString()))
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

/*
  // Only support data classes with primary constructor
        val constructor = type.constructors.first()

        val parameters = constructor.parameters
            .associateWith {
                val configParam: Any? = configProvider.getOrNull(config + "." + it.name, it.type.javaType)
                if (!it.isOptional && configParam == null) {
                    throw SmokedMeatException(
                        StatusCode.InternalServerError,
                        "unable to load config for class ${type.simpleName} non-optional parameter ${it.name} " +
                            "and config path $config"
                    )
                }
                configParam
            }
            .filterValues { it != null }

        obj = if (parameters.isNotEmpty()) {
            constructor.callBy(parameters)
        } else {
            // so try and use the default constructor, it may provide all the default args
            // if not it will fail constructing.
            type.createInstance()
        }
 */
