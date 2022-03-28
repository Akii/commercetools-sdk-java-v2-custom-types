package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.model.TypedResources
import io.vrap.rmf.base.client.utils.Generated
import io.vrap.rmf.base.client.utils.json.JsonUtils

fun typedResourceDeserializerFile(typedResources: List<TypedResources>, config: Configuration): List<FileSpec> =
    typedResources
        .map {
            FileSpec
                .builder(it.packageName, "deserializer")
                .addType(typedResourceDeserializer(it, config))
                .build()
        }

fun customTypeResolverFile(config: Configuration): FileSpec =
    FileSpec
        .builder("${config.packageName}.custom_fields", "typeResolver")
        .addType(customTypeResolver(config))
        .build()

fun customTypeResolver(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(CustomTypeResolver(config).className)
        .addAnnotation(Generated::class)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("runtimeTypes", LIST.parameterizedBy(Type::class.asTypeName()).copy(nullable = true))
                .defaultValue("null")
                .build()
            )
            .build()
        )
        .addSuperinterface(TypeResolver(config).className.parameterizedBy(Type::class.asTypeName()))
        .addProperty(PropertySpec
            .builder("compiledTypesDefinition", String::class, KModifier.PRIVATE)
            .initializer("\"\"\"%L\"\"\"", JsonUtils
                .createObjectMapper()
                .writeValueAsString(config.customTypes)
                .replace(' ', '·')
            )
            .build()
        )
        .addProperty(PropertySpec
            .builder("compiledTypes", LIST.parameterizedBy(Type::class.asTypeName()), KModifier.PRIVATE)
            .initializer("""
                %1T
                    .createObjectMapper()
                    .readValue(compiledTypesDefinition, object : %2T<List<%3T>>() {})
            """.trimIndent(),
                JsonUtils::class,
                TypeReference::class,
                Type::class
            )
            .build()
        )
        .addProperty(PropertySpec
            .builder("typeIdMap", MAP.parameterizedBy(String::class.asTypeName(), String::class.asTypeName()), KModifier.PRIVATE)
            .initializer("""
                mapOf(
                    *(runtimeTypes ?: compiledTypes).map { it.id to it.key }.toTypedArray()
                )
            """.trimIndent())
            .build()
        )
        .addInitializerBlock(CodeBlock.of("""
                if (runtimeTypes != null) {
                    compiledTypes.forEach { compiledType ->
                        val runtimeType = runtimeTypes.find { it.key == compiledType.key } ?:
                            throw RuntimeException("Types·verification·failed:·Unable·to·find·runtime·type·with·key·${'$'}{compiledType.key}")
        
                        compiledType.fieldDefinitions.forEach { compiledField ->
                            val runtimeField = runtimeType.fieldDefinitions.find { it.name == compiledField.name } ?:
                                throw RuntimeException("Types·verification·failed:·Unable·to·find·runtime·attribute·with·name·${'$'}{compiledField.name}·in·type·${'$'}{compiledType.name}")
        
                            if (compiledField.type != runtimeField.type) {
                                throw RuntimeException("Types·verification·failed:·Attribute·type·differs·for·attribute·with·name·${'$'}{compiledField.name}·in·type·${'$'}{compiledType.name}·between·compiled·attribute·type·${'$'}{compiledField.type}·and·runtime·attribute·type·${'$'}{compiledField.type}")
                            }
                        }
                    }
                }
            """.trimIndent())
        )
        .addFunction(FunSpec
            .builder("resolveTypeKey")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("typeId", String::class)
            .addCode("return typeIdMap[typeId]")
            .returns(String::class.asTypeName().copy(nullable = true))
            .build())
        .build()

fun typedResourceDeserializer(typedResources: TypedResources, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedResourceDeserializer(typedResources).className)
        .addAnnotation(Generated::class)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(typedResources.resourceInterface.asClassName()))
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("typeResolver", TypeResolver(config).className.parameterizedBy(Type::class.asTypeName()))
                .defaultValue("%T()", CustomTypeResolver(config).className)
                .build()
            )
            .build()
        )
        .addProperty(PropertySpec
            .builder("typeResolver", TypeResolver(config).className.parameterizedBy(Type::class.asTypeName()))
            .initializer("typeResolver")
            .build()
        )
        .addFunction(deserialize(typedResources, config))
        .addFunction(makeParser)
        .addFunction(transformJson)
        .addFunction(transformFieldsJson)
        .build()

private fun deserialize(typedResources: TypedResources, config: Configuration): FunSpec =
    FunSpec
        .builder("deserialize")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("p", JsonParser::class.asTypeName().copy(true))
        .addParameter("ctxt", DeserializationContext::class.asTypeName().copy(true))
        .addCode("""
            val codec = p?.codec
            val node: com.fasterxml.jackson.databind.JsonNode? = codec?.readTree(p)
            val typeId: String? = typeResolver.resolveTypeKey(node?.path("custom")?.path("type")?.path("id")?.asText() ?: "")

        """.trimIndent())
        .addCode(generateTypeToIdMap(typedResources))
        .returns(typedResources.resourceInterface.asTypeName().copy(nullable = true))
        .build()

private fun generateTypeToIdMap(typedResources: TypedResources): CodeBlock {
    val whenExpression = CodeBlock
        .builder()
        .add("return when (typeId) {\n")
        .add("⇥")

    typedResources.resources.forEach {
        whenExpression.add(
            "%1S -> ctxt?.readValue(transformJson(node, codec), %2T::class.java)\n",
            it.type.key!!,
            it.typedResourceClassName
        )
    }

    return whenExpression
        .add("else -> ctxt?.readValue(makeParser(node, codec), %T::class.java)\n", typedResources.resourceDefaultImplementation)
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
            wrapperObject.set<JsonNode>("custom", transformFieldsJson(objectNode.get("custom")))

            return makeParser(wrapperObject, codec)
        """.trimIndent())
        .returns(JsonParser::class.asTypeName().copy(nullable = true))
        .build()

private val transformFieldsJson =
    FunSpec
        .builder("transformFieldsJson")
        .addModifiers(KModifier.PRIVATE)
        .addParameter("json", JsonNode::class.asTypeName().copy(nullable = true))
        .addCode("""
            val objectNode = json as com.fasterxml.jackson.databind.node.ObjectNode
            val wrapperObject = objectNode.objectNode()

            wrapperObject.set<JsonNode>("fields", objectNode)
            wrapperObject.set<JsonNode>("typedFields", objectNode.get("fields"))

            return wrapperObject
        """.trimIndent())
        .returns(JsonNode::class.asTypeName().copy(nullable = true))
        .build()