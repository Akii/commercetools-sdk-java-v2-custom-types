package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated
import io.vrap.rmf.base.client.utils.json.JsonUtils

fun productDeserializerFile(config: Configuration): FileSpec =
    FileSpec
        .builder("${config.packageName}.product", "deserializer")
        .addType(productTypeResolver(config))
        .addType(customProductDeserializer(config))
        .addType(customProductVariantAttributesModifier(config))
        .addType(customProductVariantAttributesDelegatingDeserializer(config))
        .build()

private fun productTypeResolver(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ProductTypeResolver(config).className)
        .addAnnotation(Generated::class)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("runtimeTypes", LIST.parameterizedBy(ProductType::class.asTypeName()).copy(nullable = true))
                .defaultValue("null")
                .build()
            )
            .build()
        )
        .addSuperinterface(TypeResolver(config).className.parameterizedBy(ProductType::class.asTypeName()))
        .addProperty(PropertySpec
            .builder("compiledTypesDefinition", String::class, KModifier.PRIVATE)
            .initializer("\"\"\"%L\"\"\"", JsonUtils
                .createObjectMapper()
                .writeValueAsString(config.productTypes)
                .replace(' ', '·')
            )
            .build()
        )
        .addProperty(PropertySpec
            .builder("compiledTypes", LIST.parameterizedBy(ProductType::class.asTypeName()), KModifier.PRIVATE)
            .initializer("""
                %1T
                    .createObjectMapper()
                    .readValue(compiledTypesDefinition, object : %2T<List<%3T>>() {})
            """.trimIndent(),
                JsonUtils::class,
                TypeReference::class,
                ProductType::class
            )
            .build()
        )
        .addProperty(PropertySpec
            .builder("typeIdMap", MAP.parameterizedBy(String::class.asTypeName(), String::class.asTypeName()), KModifier.PRIVATE)
            .initializer("""
                mapOf(
                    *(runtimeTypes ?: compiledTypes).map { it.id to it.name }.toTypedArray()
                )
            """.trimIndent())
            .build()
        )
        .addInitializerBlock(CodeBlock.of("""
                if (runtimeTypes != null) {
                    compiledTypes.forEach { compiledType ->
                        val runtimeType = runtimeTypes.find { it.name == compiledType.name } ?:
                            throw RuntimeException("Types·verification·failed:·Unable·to·find·runtime·product·type·with·name·${'$'}{compiledType.name}")
    
                        compiledType.attributes.forEach { compiledAttribute ->
                            val runtimeAttribute = runtimeType.attributes.find { it.name == compiledAttribute.name } ?:
                                throw RuntimeException("Types·verification·failed:·Unable·to·find·runtime·attribute·with·name·${'$'}{compiledAttribute.name}·in·product·type·${'$'}{compiledType.name}")
    
                            if (compiledAttribute.type != runtimeAttribute.type) {
                                throw RuntimeException("Types·verification·failed:·Attribute·type·differs·for·attribute·with·name·${'$'}{compiledAttribute.name}·in·product·type·${'$'}{compiledType.name}·between·compiled·attribute·type·${'$'}{compiledAttribute.type}·and·runtime·attribute·type·${'$'}{compiledAttribute.type}")
                            }
                        }
                    }
                }
            """.trimIndent())
        )
        .addFunction(FunSpec
            .builder("resolveTypeName")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("typeId", String::class)
            .addCode("return typeIdMap[typeId]")
            .returns(String::class.asTypeName().copy(nullable = true))
            .build())
        .build()

private fun customProductDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(CustomProductDeserializer(config).className)
        .addAnnotation(Generated::class)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(Product::class.asTypeName()))
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
        .addFunction(makeParser)
        .addFunction(transformJson)
        .addFunction(transformProductCatalogDataJson)
        .addFunction(transformProductDataJson)
        .addFunction(transformProductVariantJson)
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
            val productTypeId: String? = typeResolver.resolveTypeName(node?.path("productType")?.path("id")?.asText()!!)

        """.trimIndent())
        .addCode(generateProductTypeToIdMap(config))
        .returns(Product::class.asTypeName().copy(nullable = true))
        .build()

private fun generateProductTypeToIdMap(config: Configuration): CodeBlock {
    val whenExpression = CodeBlock
        .builder()
        .add("return when (productTypeId) {\n")
        .add("⇥")

    config.productTypes.forEach {
        whenExpression.add(
            "%1S -> ctxt?.readValue(transformJson(node, codec), %2T::class.java)\n",
            it.name,
            Product(it, config).className
        )
    }

    return whenExpression
        .add("else -> ctxt?.readValue(makeParser(node, codec), %T::class.java)\n", ProductImpl::class)
        .add("⇤")
        .add("}")
        .build()
}

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
            wrapperObject.set<JsonNode>("masterData", transformProductCatalogDataJson(objectNode.get("masterData")))

            return makeParser(wrapperObject, codec)
        """.trimIndent())
        .returns(JsonParser::class.asTypeName().copy(nullable = true))
        .build()

private val transformProductCatalogDataJson =
    FunSpec
        .builder("transformProductCatalogDataJson")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", JsonNode::class.asTypeName().copy(nullable = true))
        .addCode("""
            val objectNode = json as com.fasterxml.jackson.databind.node.ObjectNode
            val wrapperObject = objectNode.objectNode()

            wrapperObject.set<JsonNode>("delegate", objectNode)
            wrapperObject.set<JsonNode>("current", transformProductDataJson(objectNode.get("current")))
            wrapperObject.set<JsonNode>("staged", transformProductDataJson(objectNode.get("staged")))

            return wrapperObject
        """.trimIndent())
        .returns(JsonNode::class)
        .build()

private val transformProductDataJson =
    FunSpec
        .builder("transformProductDataJson")
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