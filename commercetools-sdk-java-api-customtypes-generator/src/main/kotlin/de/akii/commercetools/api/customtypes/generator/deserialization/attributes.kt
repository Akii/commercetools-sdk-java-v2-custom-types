package de.akii.commercetools.api.customtypes.generator.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun typedProductVariantAttributesDelegatingDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedProductVariantAttributesDelegatingDeserializer(config).className)
        .addAnnotation(generated)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter("d", jsonDeserializerType)
            .build()
        )
        .superclass(DelegatingDeserializer::class)
        .addSuperclassConstructorParameter("d")
        .addFunction(newDelegatingInstance(config))
        .addFunction(deserialize)
        .addFunction(makeParser)
        .addFunction(transformProductVariantAttributesJson)
        .build()

private fun newDelegatingInstance(config: Configuration) =
    FunSpec
        .builder("newDelegatingInstance")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("newDelegatee", jsonDeserializerType)
        .addStatement(
            "return %1T(newDelegatee)",
            TypedProductVariantAttributesDelegatingDeserializer(config).className
        )
        .returns(jsonDeserializerType)
        .build()

private val deserialize =
    FunSpec
        .builder("deserialize")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("p", JsonParser::class.asTypeName().copy(true))
        .addParameter("ctxt", DeserializationContext::class.asTypeName().copy(true))
        .addCode("""
            val codec = p?.codec
            val node: JsonNode? = codec?.readTree(p)

            return when (node) {
                is ArrayNode -> super.deserialize(makeParser(transformProductVariantAttributesJson(node), codec), ctxt)
                else -> return super.deserialize(makeParser(node, codec), ctxt)
            }
        """.trimIndent())
        .returns(Any::class)
        .build()

private val transformProductVariantAttributesJson =
    FunSpec
        .builder("transformProductVariantAttributesJson")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", ArrayNode::class)
        .addCode("""
            val typedAttributes = json.objectNode()

            json.forEach {
                val attributeName = it.get("name").asText()
                val attributeValue = it.get("value")
                
                typedAttributes.set<JsonNode>(
                    attributeName,
                    attributeValue
                )
            }

            return typedAttributes
        """.trimIndent())
        .returns(JsonNode::class)
        .build()