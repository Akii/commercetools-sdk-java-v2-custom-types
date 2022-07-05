package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.cart.CartReference
import com.commercetools.api.models.category.CategoryReference
import com.commercetools.api.models.channel.ChannelReference
import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.Reference
import com.commercetools.api.models.common.TypedMoney
import com.commercetools.api.models.custom_object.CustomObjectReference
import com.commercetools.api.models.customer.CustomerReference
import com.commercetools.api.models.order.OrderReference
import com.commercetools.api.models.product.Attribute
import com.commercetools.api.models.product.ProductReference
import com.commercetools.api.models.product_type.*
import com.commercetools.api.models.review.ReviewReference
import com.commercetools.api.models.shipping_method.ShippingMethodReference
import com.commercetools.api.models.state.StateReference
import com.commercetools.api.models.zone.ZoneReference
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

fun productVariantAttributes(
    typedProductVariantAttributesClassName: TypedProductVariantAttributes,
    typedProductVariantAttributesInterfaceClassName: TypedProductVariantAttributesInterface,
    productType: ProductType,
    config: Configuration
): TypeSpec {
    return TypeSpec
        .classBuilder(typedProductVariantAttributesClassName.className)
        .addModifiers(
            if (productType.attributes.isEmpty())
                emptyList()
            else
                listOf(KModifier.DATA)
        )
        .addSuperinterface(typedProductVariantAttributesInterfaceClassName.className)
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(typedProductVariantAttributesClassName.className))
        .primaryConstructor(constructor(productType, config))
        .addProperties(productType.attributes.map { attribute(productType, it, config) })
        .addType(productVariantAttributesCompanionObject(productType, productType.attributes, config))
        .build()
}

private fun constructor(productType: ProductType, config: Configuration): FunSpec =
    FunSpec
        .constructorBuilder()
        .addParameters(productType.attributes.map { parameter(productType, it, config) })
        .build()

private fun parameter(productType: ProductType, attributeDefinition: AttributeDefinition, config: Configuration): ParameterSpec =
    ParameterSpec
        .builder(
            config.attributeToPropertyName(productType, attributeDefinition),
            typeNameForAttributeType(attributeDefinition.type, isAttributeRequired(productType, attributeDefinition, config), config)
        )
        .addAnnotation(jsonProperty(attributeDefinition.name))
        .build()

private fun attribute(productType: ProductType, attributeDefinition: AttributeDefinition, config: Configuration): PropertySpec =
    PropertySpec
        .builder(
            config.attributeToPropertyName(productType, attributeDefinition),
            typeNameForAttributeType(attributeDefinition.type, isAttributeRequired(productType, attributeDefinition, config), config)
        )
        .initializer(config.attributeToPropertyName(productType, attributeDefinition))
        .build()

private fun typeNameForAttributeType(attributeType: AttributeType, isRequired: Boolean, config: Configuration): TypeName =
    when (attributeType) {
        is AttributeBooleanType -> Boolean::class.asTypeName()
        is AttributeTextType -> String::class.asTypeName()
        is AttributeLocalizableTextType -> LocalizedString::class.asTypeName()
        is AttributeEnumType -> AttributePlainEnumValue::class.asTypeName()
        is AttributeLocalizedEnumValue -> AttributeLocalizedEnumValue::class.asTypeName()
        is AttributeNumberType -> Double::class.asTypeName()
        is AttributeMoneyType -> TypedMoney::class.asTypeName()
        is AttributeDateType -> LocalDate::class.asTypeName()
        is AttributeTimeType -> LocalTime::class.asTypeName()
        is AttributeDateTimeType -> ZonedDateTime::class.asTypeName()
        is AttributeReferenceType -> referenceTypeIdToClassName(attributeType.referenceTypeId)
        is AttributeSetType -> SET.parameterizedBy(typeNameForAttributeType(attributeType.elementType, isRequired, config))
        is AttributeNestedType -> findProductVariantAttributesTypeByProductTypeId(attributeType.typeReference.id, config)
        else -> Any::class.asTypeName()
    }.copy(nullable = !isRequired)

private fun referenceTypeIdToClassName(referenceTypeId: AttributeReferenceTypeId): ClassName =
    when (referenceTypeId) {
        AttributeReferenceTypeId.CART -> CartReference::class.asClassName()
        AttributeReferenceTypeId.CATEGORY -> CategoryReference::class.asClassName()
        AttributeReferenceTypeId.CHANNEL -> ChannelReference::class.asClassName()
        AttributeReferenceTypeId.CUSTOMER -> CustomerReference::class.asClassName()
        AttributeReferenceTypeId.KEY_VALUE_DOCUMENT -> CustomObjectReference::class.asClassName()
        AttributeReferenceTypeId.ORDER -> OrderReference::class.asClassName()
        AttributeReferenceTypeId.PRODUCT -> ProductReference::class.asClassName()
        AttributeReferenceTypeId.PRODUCT_TYPE -> ProductTypeReference::class.asClassName()
        AttributeReferenceTypeId.REVIEW -> ReviewReference::class.asClassName()
        AttributeReferenceTypeId.SHIPPING_METHOD -> ShippingMethodReference::class.asClassName()
        AttributeReferenceTypeId.STATE -> StateReference::class.asClassName()
        AttributeReferenceTypeId.ZONE -> ZoneReference::class.asClassName()
        else -> Reference::class.asClassName()
    }

private fun findProductVariantAttributesTypeByProductTypeId(productTypeId: String, config: Configuration): TypeName =
    when (val productType = config.productTypes.find { it.id == productTypeId }) {
        is ProductType -> TypedProductVariantAttributes(productType, config).className
        else -> MUTABLE_LIST.parameterizedBy(Attribute::class.asTypeName())
    }

private fun productVariantAttributesCompanionObject(productType: ProductType, attributes: List<AttributeDefinition>, config: Configuration): TypeSpec =
    TypeSpec
        .companionObjectBuilder()
        .addProperties(attributes.map {
            PropertySpec
                .builder(attributeToConstantName(productType, it, config), String::class)
                .addModifiers(KModifier.CONST)
                .initializer("%S", it.name)
                .build()
        })
        .build()

private fun isAttributeRequired(productType: ProductType, attributeDefinition: AttributeDefinition, config: Configuration): Boolean =
    config.isAttributeRequired(productType, attributeDefinition)

private fun attributeToConstantName(productType: ProductType, attributeDefinition: AttributeDefinition, config: Configuration) =
    config.attributeToPropertyName(productType, attributeDefinition)
        .split(Regex("(?=\\p{Upper})"))
        .joinToString("_") { it.uppercase() }