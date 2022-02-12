package de.akii.commercetools.api.customtypes.generator.product.deserialization

import com.commercetools.api.models.product.Product
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.Configuration
import de.akii.commercetools.api.customtypes.generator.common.*

fun customProductDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(CustomProductDeserializerClassName(config).className)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(Product::class.asTypeName()))
        .addFunction(deserialize(config))
        .addFunction(makeParser)
        .addFunction(transformJson)
        .addFunction(transformProductDataJson)
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
            val productTypeId: String? = node?.path("productType")?.path("id")?.asText()

            return when (productTypeId) {

        """.trimIndent())
        .addCode(productTypeIdStatements(config) + "\n")
        .addStatement("  else -> ctxt?.readValue(makeParser(node, codec), com.commercetools.api.models.product.ProductImpl::class.java)")
        .addStatement("}")
        .returns(Product::class.asTypeName().copy(nullable = true))
        .build()

private fun productTypeIdStatements(config: Configuration): String =
    config.productTypes
        .joinToString("\n") {
            "|  \"${it.id}\" -> ctxt?.readValue(transformJson(node, codec), ${
                ProductClassName(
                    it.name,
                    config
                ).className.canonicalName
            }::class.java)"
        }
        .trimMargin()

private val transformJson =
    FunSpec
        .builder("transformJson")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", JsonNode::class.asTypeName().copy(nullable = true))
        .addParameter("codec", ObjectCodec::class.asTypeName().copy(nullable = true))
        .addCode("""
            transformProductDataJson(json?.path("masterData")?.path("current"))
            transformProductDataJson(json?.path("masterData")?.path("staged"))
            return makeParser(json, codec)
        """.trimIndent())
        .returns(JsonParser::class.asTypeName().copy(nullable = true))
        .build()

private val transformProductDataJson =
    FunSpec
        .builder("transformProductDataJson")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", JsonNode::class.asTypeName().copy(nullable = true))
        .addCode("""
            val variants: List<JsonNode?> =
                listOf(json?.path("masterVariant")) +
                        (json?.path("variants")?.elements()?.asSequence()?.toList() ?: emptyList())
    
            variants.forEach {
                when (it) {
                    is com.fasterxml.jackson.databind.node.ObjectNode ->
                        it.set<JsonNode>("typedAttributes", it.path("attributes"))
                }
            }
        """.trimIndent())
        .build()