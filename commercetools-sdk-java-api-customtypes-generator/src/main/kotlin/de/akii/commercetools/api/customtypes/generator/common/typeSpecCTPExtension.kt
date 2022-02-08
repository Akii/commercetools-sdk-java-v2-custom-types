package de.akii.commercetools.api.customtypes.generator.common

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass

const val MUTABLE_LIST_INITIALIZER = "kotlin.collections.mutableListOf()"
const val ZONED_DATE_TIME_INITIALIZER = "java.time.ZonedDateTime.now()"

fun initializerFor(kclass: KClass<*>): String =
    initializerFor(kclass.asTypeName())

fun initializerFor(className: ClassName): String =
    "${className.canonicalName}()"

fun TypeSpec.Builder.addCTPProperty(name: String, type: KClass<*>, nullable: Boolean = false, initializer: String? = null, castedFrom: KClass<*>? = null): TypeSpec.Builder =
    this.addCTPProperty(name, kclassToClassName(type), nullable, initializer, castedFrom)

fun TypeSpec.Builder.addCTPProperty(name: String, type: TypeName, nullable: Boolean = false, initializer: String? = null, castedFrom: KClass<*>? = null): TypeSpec.Builder {
    val property = property(name, type, nullable, initializer)
    val getter = propertyGetter(name, type, nullable)
    val setter = propertySetter(name, type, nullable, castedFrom)

    return this
        .addProperty(property)
        .addFunction(getter)
        .addFunction(setter)
}

fun TypeSpec.Builder.addCTPProperty(name: String, type: KClass<*>, parameterizedBy: KClass<*>, nullable: Boolean = false, initializer: String? = null, castedFrom: KClass<*>? = null): TypeSpec.Builder =
    this.addCTPProperty(name, type, kclassToClassName(parameterizedBy), nullable, initializer, castedFrom)

fun TypeSpec.Builder.addCTPProperty(name: String, type: KClass<*>, parameterizedBy: TypeName, nullable: Boolean = false, initializer: String? = null, castedFrom: KClass<*>? = null): TypeSpec.Builder {
    val parameterizedType = kclassToClassName(type).parameterizedBy(parameterizedBy)
    val builder = this
        .addProperty(property(name, parameterizedType, nullable, initializer))
        .addFunction(propertyGetter(name, parameterizedType, nullable))

    // sketchy but doesn't require reflection
    if (type == List::class || type == MutableList::class) {
        builder.addFunction(propertyListSetter(name, kclassToClassName(type), parameterizedBy, nullable, castedFrom))
        builder.addFunction(propertyVarargSetter(name, parameterizedBy, castedFrom))
    }

    return builder
}

private fun property(name: String, type: TypeName, nullable: Boolean = false, initializer: String? = null): PropertySpec {
    val property = PropertySpec
        .builder(name, type.copy(nullable))
        .addModifiers(KModifier.PRIVATE)
        .mutable(true)

    if (initializer != null) {
        property.initializer(initializer)
    } else if (nullable) {
        property.initializer("null")
    }

    return property.build()
}

private fun propertyGetter(name: String, type: TypeName, nullable: Boolean): FunSpec =
    FunSpec
        .builder(propertyNameToFunctionName("get", name))
        .returns(type.copy(nullable))
        .addStatement("return this.${name}")
        .addModifiers(KModifier.OVERRIDE)
        .build()

private fun propertySetter(name: String, type: TypeName, nullable: Boolean, castedFrom: KClass<*>?): FunSpec =
    if (castedFrom == null)
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, type.copy(nullable))
            .addStatement("this.${name} = $name")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    else
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, kclassToClassName(castedFrom).copy(nullable))
            .addStatement("this.${name} = $name as $type")
            .addModifiers(KModifier.OVERRIDE)
            .addAnnotation(suppressUncheckedCalls)
            .build()

private fun propertyListSetter(name: String, type: ClassName, parameterizedBy: TypeName, nullable: Boolean, castedFrom: KClass<*>?): FunSpec =
    if (castedFrom == null)
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, type.parameterizedBy(parameterizedBy).copy(nullable))
            .addStatement("this.${name} = $name")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    else
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, type.parameterizedBy(kclassToClassName(castedFrom)).copy(nullable))
            .addStatement("this.${name} = $name as ${type.parameterizedBy(parameterizedBy).copy(nullable)}")
            .addModifiers(KModifier.OVERRIDE)
            .addAnnotation(suppressUncheckedCalls)
            .build()

private fun propertyVarargSetter(name: String, type: TypeName, castedFrom: KClass<*>?): FunSpec =
    if (castedFrom == null)
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, type, KModifier.VARARG)
            .addStatement("this.${name} = ${name}.asList().toMutableList()")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    else
        FunSpec
            .builder(propertyNameToFunctionName("set", name))
            .addParameter(name, kclassToClassName(castedFrom), KModifier.VARARG)
            .addStatement("this.${name} = ${name}.asList().toMutableList() as kotlin.collections.MutableList<$type>")
            .addModifiers(KModifier.OVERRIDE)
            .addAnnotation(suppressUncheckedCalls)
            .build()

private fun propertyNameToFunctionName(prefix: String, attributeName: String): String =
    "${prefix}${attributeName.replaceFirstChar { it.uppercase() }}"

private fun kclassToClassName(type: KClass<*>): ClassName =
    when (type) {
        MutableList::class -> ClassName("kotlin.collections", "MutableList")
        else -> type.asTypeName()
    }

private val suppressUncheckedCalls =
    AnnotationSpec
        .builder(Suppress::class)
        .addMember("\"UNCHECKED_CAST\"")
        .build()