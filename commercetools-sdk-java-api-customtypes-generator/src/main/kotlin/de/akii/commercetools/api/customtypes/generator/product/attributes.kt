package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.TypedMoney
import com.commercetools.api.models.common.Reference
import com.commercetools.api.models.product_type.AttributeLocalizedEnumValue
import com.commercetools.api.models.product_type.AttributePlainEnumValue
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.Configuration
import de.akii.commercetools.api.customtypes.generator.common.ProductVariantAttributesClassName
import de.akii.commercetools.api.customtypes.generator.types.*
import io.vrap.rmf.base.client.utils.Generated
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

fun generateProductVariantAttributes(
    productVariantAttributesClassName: ProductVariantAttributesClassName,
    attributes: List<AttributeDefinition>,
    config: Configuration
): TypeSpec {
    val generatedAttributes = attributes.map { generateAttribute(it, config) }
    val modifiers = if (attributes.isEmpty()) emptyList() else listOf(KModifier.DATA)

    return TypeSpec
        .classBuilder(productVariantAttributesClassName.className)
        .addModifiers(modifiers)
        .addAnnotation(Generated::class)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameters(generatedAttributes.map { it.first })
                .build()
        )
        .addProperties(generatedAttributes.map { it.second })
        .build()
}

private fun generateAttribute(attributeDefinition: AttributeDefinition, config: Configuration): Pair<ParameterSpec, PropertySpec> {
    val attributeName = attributeNameToPropertyName(attributeDefinition.name)
    val attributeType = getTypeNameForAttributeType(attributeDefinition.type, config)
        .copy(nullable = !attributeDefinition.isRequired)

    val parameter = ParameterSpec.builder(attributeName, attributeType)

    if (!attributeDefinition.isRequired) {
        parameter.defaultValue("null")
    }

    val property = PropertySpec
        .builder(attributeName, attributeType)
        .initializer(attributeName)
        .build()

    return parameter.build() to property
}

private fun getTypeNameForAttributeType(attributeType: AttributeType, config: Configuration): TypeName =
    when (attributeType) {
        is BooleanType -> Boolean::class.asTypeName()
        is TextType -> String::class.asTypeName()
        is LocalizableTextType -> LocalizedString::class.asTypeName()
        is EnumType -> AttributePlainEnumValue::class.asTypeName()
        is LocalizableEnumType -> AttributeLocalizedEnumValue::class.asTypeName()
        is NumberType -> Int::class.asTypeName()
        is MoneyType -> TypedMoney::class.asTypeName()
        is DateType -> LocalDate::class.asTypeName()
        is TimeType -> LocalTime::class.asTypeName()
        is DateTimeType -> ZonedDateTime::class.asTypeName()
        is ReferenceType -> Reference::class.asTypeName()
        is SetType -> SET.parameterizedBy(getTypeNameForAttributeType(attributeType.elementType, config))
        is NestedType -> ProductVariantAttributesClassName(attributeType.typeReference.id, config).className
    }

fun attributeNameToPropertyName(attributeName: String): String {
    return attributeName
        .split('-', '_', ' ')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
        .replaceFirstChar { it.lowercase() }
}