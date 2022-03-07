package de.akii.commercetools.api.customtypes.generator.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass

private val jsonCreator =
    AnnotationSpec
        .builder(JsonCreator::class)
        .build()

sealed class ConstructorArgument(
    val name: String,
    private val type: ClassName,
    private val parameterizedBy: ClassName? = null,
    private val nullable: Boolean = false
) {
    val typeName
        get() = if (parameterizedBy != null) {
            type.parameterizedBy(parameterizedBy).copy(nullable)
        } else {
            type.copy(nullable)
        }
}

class CTParameter(
    name: String,
    type: ClassName,
    parameterizedBy: ClassName? = null,
    nullable: Boolean = false
) : ConstructorArgument(name, type, parameterizedBy, nullable) {
    constructor(
        name: String,
        type: KClass<*>,
        parameterizedBy: KClass<*>? = null,
        nullable: Boolean = false
    ) : this(name, type.asTypeName(), parameterizedBy?.asTypeName(), nullable)
}

class CTProperty(
    name: String,
    type: ClassName,
    parameterizedBy: ClassName? = null,
    nullable: Boolean = false,
    val modifiers: List<KModifier> = listOf(KModifier.PRIVATE)
) : ConstructorArgument(name, type, parameterizedBy, nullable)

fun TypeSpec.Builder.addCTConstructorArguments(vararg properties: ConstructorArgument): TypeSpec.Builder =
    this.addCTConstructorArguments(properties.asList())

fun TypeSpec.Builder.addCTConstructorArguments(properties: List<ConstructorArgument>): TypeSpec.Builder {
    val attributes = properties.map {
        when (it) {
            is CTParameter -> parameter(it.name, it.typeName) to null
            is CTProperty -> parameter(it.name, it.typeName) to property(
                it.name,
                it.typeName,
                it.modifiers
            )
        }
    }

    val constructor = FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameters(attributes.map { it.first })

    attributes.forEach {
        constructor.addStatement("this.%1N = %2N", it.first.name, it.first.name)
    }

    this.primaryConstructor(constructor.build())
    this.addProperties(attributes.mapNotNull { it.second })

    return this
}

private fun parameter(name: String, type: TypeName): ParameterSpec =
    ParameterSpec
        .builder(name, type)
        .addAnnotation(jsonProperty(name))
        .build()

private fun property(name: String, type: TypeName, modifiers: List<KModifier>): PropertySpec =
    PropertySpec
        .builder(name, type)
        .addModifiers(modifiers)
        .build()

fun deserializeAs(asClassName: ClassName): AnnotationSpec =
    AnnotationSpec
        .builder(JsonDeserialize::class)
        .addMember(CodeBlock.of("`as` = %T::class", asClassName))
        .build()

fun deserializeUsing(asClassName: ClassName): AnnotationSpec =
    AnnotationSpec
        .builder(JsonDeserialize::class)
        .addMember(CodeBlock.of("using = %T::class", asClassName))
        .build()

fun jsonProperty(name: String): AnnotationSpec =
    AnnotationSpec
        .builder(JsonProperty::class)
        .addMember("%S", name)
        .build()