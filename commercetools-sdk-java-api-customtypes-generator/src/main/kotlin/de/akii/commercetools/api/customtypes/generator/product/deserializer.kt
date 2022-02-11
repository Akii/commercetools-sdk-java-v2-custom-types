package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.product.Product
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.Configuration
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.types.ProductType

fun customProductDeserializer(productTypes: List<ProductType>, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(CustomProductDeserializerClassName(config).className)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(Product::class.asTypeName()))
        .addFunction(deserializeCustomProduct(productTypes, config))
        .addFunction(makeParser)
        .addFunction(transformJson)
        .addFunction(transformProductDataJson)
        .addFunction(transformProductVariantJson)
        .addFunction(attributeNameToPropertyName)
        .build()

private fun deserializeCustomProduct(productTypes: List<ProductType>, config: Configuration): FunSpec =
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
        .addCode(productTypeIdStatements(productTypes, config) + "\n")
        .addStatement("  else -> ctxt?.readValue(makeParser(node, codec), com.commercetools.api.models.product.ProductImpl::class.java)")
        .addStatement("}")
        .returns(Product::class.asTypeName().copy(nullable = true))
        .build()

private fun productTypeIdStatements(productTypes: List<ProductType>, config: Configuration): String =
    productTypes
        .joinToString("\n") {
            "|  \"${it.id}\" -> ctxt?.readValue(transformJson(node, codec), ${
                ProductClassName(
                    it.name,
                    config
                ).className.canonicalName
            }::class.java)"
        }
        .trimMargin()

private val makeParser =
    FunSpec
        .builder("makeParser")
        .addParameter("json", JsonNode::class.asTypeName().copy(nullable = true))
        .addParameter("codec", ObjectCodec::class.asTypeName().copy(nullable = true))
        .addCode("""
            val p = json?.traverse()
            p?.setCodec(codec)
            p?.nextToken()
            return p
        """.trimIndent())
        .returns(JsonParser::class.asTypeName().copy(nullable = true))
        .build()

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
                    is ObjectNode -> transformProductVariantJson(it)
                }
            }
        """.trimIndent())
        .build()

private val transformProductVariantJson =
    FunSpec
        .builder("transformProductVariantJson")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", ObjectNode::class)
        .addCode("""
            val typedAttributes = json.putObject("typedAttributes")
            
            json.path("attributes").forEach {
                val attributeName = it.get("name").asText()
                val attributeValue = it.get("value")
                
                typedAttributes.set<JsonNode>(
                    attributeNameToPropertyName(attributeName),
                    attributeValue
                )
            }
        """.trimIndent())
        .build()

private val attributeNameToPropertyName =
    FunSpec
        .builder("attributeNameToPropertyName")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("attributeName", String::class)
        .addCode("""
            return attributeName
                .split('-', '_', ' ')
                .joinToString("") { part ->
                    part.replaceFirstChar { it.uppercase() }
                }
                .replaceFirstChar { it.lowercase() }
        """.trimIndent())
        .returns(String::class)
        .build()