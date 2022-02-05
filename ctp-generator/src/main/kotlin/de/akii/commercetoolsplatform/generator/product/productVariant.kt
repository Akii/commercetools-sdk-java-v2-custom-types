package de.akii.commercetoolsplatform.generator

import com.squareup.kotlinpoet.*
import de.akii.commercetoolsplatform.types.common.*
import de.akii.commercetoolsplatform.types.producttype.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.annotation.processing.Generated

fun generateCodeForProductVariant(
    productName: String,
    packageName: String,
    attributesClassName: ClassName
): Pair<ClassName, TypeSpec> {
    val className = ClassName(packageName, "${productName}ProductVariant")
    val type = TypeSpec
        .classBuilder(className)
        .addAnnotation(Generated::class)
        .addAnnotation(serializableBy(Serializable::class))
        .addProperty("attributes", attributesClassName)
        .build()

    return className to type
}

fun generateCodeForProductVariantAttributes(
    productName: String,
    packageName: String,
    attributes: List<AttributeDefinition>
): Pair<ClassName, TypeSpec> {
    val className = ClassName(packageName, "${productName}ProductVariantAttributes")
    val type = TypeSpec
        .classBuilder(className)
        .addAnnotation(Generated::class)
        .addAnnotation(serializableBy(Serializable::class))
        .addProperties(
            attributes.map { makeAttributeProperty(it) }
        )
        .build()

    return className to type
}

private fun makeAttributeProperty(attributeDefinition: AttributeDefinition): PropertySpec =
    PropertySpec
        .builder(
            attributeNameToPropertyName(attributeDefinition.name),
            getTypeNameForAttributeType(attributeDefinition.type)
        )
        .addAnnotations(
            getAnnotationsForAttributeType(attributeDefinition.type)
        )
        .build()

private fun getTypeNameForAttributeType(attributeType: AttributeType): TypeName =
    when (attributeType) {
        is BooleanType -> classTypeName(Boolean::class)
        is TextType -> classTypeName(String::class)
        is LocalizableTextType -> localizedStringType
        is EnumType -> classTypeName(EnumValue::class)
        is LocalizableEnumType -> classTypeName(LocalizedEnumValue::class)
        is NumberType -> classTypeName(Int::class)
        is MoneyType -> classTypeName(Money::class)
        is DateType -> classTypeName(LocalDate::class)
        is TimeType -> classTypeName(LocalTime::class)
        is DateTimeType -> classTypeName(LocalDateTime::class)
        is ReferenceType -> classTypeName(Reference::class)
        is SetType -> listTypeName(JsonObject::class) // TODO: implement
        is NestedType -> classTypeName(Reference::class)
    }

private fun getAnnotationsForAttributeType(attributeType: AttributeType): List<AnnotationSpec> =
    when (attributeType) {
        is DateTimeType -> listOf(serializableBy(CTPLocalDateTimeSerializer::class))
        is MoneyType -> listOf(serializableBy(MoneySerializer::class))
        else -> emptyList()
    }
