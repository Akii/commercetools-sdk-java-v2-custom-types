package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.product.Product
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import de.akii.commercetools.api.customtypes.generator.Configuration
import de.akii.commercetools.api.customtypes.generator.common.ProductClassName
import de.akii.commercetools.api.customtypes.generator.common.ProductDeserializerClassName
import de.akii.commercetools.api.customtypes.generator.types.ProductType

fun customProductDeserializer(productTypes: List<ProductType>, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ProductDeserializerClassName(config).className)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(Product::class.asTypeName()))
        .addFunction(deserialize(productTypes, config))
        .build()

private fun deserialize(productTypes: List<ProductType>, config: Configuration): FunSpec =
    FunSpec
        .builder("deserialize")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("p", JsonParser::class.asTypeName().copy(true))
        .addParameter("ctxt", DeserializationContext::class.asTypeName().copy(true))
        .addStatement("val node: com.fasterxml.jackson.databind.JsonNode = p!!.codec.readTree(p)")
        .addStatement("val productTypeId: String = node.path(\"productType\")?.path(\"id\")?.asText()!!")
        .addStatement("return when (productTypeId) {")
        .addCode(productTypeIdStatements(productTypes, config) + "\n")
        .addStatement("  else -> ctxt!!.readValue(node.traverse(), com.commercetools.api.models.product.ProductImpl::class.java)")
        .addStatement("}")
        .returns(Product::class)
        .build()

private fun productTypeIdStatements(productTypes: List<ProductType>, config: Configuration): String =
    productTypes
        .joinToString("\n") {
            "|  \"${it.id}\" -> ctxt!!.readValue(node.traverse(), ${
                ProductClassName(
                    it.name,
                    config
                ).className.canonicalName
            }::class.java)"
        }
        .trimMargin()