package de.akii.commercetools.api.customtypes.generator.product.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer
import com.fasterxml.jackson.databind.node.ArrayNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*

private val jsonDeserializerType =
    JsonDeserializer::class
        .asTypeName()
        .parameterizedBy(WildcardTypeName.producerOf(
            Any::class.asTypeName().copy(nullable = true))
        )
        .copy(nullable = true)

fun customProductVariantAttributesModifier(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(CustomProductVariantAttributesModifierClassName(config).className)
        .superclass(BeanDeserializerModifier::class)
        .addFunction(modifyDeserializer)
        .build()

fun customProductVariantAttributesDelegatingDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(CustomProductVariantAttributesDelegatingDeserializerClassName(config).className)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter("d", jsonDeserializerType)
            .build()
        )
        .superclass(DelegatingDeserializer::class)
        .addSuperclassConstructorParameter("d")
        .addFunction(newDelegatingInstance)
        .addFunction(deserialize)
        .addFunction(makeParser)
        .addFunction(transformProductVariantAttributesJson)
        .addFunction(attributeNameToPropertyName(config))
        .build()

private val modifyDeserializer =
    FunSpec
        .builder("modifyDeserializer")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("config", DeserializationConfig::class.asTypeName().copy(nullable = true))
        .addParameter("beanDesc", BeanDescription::class.asTypeName().copy(nullable = true))
        .addParameter("deserializer", jsonDeserializerType)
        .addCode("""
            return if (beanDesc?.type?.isTypeOrSubTypeOf(CustomProductVariantAttributes::class.java) == true)
                super.modifyDeserializer(config, beanDesc, CustomProductVariantAttributesDelegatingDeserializer(deserializer))
            else
                super.modifyDeserializer(config, beanDesc, deserializer)
        """.trimIndent())
        .returns(jsonDeserializerType)
        .build()

private val newDelegatingInstance =
    FunSpec
        .builder("newDelegatingInstance")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("newDelegatee", jsonDeserializerType)
        .addStatement("return CustomProductVariantAttributesDelegatingDeserializer(newDelegatee)")
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
                    attributeNameToPropertyName(attributeName),
                    attributeValue
                )
            }

            return typedAttributes
        """.trimIndent())
        .returns(JsonNode::class)
        .build()

private fun attributeNameToPropertyName(config: Configuration) =
    FunSpec
        .builder("attributeNameToPropertyName")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("attributeName", String::class)
        .addCode(generateAttributeNameToPropertyNameMap(config))
        .returns(String::class)
        .build()

fun generateAttributeNameToPropertyNameMap(config: Configuration): CodeBlock {
    val whenExpression = CodeBlock
        .builder()
        .add("return when(attributeName) {\n")
        .add("⇥")

    config
        .productTypes
        .flatMap { it.attributes }
        .map { it.name to config.attributeNameToPropertyName(it.name) }
        .toSet()
        .forEach {
            whenExpression.add("%1S -> %2S\n", it.first, it.second)
        }

    return whenExpression
        .add("else -> attributeName\n")
        .add("⇤")
        .add("}")
        .build()
}