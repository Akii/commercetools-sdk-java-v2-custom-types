package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.TypedMoney
import com.commercetools.api.models.product.Attribute
import com.commercetools.api.models.product_type.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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
        .primaryConstructor(constructor(attributes, config))
        .addProperties(attributes.map { attribute(it, config) })
        .build()
}

private fun constructor(attributes: List<AttributeDefinition>, config: Configuration): FunSpec =
    FunSpec
        .constructorBuilder()
        .addParameters(attributes.map { parameter(it, config) })
        .build()

private fun parameter(attributeDefinition: AttributeDefinition, config: Configuration): ParameterSpec =
    ParameterSpec
        .builder(
            config.attributeNameToPropertyName(attributeDefinition.name),
            typeNameForAttributeType(attributeDefinition.type, config)
        )
        .addAnnotation(jsonProperty(config.attributeNameToPropertyName(attributeDefinition.name)))
        .build()

private fun attribute(attributeDefinition: AttributeDefinition, config: Configuration): PropertySpec =
    PropertySpec
        .builder(
            config.attributeNameToPropertyName(attributeDefinition.name),
            typeNameForAttributeType(attributeDefinition.type, config)
        )
        .initializer(config.attributeNameToPropertyName(attributeDefinition.name))
        .build()

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
        is AttributeReferenceType -> referenceTypeIdToClassName(attributeType.referenceTypeId)
        is AttributeSetType -> SET.parameterizedBy(typeNameForAttributeType(attributeType.elementType, config))
        is AttributeNestedType -> findProductVariantAttributesTypeByProductTypeId(attributeType.typeReference.id, config)
        else -> Any::class.asTypeName()
    }.copy(nullable = true)

private fun findProductVariantAttributesTypeByProductTypeId(productTypeId: String, config: Configuration): TypeName =
    when (val productType = config.productTypes.find { it.id == productTypeId }) {
        is ProductType -> ProductVariantAttributesClassName(productType, config).className
        else -> MUTABLE_LIST.parameterizedBy(Attribute::class.asTypeName())
    }
