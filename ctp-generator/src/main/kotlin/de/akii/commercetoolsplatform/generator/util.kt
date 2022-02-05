package de.akii.commercetoolsplatform.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetoolsplatform.types.common.CTPLocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KClass


fun dateTimeProperty(name: String): PropertySpec =
    PropertySpec
        .builder(name, LocalDateTime::class)
        .addAnnotation(serializableBy(CTPLocalDateTimeSerializer::class))
        .build()

val localizedStringType =
    ClassName("de.akii.commercetoolsplatform.common", "LocalizedString")

fun classTypeName(kclass: KClass<*>): TypeName =
    ClassName(kclass.java.packageName, kclass.simpleName!!)

fun listTypeName(kclass: KClass<*>): ParameterizedTypeName =
    LIST.parameterizedBy(classTypeName(kclass))

fun listTypeName(className: ClassName): ParameterizedTypeName =
    LIST.parameterizedBy(className)

fun mapTypeName(keyKClass: KClass<*>, valueKClass: KClass<*>): ParameterizedTypeName =
    MAP.parameterizedBy(
        classTypeName(keyKClass),
        classTypeName(valueKClass),
    )

fun serializableBy(className: ClassName): AnnotationSpec =
    AnnotationSpec
        .builder(Serializable::class)
        .addMember("${className.canonicalName}::class")
        .build()

fun serializableBy(kclass: KClass<*>): AnnotationSpec =
    AnnotationSpec
        .builder(Serializable::class)
        .addMember("${kclass.qualifiedName!!}::class")
        .build()

fun productTypeNameToClassName(productTypeName: String) =
    productTypeName
        .split('-', '_')
        .joinToString("") { part ->
            part.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
            }
        }

fun attributeNameToPropertyName(attributeName: String): String {
    return productTypeNameToClassName(attributeName).replaceFirstChar { it.lowercase() }
}