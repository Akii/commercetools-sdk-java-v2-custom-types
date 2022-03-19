package de.akii.commercetools.api.customtypes.generator.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.common.TypedResourceDeserializer
import io.vrap.rmf.base.client.utils.Generated
import kotlin.reflect.KClass

fun typedResourceDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedResourceDeserializer(config).className)
        .addAnnotation(Generated::class)
        .addTypeVariable(TypeVariableName.invoke("A : Any"))
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("typeClass", KClass::class.asTypeName().parameterizedBy(TypeVariableName.invoke("A")))
                .build()
            )
            .build()
        )
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(TypeVariableName.invoke("A")))
        .addProperty(PropertySpec
            .builder("typeClass", KClass::class.asTypeName().parameterizedBy(TypeVariableName.invoke("A")))
            .addModifiers(KModifier.PRIVATE)
            .initializer("typeClass")
            .build()
        )
        .addFunction(deserialize)
        .addFunction(makeParser)
        .addFunction(transformJson)
        .build()

private val deserialize: FunSpec =
    FunSpec
        .builder("deserialize")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("p", JsonParser::class.asTypeName().copy(true))
        .addParameter("ctxt", DeserializationContext::class.asTypeName().copy(true))
        .addCode("""
            val codec = p?.codec
            val node: com.fasterxml.jackson.databind.JsonNode? = codec?.readTree(p)
            return ctxt?.readValue(transformJson(node, codec), typeClass.java)
        """.trimIndent())
        .returns(TypeVariableName.invoke("A").copy(nullable = true))
        .build()

private val transformJson =
    FunSpec
        .builder("transformJson")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", JsonNode::class.asTypeName().copy(nullable = true))
        .addParameter("codec", ObjectCodec::class.asTypeName().copy(nullable = true))
        .addCode("""
            val objectNode = json as com.fasterxml.jackson.databind.node.ObjectNode
            val wrapperObject = objectNode.objectNode()

            wrapperObject.set<JsonNode>("delegate", objectNode)
            wrapperObject.set<JsonNode>("custom", objectNode.get("custom"))

            return makeParser(wrapperObject, codec)
        """.trimIndent())
        .returns(JsonParser::class.asTypeName().copy(nullable = true))
        .build()