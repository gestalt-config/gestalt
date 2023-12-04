package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.annotations.Config
import org.github.gestalt.config.decoder.Decoder
import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.kotlin.entity.DataClassCanNotBeConstructed
import org.github.gestalt.config.kotlin.entity.DataClassHasNoConstructor
import org.github.gestalt.config.kotlin.entity.DataClassMissingRequiredMember
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.node.LeafNode
import org.github.gestalt.config.node.MapNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.PathUtil
import org.github.gestalt.config.utils.ValidateOf
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaField

/**
 * Kotlin Data Decoder.
 * Builds a Kotlin Data class by creating a map of parameters. If there are any missing required parameters it fails.
 * Required parameters are ones that don't have a default and are not nullable. An exception will be thrown in this case.
 *
 * <p>If all members are optional and we have no parameters we will try and create the class with the default empty constructor.
 *
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2023.
 */
class DataClassDecoder : Decoder<Any> {
    override fun name(): String {
        return "DataClass"
    }

    override fun priority(): Priority {
        return Priority.MEDIUM
    }

    override fun canDecode(path: String, tags: Tags, configNode:ConfigNode?, klass: TypeCapture<*>): Boolean {
        if (klass is KTypeCapture<*>) {
            val classifier = klass.kType.classifier

            if (classifier is KClass<*>) {
                return classifier.isData
            }
        }

        return false
    }

    @Suppress("LongMethod")
    override fun decode(
        path: String,
        tags: Tags,
        node: ConfigNode,
        type: TypeCapture<*>,
        decoderContext: DecoderContext
    ): ValidateOf<Any> {
        if (node !is MapNode) {
            return ValidateOf.inValid(ValidationError.DecodingExpectedMapNodeType(path, node))
        }

        val decoderService = decoderContext.decoderService

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
                        var paramName: String = it.name ?: ""
                        val props = classifier.declaredMemberProperties.firstOrNull { it.name == paramName }


                        // if we have an annotation, use that for the path instead of the name.
                        val configAnnotation: Config? = props?.javaField?.getAnnotation(Config::class.java)
                        if (configAnnotation?.path?.isNotEmpty() == true) {
                            paramName = configAnnotation.path
                        }
                        val nextPath = PathUtil.pathForKey(path, paramName)

                        val configNode = decoderService.getNextNode(nextPath, paramName, node)
                        errors.addAll(configNode.errors)
                        var results: Any? = null
                        when {
                            !configNode.hasResults() && configAnnotation?.defaultVal?.isNotBlank() ?: false -> {
                                val defaultValidateOf: ValidateOf<*> = decoderService.decodeNode(
                                    nextPath,
                                    tags,
                                    LeafNode(configAnnotation?.defaultVal),
                                    KTypeCapture.of<Any>(it.type),
                                    decoderContext
                                )

                                if (defaultValidateOf.hasErrors()) {
                                    errors.addAll(defaultValidateOf.errors)
                                }

                                if (defaultValidateOf.hasResults()) {
                                    results = defaultValidateOf.results()
                                } else {
                                    missingMembers.add(it.name ?: "null")
                                }
                            }

                            !it.isOptional && !configNode.hasResults() -> {
                                missingMembers.add(paramName)

                            }

                            configNode.hasResults() -> {
                                val parameter = decoderService.decodeNode(nextPath, tags, configNode.results(),
                                    KTypeCapture.of<Any>(it.type), decoderContext)
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
        return ValidateOf.inValid(DataClassCanNotBeConstructed(path, type.name))
    }
}
