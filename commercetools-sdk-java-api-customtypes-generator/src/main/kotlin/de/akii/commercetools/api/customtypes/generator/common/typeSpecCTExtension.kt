package de.akii.commercetools.api.customtypes.generator.common

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass

sealed class CTProperty
data class SimpleCTProperty(
    val name: String,
    val type: TypeName,
    val castedFrom: KClass<*>? = null,
    val nullable: Boolean = false,
    val modifiers: List<KModifier> = listOf(KModifier.OVERRIDE)
) : CTProperty() {
    constructor(
        name: String,
        type: KClass<*>,
        castedFrom: KClass<*>? = null,
        nullable: Boolean = false,
        modifiers: List<KModifier> = listOf(KModifier.OVERRIDE)
    ) : this(name, type.asTypeName(), castedFrom, nullable, modifiers)
}

data class ListCTProperty(
    val name: String,
    val type: ClassName,
    val parameterizedBy: TypeName,
    val castedFrom: KClass<*>? = null,
    val nullable: Boolean = false,
    val modifiers: List<KModifier> = listOf(KModifier.OVERRIDE)
) : CTProperty() {
    constructor(
        name: String,
        type: KClass<*>,
        parameterizedBy: TypeName,
        castedFrom: KClass<*>? = null,
        nullable: Boolean = false,
        modifiers: List<KModifier> = listOf(KModifier.OVERRIDE)
    ) : this(name, type.asTypeName(), parameterizedBy, castedFrom, nullable, modifiers)

    constructor(
        name: String,
        type: KClass<*>,
        parameterizedBy: KClass<*>,
        castedFrom: KClass<*>? = null,
        nullable: Boolean = false,
        modifiers: List<KModifier> = listOf(KModifier.OVERRIDE)
    ) : this(name, type.asTypeName(), parameterizedBy.asTypeName(), castedFrom, nullable, modifiers)
}

fun TypeSpec.Builder.addCTProperties(vararg properties: CTProperty): TypeSpec.Builder =
    this.addCTProperties(properties.asList())

fun TypeSpec.Builder.addCTProperties(properties: List<CTProperty>): TypeSpec.Builder {
    val attributes = properties.map {
        when (it) {
            is SimpleCTProperty -> attribute(it.name, it.type.copy(it.nullable))
            is ListCTProperty -> attribute(it.name, it.type.parameterizedBy(it.parameterizedBy).copy(it.nullable))
        }
    }

    this.primaryConstructor(
        FunSpec
            .constructorBuilder()
            .addAnnotation(jsonCreator)
            .addParameters(attributes.map { it.first })
            .build()
    )

    this.addProperties(
        attributes.map { it.second }
    )

    properties.forEach {
        when (it) {
            is SimpleCTProperty -> {
                this.addFunction(propertyGetter(it.name, it.type, it.nullable, it.modifiers))
                this.addFunction(propertySetter(it.name, it.type, it.nullable, it.castedFrom, it.modifiers))
            }
            is ListCTProperty -> {
                val parameterizedType = it.type.parameterizedBy(it.parameterizedBy)

                this.addFunction(propertyGetter(it.name, parameterizedType, it.nullable, it.modifiers))
                this.addFunction(propertyListSetter(it.name, it.type, it.parameterizedBy, it.nullable, it.castedFrom, it.modifiers))
                this.addFunction(propertyVarargSetter(it.name, it.parameterizedBy, it.castedFrom, it.modifiers))
            }
        }
    }

    return this
}

private fun attribute(name: String, type: TypeName): Pair<ParameterSpec, PropertySpec> {
    val parameter = ParameterSpec
        .builder(name, type)
        .addModifiers(KModifier.PRIVATE)
        .addAnnotation(jsonProperty(name))
        .build()

    val property = PropertySpec
        .builder(name, type)
        .addModifiers(KModifier.PRIVATE)
        .mutable(true)
        .initializer(name)
        .build()

    return parameter to property
}

private fun propertyGetter(name: String, type: TypeName, nullable: Boolean, modifiers: List<KModifier>): FunSpec =
    FunSpec
        .builder(propertyNameToFunctionName("get", name))
        .returns(type.copy(nullable))
        .addStatement("return this.%N", name)
        .addModifiers(modifiers)
        .build()

private fun propertySetter(
    name: String,
    type: TypeName,
    nullable: Boolean,
    castedFrom: KClass<*>?,
    modifiers: List<KModifier>
): FunSpec =
    if (castedFrom == null)
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, type.copy(nullable))
            .addStatement("this.%1N = %2N", name, name)
            .addModifiers(modifiers)
            .build()
    else
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, kclassToClassName(castedFrom).copy(nullable))
            .addStatement("this.${name} = $name as $type")
            .addModifiers(modifiers)
            .addAnnotation(suppressUncheckedCalls)
            .build()

private fun propertyListSetter(
    name: String,
    type: ClassName,
    parameterizedBy: TypeName,
    nullable: Boolean,
    castedFrom: KClass<*>?, modifiers: List<KModifier>
): FunSpec =
    if (castedFrom == null)
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, type.parameterizedBy(parameterizedBy).copy(nullable))
            .addStatement("this.%1N = %2N", name, name)
            .addModifiers(modifiers)
            .build()
    else
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, type.parameterizedBy(kclassToClassName(castedFrom)).copy(nullable))
            .addStatement("this.%1N = %2N as %3T", name, name, type.parameterizedBy(parameterizedBy).copy(nullable))
            .addModifiers(modifiers)
            .addAnnotation(suppressUncheckedCalls)
            .build()

private fun propertyVarargSetter(
    name: String,
    type: TypeName,
    castedFrom: KClass<*>?,
    modifiers: List<KModifier>
): FunSpec =
    if (castedFrom == null)
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, type, KModifier.VARARG)
            .addStatement("this.%1N = %2N.asList().toMutableList()", name, name)
            .addModifiers(modifiers)
            .build()
    else
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, kclassToClassName(castedFrom), KModifier.VARARG)
            .addStatement("this.%1N = %2N.asList().toMutableList() as kotlin.collections.MutableList<$type>", name, name)
            .addModifiers(modifiers)
            .addAnnotation(suppressUncheckedCalls)
            .build()

private fun propertyNameToFunctionName(prefix: String, attributeName: String): String =
    "${prefix}${attributeName.replaceFirstChar { it.uppercase() }}"

private fun kclassToClassName(type: KClass<*>): ClassName =
    when (type) {
        MutableList::class -> ClassName("kotlin.collections", "MutableList")
        else -> type.asTypeName()
    }

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

private val jsonCreator =
    AnnotationSpec
        .builder(JsonCreator::class)
        .build()

private fun jsonProperty(name: String): AnnotationSpec =
    AnnotationSpec
        .builder(JsonProperty::class)
        .addMember("%S", name)
        .build()

private val suppressUncheckedCalls =
    AnnotationSpec
        .builder(Suppress::class)
        .addMember("%S", "UNCHECKED_CAST")
        .build()