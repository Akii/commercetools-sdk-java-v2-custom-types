package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.product.ProductProjection
import com.commercetools.api.models.product.ProductProjectionImpl
import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*

fun typedProductProjectionDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedProductProjectionDeserializer(config).className)
        .addAnnotation(generated)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(ProductProjection::class.asTypeName()))
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("typeResolver", TypeResolver(config).className.parameterizedBy(ProductType::class.asTypeName()))
                .defaultValue("%T()", ProductTypeResolver(config).className)
                .build()
            )
            .build()
        )
        .addProperty(PropertySpec
            .builder("typeResolver", TypeResolver(config).className.parameterizedBy(ProductType::class.asTypeName()))
            .initializer("typeResolver")
            .build()
        )
        .addFunction(deserialize(config))
        .addFunction(transformProductProjectionJson)
        .addFunction(transformProductVariantJson)
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
            val productTypeKey: String? = typeResolver.resolveTypeKeyById(node?.path("productType")?.path("id")?.asText()!!)

        """.trimIndent())
        .addCode(generateProductTypeToIdMap(config))
        .returns(ProductProjection::class.asTypeName().copy(nullable = true))
        .build()

private fun generateProductTypeToIdMap(config: Configuration): CodeBlock {
    val whenExpression = CodeBlock
        .builder()
        .add("return when (productTypeKey) {\n")
        .add("⇥")

    config.productTypes.forEach {
        whenExpression.add(
            "%1S -> ctxt?.readValue(makeParser(transformProductProjectionJson(node), codec), %2T::class.java)\n",
            config.productTypeToKey(it),
            TypedProductProjection(it, config).className
        )
    }

    return whenExpression
        .add("else -> ctxt?.readValue(makeParser(node, codec), %T::class.java)\n", ProductProjectionImpl::class)
        .add("⇤")
        .add("}")
        .build()
}

private val transformProductProjectionJson =
    FunSpec
        .builder("transformProductProjectionJson")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", JsonNode::class.asTypeName().copy(nullable = true))
        .addCode("""
            val objectNode = json as com.fasterxml.jackson.databind.node.ObjectNode
            val wrapperObject = objectNode.objectNode()
            val variantsNode = objectNode.arrayNode()

            wrapperObject.set<JsonNode>("delegate", objectNode)
            wrapperObject.set<JsonNode>("masterVariant", transformProductVariantJson(objectNode.get("masterVariant")))
            wrapperObject.set<JsonNode>("variants", variantsNode.addAll(objectNode.get("variants")?.map { transformProductVariantJson(it) } ?: emptyList()))

            return wrapperObject
        """.trimIndent())
        .returns(JsonNode::class)
        .build()

private val transformProductVariantJson =
    FunSpec
        .builder("transformProductVariantJson")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", JsonNode::class.asTypeName().copy(nullable = true))
        .addCode("""
            val objectNode = json as com.fasterxml.jackson.databind.node.ObjectNode
            val wrapperObject = objectNode.objectNode()

            wrapperObject.set<JsonNode>("delegate", objectNode)
            wrapperObject.set<JsonNode>("typedAttributes", objectNode.get("attributes"))

            return wrapperObject
        """.trimIndent())
        .returns(JsonNode::class)
        .build()