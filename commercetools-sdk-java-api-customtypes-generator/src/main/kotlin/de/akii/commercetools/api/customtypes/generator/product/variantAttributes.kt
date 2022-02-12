package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.TypedMoney
import com.commercetools.api.models.common.Reference
import com.commercetools.api.models.product.Attribute
import com.commercetools.api.models.product_type.AttributeLocalizedEnumValue
import com.commercetools.api.models.product_type.AttributePlainEnumValue
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.Configuration
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.types.*
import io.vrap.rmf.base.client.utils.Generated
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

fun productVariantAttributes(
    productVariantAttributesClassName: ProductVariantAttributesClassName,
    customProductVariantAttributesClassName: CustomProductVariantAttributesClassName,
    attributes: List<AttributeDefinition>,
    config: Configuration
): TypeSpec {
    return TypeSpec
        .classBuilder(productVariantAttributesClassName.className)
        .addModifiers(
            if (attributes.isEmpty())
                emptyList()
            else
                listOf(KModifier.DATA)
        )
        .addSuperinterface(customProductVariantAttributesClassName.className)
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(productVariantAttributesClassName.className))
        .addCTProperties(attributes.map { attribute(it, config) })
        .build()
}

private fun attribute(attributeDefinition: AttributeDefinition, config: Configuration): CTProperty =
    SimpleCTProperty(
        attributeNameToPropertyName(attributeDefinition.name),
        typeNameForAttributeType(attributeDefinition.type, config),
        nullable = true,
        modifiers = emptyList()
    )

private fun typeNameForAttributeType(attributeType: AttributeType, config: Configuration): TypeName =
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
        is SetType -> SET.parameterizedBy(typeNameForAttributeType(attributeType.elementType, config))
        is NestedType -> findProductVariantAttributesTypeByProductTypeId(attributeType.typeReference.id, config)
    }

private fun attributeNameToPropertyName(attributeName: String): String {
    return attributeName
        .split('-', '_', ' ')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
        .replaceFirstChar { it.lowercase() }
}

private fun findProductVariantAttributesTypeByProductTypeId(productTypeId: String, config: Configuration): TypeName =
    when (val productType = config.productTypes.find { it.id == productTypeId }) {
        is ProductType -> ProductVariantAttributesClassName(productType.name, config).className
        else -> MUTABLE_LIST.parameterizedBy(Attribute::class.asTypeName())
    }
