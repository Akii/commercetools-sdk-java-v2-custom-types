package de.akii.commercetools.api.customtypes.generator.common

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass

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
    val modifiers: List<KModifier> = listOf(KModifier.PRIVATE, KModifier.OVERRIDE)
) : ConstructorArgument(name, type, parameterizedBy, nullable)

fun TypeSpec.Builder.addCTConstructorArguments(vararg constructorArguments: ConstructorArgument): TypeSpec.Builder =
    this.addCTConstructorArguments(constructorArguments.asList())

fun TypeSpec.Builder.addCTConstructorArguments(constructorArguments: List<ConstructorArgument>): TypeSpec.Builder {
    val attributes = constructorArguments.map {
        when (it) {
            is CTParameter -> parameter(it.name, it.typeName) to null
            is CTProperty -> parameter(it.name, it.typeName) to property(
                it.name,
                it.typeName
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
    this.addFunctions(constructorArguments
        .filterIsInstance(CTProperty::class.java)
        .map { getter(it.name, it.typeName, it.modifiers) }
    )

    return this
}

private fun parameter(name: String, type: TypeName): ParameterSpec =
    ParameterSpec
        .builder(name, type)
        .addAnnotation(jsonProperty(name))
        .build()

private fun property(name: String, type: TypeName): PropertySpec =
    PropertySpec
        .builder(name, type)
        .addModifiers(KModifier.PRIVATE)
        .build()

private fun getter(name: String, type: TypeName, modifiers: List<KModifier>): FunSpec =
    FunSpec
        .builder("get${name.replaceFirstChar { it.uppercase() }}")
        .addModifiers(modifiers.filter { it != KModifier.PRIVATE })
        .addStatement("return this.%N", name)
        .returns(type)
        .build()