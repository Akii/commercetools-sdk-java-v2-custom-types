package de.akii.commercetools.api.customtypes.generator.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.TypedResourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.common.TypedResourceDeserializerClassName
import io.vrap.rmf.base.client.utils.Generated

fun typedResourceDeserializer(typedResource: TypedResourceFile, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedResourceDeserializerClassName(typedResource, config).className)
        .addAnnotation(Generated::class)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(typedResource.resourceInterface.asClassName()))
        .addFunction(deserialize(typedResource))
        .addFunction(makeParser)
        .addFunction(transformJson)
        .build()

private fun deserialize(typedResource: TypedResourceFile): FunSpec =
    FunSpec
        .builder("deserialize")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("p", JsonParser::class.asTypeName().copy(true))
        .addParameter("ctxt", DeserializationContext::class.asTypeName().copy(true))
        .addCode("""
            val codec = p?.codec
            val node: com.fasterxml.jackson.databind.JsonNode? = codec?.readTree(p)
            return ctxt?.readValue(transformJson(node, codec), %1T::class.java)
        """.trimIndent(), typedResource.typedResourceClassName)
        .returns(typedResource.resourceInterface.asClassName().copy(nullable = true))
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