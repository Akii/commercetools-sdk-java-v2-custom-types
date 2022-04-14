package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.custom_object.CustomObject
import com.commercetools.api.models.custom_object.CustomObjectImpl
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*

fun typedCustomObjectDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedCustomObjectsDeserializer(config).className)
        .addAnnotation(generated)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(CustomObject::class.asTypeName()))
        .addFunction(deserialize(config))
        .addFunction(makeParser)
        .build()

fun typedCustomObjectsBeanDeserializerModifier(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedCustomObjectsBeanDeserializerModifier(config).className)
        .addAnnotation(generated)
        .superclass(BeanDeserializerModifier::class)
        .addFunction(FunSpec
            .builder("modifyDeserializer")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("config", DeserializationConfig::class.asTypeName().copy(nullable = true))
            .addParameter("beanDesc", BeanDescription::class.asTypeName().copy(nullable = true))
            .addParameter("deserializer", jsonDeserializerType)
            .addCode("""
                    return if (beanDesc?.type?.isTypeOrSubTypeOf(%1T::class.java) == true)
                        super.modifyDeserializer(config, beanDesc, %2T(deserializer))
                    else
                        super.modifyDeserializer(config, beanDesc, deserializer)
                """.trimIndent(),
                TypedCustomObjectInterface(config).className,
                TypedCustomObjectsDelegatingDeserializer(config).className,
            )
            .returns(jsonDeserializerType)
            .build()
        )
        .build()

fun typedCustomObjectsDelegatingDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedCustomObjectsDelegatingDeserializer(config).className)
        .addAnnotation(generated)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter("d", jsonDeserializerType)
            .build()
        )
        .superclass(DelegatingDeserializer::class)
        .addSuperclassConstructorParameter("d")
        .addFunction(newDelegatingInstance(config))
        .addFunction(transformJson)
        .addFunction(makeParser)
        .build()

private fun deserialize(config: Configuration): FunSpec =
    FunSpec
        .builder("deserialize")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("p", JsonParser::class.asTypeName().copy(true))
        .addParameter("ctxt", DeserializationContext::class.asTypeName().copy(true))
        .addCode("""
            val codec = p?.codec
            val node: com.fasterxml.jackson.databind.JsonNode? = codec?.readTree(p)
            val containerName: String? = node?.path("container")?.asText()

        """.trimIndent())
        .addCode(generateContainerNameToTypeMap(config))
        .returns(CustomObject::class.asTypeName().copy(nullable = true))
        .build()

private fun generateContainerNameToTypeMap(config: Configuration): CodeBlock {
    val whenExpression = CodeBlock
        .builder()
        .add("return when (containerName) {\n")
        .add("⇥")

    config.customObjectTypes.forEach { (containerName, className) ->
        whenExpression.add(
            "%1S -> ctxt?.readValue(makeParser(node, codec), %2T::class.java)\n",
            containerName,
            TypedCustomObject(containerName, className, config).className
        )
    }

    return whenExpression
        .add("else -> ctxt?.readValue(makeParser(node, codec), %T::class.java)\n", CustomObjectImpl::class)
        .add("⇤")
        .add("}")
        .build()
}

private fun newDelegatingInstance(config: Configuration) =
    FunSpec
        .builder("newDelegatingInstance")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("newDelegatee", jsonDeserializerType)
        .addStatement(
            "return %1T(newDelegatee)",
            TypedCustomObjectsDelegatingDeserializer(config).className
        )
        .returns(jsonDeserializerType)
        .build()

private val transformJson =
    FunSpec
        .builder("deserialize")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("p", JsonParser::class.asTypeName().copy(true))
        .addParameter("ctxt", DeserializationContext::class.asTypeName().copy(true))
        .addCode("""
            val codec = p?.codec
            val node: com.fasterxml.jackson.databind.JsonNode? = codec?.readTree(p)
            val objectNode = node as com.fasterxml.jackson.databind.node.ObjectNode
            val wrapperObject = objectNode.objectNode()

            wrapperObject.set<JsonNode>("delegate", objectNode)
            wrapperObject.set<JsonNode>("value", objectNode.get("value"))

            return super.deserialize(makeParser(wrapperObject, codec), ctxt)
        """.trimIndent())
        .returns(Any::class)
        .build()