package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.model.TypedResources
import io.vrap.rmf.base.client.utils.json.JsonUtils

val defaultTypeToKey =
    FunSpec
        .builder("defaultTypeToKey")
        .addParameter("type", Type::class)
        .addStatement("return type.key")
        .returns(String::class)
        .build()

fun typedResourceTypeResolver(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedResourceTypeResolver(config).className)
        .addAnnotation(generated)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("runtimeTypes", LIST.parameterizedBy(Type::class.asTypeName()).copy(nullable = true))
                .defaultValue("null")
                .build()
            )
            .addParameter(ParameterSpec
                .builder("typeToKey", LambdaTypeName.get(null, Type::class.asClassName(), returnType = String::class.asTypeName()))
                .defaultValue("::defaultTypeToKey")
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
                    *(runtimeTypes ?: compiledTypes).map { it.id to typeToKey(it) }.toTypedArray()
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
        .addAnnotation(generated)
        .superclass(JsonDeserializer::class.asTypeName().parameterizedBy(typedResources.resourceInterface.asClassName()))
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addParameter(ParameterSpec
                .builder("typeResolver", TypeResolver(config).className.parameterizedBy(Type::class.asTypeName()))
                .defaultValue("%T()", TypedResourceTypeResolver(config).className)
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
        .build()

fun typedResourceBeanDeserializerModifier(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedResourceBeanDeserializerModifier(config).className)
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
                    else
                        super.modifyDeserializer(config, beanDesc, deserializer)
                """.trimIndent(),
                TypedResourceInterface(config).className,
                TypedResourceDelegatingDeserializer(config).className,
            )
            .returns(jsonDeserializerType)
            .build()
        )
        .build()

fun typedResourceDelegatingDeserializer(config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedResourceDelegatingDeserializer(config).className)
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
        .addFunction(transformFieldsJson)
        .addFunction(makeParser)
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
            val typeKey: String? = typeResolver.resolveTypeKey(node?.path("custom")?.path("type")?.path("id")?.asText() ?: "")

        """.trimIndent())
        .addCode(generateTypeToIdMap(typedResources, config))
        .returns(typedResources.resourceInterface.asTypeName().copy(nullable = true))
        .build()

private fun generateTypeToIdMap(typedResources: TypedResources, config: Configuration): CodeBlock {
    val whenExpression = CodeBlock
        .builder()
        .add("return when (typeKey) {\n")
        .add("⇥")

    typedResources.resources.forEach {
        whenExpression.add(
            "%1S -> ctxt?.readValue(makeParser(node, codec), %2T::class.java)\n",
            config.typeToKey(it.type),
            it.typedResourceClassName
        )
    }

    return whenExpression
        .add("else -> ctxt?.readValue(makeParser(node, codec), %T::class.java)\n", typedResources.resourceDefaultImplementation)
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
            TypedResourceDelegatingDeserializer(config).className
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
            wrapperObject.set<JsonNode>("custom", transformFieldsJson(objectNode.get("custom")))

            return super.deserialize(makeParser(wrapperObject, codec), ctxt)
        """.trimIndent())
        .returns(Any::class)
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