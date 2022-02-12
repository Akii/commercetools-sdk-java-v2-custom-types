package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.TypedMoney
import com.commercetools.api.models.common.Reference
import com.commercetools.api.models.product.Attribute
import com.commercetools.api.models.product_type.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.Configuration
import de.akii.commercetools.api.customtypes.generator.common.*
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
        is AttributeBooleanType -> Boolean::class.asTypeName()
        is AttributeTextType -> String::class.asTypeName()
        is AttributeLocalizableTextType -> LocalizedString::class.asTypeName()
        is AttributeEnumType -> AttributePlainEnumValue::class.asTypeName()
        is AttributeLocalizedEnumValue -> AttributeLocalizedEnumValue::class.asTypeName()
        is AttributeNumberType -> Int::class.asTypeName()
        is AttributeMoneyType -> TypedMoney::class.asTypeName()
        is AttributeDateType -> LocalDate::class.asTypeName()
        is AttributeTimeType -> LocalTime::class.asTypeName()
        is AttributeDateTimeType -> ZonedDateTime::class.asTypeName()
        is AttributeReferenceType -> Reference::class.asTypeName()
        is AttributeSetType -> SET.parameterizedBy(typeNameForAttributeType(attributeType.elementType, config))
        is AttributeNestedType -> findProductVariantAttributesTypeByProductTypeId(attributeType.typeReference.id, config)
        else -> Any::class.asTypeName()
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
