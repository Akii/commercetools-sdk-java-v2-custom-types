package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.type.CustomFields
import com.commercetools.api.models.type.CustomFieldsImpl
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.TypedResourceFile
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.typeToClassName
import io.vrap.rmf.base.client.utils.Generated

fun customFieldsDeserializerFile(typedResourceFiles: List<TypedResourceFile>, config: Configuration): FileSpec {
    val file = FileSpec
        .builder("${config.packageName}.custom_fields", "deserializer")
        .addType(customFieldsDeserializer(config))

    typedResourceFiles.forEach {
        file.addType(typedResourceDeserializer(it, config))
    }

    return file.build()
}

fun customFieldsDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedCustomFieldsDeserializerClassName(config).className)
        .addAnnotation(Generated::class)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(CustomFields::class.asTypeName()))
        .addFunction(deserialize(config))
        .addFunction(makeParser)
        .addFunction(transformJson)
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
            val typeId: String? = node?.path("type")?.path("id")?.asText()

        """.trimIndent())
        .addCode(generateTypeToIdMap(config))
        .returns(CustomFields::class.asTypeName().copy(nullable = true))
        .build()

private fun generateTypeToIdMap(config: Configuration): CodeBlock {
    val whenExpression = CodeBlock
        .builder()
        .add("return when (typeId) {\n")
        .add("⇥")

    config.customTypes.forEach {
        whenExpression.add(
            "%1S -> ctxt?.readValue(transformJson(node, codec), %2T::class.java)\n",
            it.id,
            typeToClassName(it, config)
        )
    }

    return whenExpression
        .add("else -> ctxt?.readValue(makeParser(node, codec), %T::class.java)\n", CustomFieldsImpl::class)
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

            wrapperObject.set<JsonNode>("custom", objectNode)
            wrapperObject.set<JsonNode>("typedFields", objectNode.get("fields"))

            return makeParser(wrapperObject, codec)
        """.trimIndent())
        .returns(JsonParser::class.asTypeName().copy(nullable = true))
        .build()