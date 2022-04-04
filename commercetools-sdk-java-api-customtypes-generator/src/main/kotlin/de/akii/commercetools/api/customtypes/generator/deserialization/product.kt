package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.json.JsonUtils

val defaultProductTypeToKey =
    FunSpec
        .builder("defaultProductTypeToKey")
        .addParameter("productType", ProductType::class)
        .addStatement("return productType.key!!")
        .returns(String::class)
        .build()

fun productTypeResolver(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(ProductTypeResolver(config).className)
        .addAnnotation(generated)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("runtimeTypes", LIST.parameterizedBy(ProductType::class.asTypeName()).copy(nullable = true))
                .defaultValue("null")
                .build()
            )
            .addParameter(ParameterSpec
                .builder("productTypeToKey", LambdaTypeName.get(null, ProductType::class.asClassName(), returnType = String::class.asTypeName()))
                .defaultValue("::defaultProductTypeToKey")
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
                    *(runtimeTypes ?: compiledTypes).map { it.id to productTypeToKey(it) }.toTypedArray()
                )
            """.trimIndent())
            .build()
        )
        .addInitializerBlock(CodeBlock.of("""
                if (runtimeTypes != null) {
                    compiledTypes.forEach { compiledType ->
                        val runtimeType = runtimeTypes.find { productTypeToKey(it) == productTypeToKey(compiledType) } ?:
                            throw RuntimeException("Types·verification·failed:·Unable·to·find·runtime·product·type·with·name·\"${'$'}{compiledType.name}\"")
    
                        compiledType.attributes.forEach { compiledAttribute ->
                            val runtimeAttribute = runtimeType.attributes.find { it.name == compiledAttribute.name } ?:
                                throw RuntimeException("Types·verification·failed:·Unable·to·find·runtime·attribute·with·name·\"${'$'}{compiledAttribute.name}\"·in·product·type·\"${'$'}{compiledType.name}\"")
    
                            if (compiledAttribute.type != runtimeAttribute.type) {
                                throw RuntimeException("Types·verification·failed:·Attribute·type·differs·for·attribute·with·name·\"${'$'}{compiledAttribute.name}\"·in·product·type·\"${'$'}{compiledType.name}\"·between·compiled·attribute·type·\"${'$'}{compiledAttribute.type.name}\"·and·runtime·attribute·type·\"${'$'}{compiledAttribute.type.name}\"")
                            }
                        }
                    }
                }

            """.trimIndent())
        )
        .addFunction(FunSpec
            .builder("resolveTypeKeyById")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("typeId", String::class)
            .addCode("return typeIdMap[typeId]")
            .returns(String::class.asTypeName().copy(nullable = true))
            .build()
        )
        .addFunction(FunSpec
            .builder("resolveTypeIdByKey")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("typeKey", String::class)
            .addCode("return typeIdMap.filterValues { it == typeKey }.keys.first()")
            .returns(String::class.asTypeName().copy(nullable = true))
            .build()
        )
        .build()

fun typedProductDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedProductDeserializer(config).className)
        .addAnnotation(generated)
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
        .build()

fun typedProductBeanDeserializerModifier(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedProductBeanDeserializerModifier(config).className)
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
                    else if (beanDesc?.type?.isTypeOrSubTypeOf(%3T::class.java) == true)
                        super.modifyDeserializer(config, beanDesc, %4T(deserializer))
                    else
                        super.modifyDeserializer(config, beanDesc, deserializer)
                """.trimIndent(),
                TypedProductInterface(config).className,
                TypedProductDelegatingDeserializer(config).className,
                TypedProductVariantAttributesInterface(config).className,
                TypedProductVariantAttributesDelegatingDeserializer(config).className
            )
            .returns(jsonDeserializerType)
            .build()
        )
        .build()

fun typedProductDelegatingDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedProductDelegatingDeserializer(config).className)
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
        .addFunction(transformProductCatalogDataJson)
        .addFunction(transformProductDataJson)
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
        .returns(Product::class.asTypeName().copy(nullable = true))
        .build()

private fun generateProductTypeToIdMap(config: Configuration): CodeBlock {
    val whenExpression = CodeBlock
        .builder()
        .add("return when (productTypeKey) {\n")
        .add("⇥")

    config.productTypes.forEach {
        whenExpression.add(
            "%1S -> ctxt?.readValue(makeParser(node, codec), %2T::class.java)\n",
            config.productTypeToKey(it),
            TypedProduct(it, config).className
        )
    }

    return whenExpression
        .add("else -> ctxt?.readValue(makeParser(node, codec), %T::class.java)\n", ProductImpl::class)
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
            TypedProductDelegatingDeserializer(config).className
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
            wrapperObject.set<JsonNode>("masterData", transformProductCatalogDataJson(objectNode.get("masterData")))

            return super.deserialize(makeParser(wrapperObject, codec), ctxt)
        """.trimIndent())
        .returns(Any::class)
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