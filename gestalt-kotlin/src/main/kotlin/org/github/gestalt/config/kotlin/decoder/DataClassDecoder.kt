package org.github.gestalt.config.kotlin.decoder

import org.github.gestalt.config.annotations.Config
import org.github.gestalt.config.decoder.Decoder
import org.github.gestalt.config.decoder.DecoderContext
import org.github.gestalt.config.decoder.Priority
import org.github.gestalt.config.entity.ValidationError
import org.github.gestalt.config.entity.ValidationError.OptionalMissingValueDecoding
import org.github.gestalt.config.entity.ValidationLevel
import org.github.gestalt.config.kotlin.entity.DataClassCanNotBeConstructed
import org.github.gestalt.config.kotlin.entity.DataClassHasNoConstructor
import org.github.gestalt.config.kotlin.entity.DataClassMissingRequiredMember
import org.github.gestalt.config.kotlin.reflect.KTypeCapture
import org.github.gestalt.config.node.ConfigNode
import org.github.gestalt.config.node.LeafNode
import org.github.gestalt.config.node.MapNode
import org.github.gestalt.config.reflect.TypeCapture
import org.github.gestalt.config.tag.Tags
import org.github.gestalt.config.utils.GResultOf
import org.github.gestalt.config.utils.PathUtil
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
 * @author <a href="mailto:colin.redmond@outlook.com"> Colin Redmond </a> (c) 2024.
 */
class DataClassDecoder : Decoder<Any> {
    override fun name(): String {
        return "DataClass"
    }

    override fun priority(): Priority {
        return Priority.MEDIUM
    }

    override fun canDecode(path: String, tags: Tags, configNode: ConfigNode?, klass: TypeCapture<*>): Boolean {
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
    ): GResultOf<Any> {
        if (node !is MapNode) {
            return GResultOf.errors(ValidationError.DecodingExpectedMapNodeType(path, node))
        }

        val decoderService = decoderContext.decoderService

        if (type is KTypeCapture<*>) {
            val classifier = type.kType.classifier

            if (classifier is KClass<*> && classifier.isData) {
                if (classifier.constructors.isEmpty()) {
                    return GResultOf.errors(DataClassHasNoConstructor(path, classifier.simpleName ?: ""))
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
                        errors.addAll(configNode.getErrorsNotLevel(ValidationLevel.MISSING_VALUE))
                        var results: Any? = null
                        when {
                            // if we have results for the config node.
                            configNode.hasResults() -> {
                                val parameter = decoderService.decodeNode(
                                    nextPath, tags, configNode.results(),
                                    KTypeCapture.of<Any>(it.type), decoderContext
                                )
                                errors.addAll(parameter.errors)

                                if (parameter.hasResults()) {
                                    results = parameter.results()
                                } else {
                                    missingMembers.add(it.name ?: "null")
                                }
                            }

                            // if we dont have results for the config node, and the default annotation is not blank
                            configAnnotation?.defaultVal?.isNotBlank() ?: false -> {
                                val defaultGResultOf: GResultOf<*> = decoderService.decodeNode(
                                    nextPath,
                                    tags,
                                    LeafNode(configAnnotation?.defaultVal),
                                    KTypeCapture.of<Any>(it.type),
                                    decoderContext
                                )

                                if (defaultGResultOf.hasErrors()) {
                                    errors.addAll(defaultGResultOf.errors)
                                }

                                if (defaultGResultOf.hasResults()) {
                                    results = defaultGResultOf.results()
                                    errors.add(OptionalMissingValueDecoding(nextPath, node, name(), type.rawType.simpleName))
                                } else {
                                    missingMembers.add(it.name ?: "null")
                                }
                            }

                            // if we dont have results for the config node, and the value is not optional
                            !it.isOptional -> {
                                missingMembers.add(it.name ?: "null")
                            }

                            // if we dont have results for the config node, and the value is optional
                            it.isOptional -> {
                                errors.add(OptionalMissingValueDecoding(nextPath, node, name(), type.rawType.simpleName))
                            }

                        }
                        results
                    }
                    .filterValues { it != null }

                return when {
                    missingMembers.isNotEmpty() -> {
                        errors.add(DataClassMissingRequiredMember(path, classifier.simpleName ?: "", missingMembers))
                        GResultOf.errors(errors)
                    }

                    parameters.isNotEmpty() -> {
                        GResultOf.resultOf(constructor.callBy(parameters), errors)
                    }

                    else -> {
                        try {
                            // so try and use the default constructor, it may provide all the default args
                            // if not it will fail constructing.
                            GResultOf.resultOf(classifier.createInstance(), errors)
                        } catch (e: IllegalArgumentException) {
                            errors.add(DataClassCanNotBeConstructed(path, classifier.simpleName ?: ""))
                            GResultOf.errors(errors)
                        }
                    }
                }
            }
        }
        return GResultOf.errors(DataClassCanNotBeConstructed(path, type.name))
    }
}
