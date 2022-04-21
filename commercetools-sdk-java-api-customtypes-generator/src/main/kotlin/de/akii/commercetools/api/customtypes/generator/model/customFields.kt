package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.cart.CartReference
import com.commercetools.api.models.category.CategoryReference
import com.commercetools.api.models.channel.ChannelReference
import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.Reference
import com.commercetools.api.models.common.TypedMoney
import com.commercetools.api.models.custom_object.CustomObjectReference
import com.commercetools.api.models.customer.CustomerReference
import com.commercetools.api.models.order.OrderReference
import com.commercetools.api.models.product.ProductReference
import com.commercetools.api.models.product_type.ProductTypeReference
import com.commercetools.api.models.review.ReviewReference
import com.commercetools.api.models.shipping_method.ShippingMethodReference
import com.commercetools.api.models.state.StateReference
import com.commercetools.api.models.type.*
import com.commercetools.api.models.zone.ZoneReference
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

fun typedCustomField(type: Type, config: Configuration): TypeSpec {
    val className = TypedCustomFields(type, config).className
    val fields = typedFields(type, config)

    return TypeSpec
        .classBuilder(className)
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(className))
        .primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addAnnotation(jsonCreator)
                .addParameter(
                    ParameterSpec
                        .builder("fields", CustomFieldsImpl::class)
                        .addAnnotation(jsonProperty("fields"))
                        .build()
                )
                .addParameter(
                    ParameterSpec
                        .builder("typedFields", ClassName("", "Fields"))
                        .addAnnotation(jsonProperty("typedFields"))
                        .build()
                )
                .build()
        )
        .addSuperinterface(CustomFields::class, "fields")
        .addType(fields)
        .addType(customFieldCompanionObject(type, type.fieldDefinitions, config))
        .addProperty(
            PropertySpec
                .builder("typedFields", ClassName("", "Fields"))
                .mutable()
                .initializer("typedFields")
                .build()
        )
        .build()
}

private fun typedFields(type: Type, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder("Fields")
        .addModifiers(
            if (type.fieldDefinitions.isEmpty())
                emptyList()
            else
                listOf(KModifier.DATA)
        )
        .primaryConstructor(constructor(type, config))
        .addProperties(type.fieldDefinitions.map { attribute(type, it, config) })
        .build()

private fun constructor(type: Type, config: Configuration): FunSpec =
    FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameters(type.fieldDefinitions.map { parameter(type, it, config) })
        .build()

private fun parameter(type: Type, fieldDefinition: FieldDefinition, config: Configuration): ParameterSpec =
    ParameterSpec
        .builder(
            config.fieldToPropertyName(type, fieldDefinition),
            typeNameForFieldType(fieldDefinition.type, isFieldRequired(type, fieldDefinition, config), config)
        )
        .addAnnotation(jsonProperty(fieldDefinition.name))
        .build()

private fun attribute(type: Type, fieldDefinition: FieldDefinition, config: Configuration): PropertySpec =
    PropertySpec
        .builder(
            config.fieldToPropertyName(type, fieldDefinition),
            typeNameForFieldType(fieldDefinition.type, isFieldRequired(type, fieldDefinition, config), config)
        )
        .initializer(config.fieldToPropertyName(type, fieldDefinition))
        .build()

private fun typeNameForFieldType(fieldType: FieldType, isRequired: Boolean, config: Configuration): TypeName =
    when (fieldType) {
        is CustomFieldBooleanType -> Boolean::class.asTypeName()
        is CustomFieldStringType -> String::class.asTypeName()
        is CustomFieldLocalizedStringType -> LocalizedString::class.asTypeName()
        is CustomFieldEnumType -> CustomFieldEnumValue::class.asTypeName()
        is CustomFieldLocalizedEnumType -> CustomFieldLocalizedEnumValue::class.asTypeName()
        is CustomFieldNumberType -> Double::class.asTypeName()
        is CustomFieldMoneyType -> TypedMoney::class.asTypeName()
        is CustomFieldDateType -> LocalDate::class.asTypeName()
        is CustomFieldTimeType -> LocalTime::class.asTypeName()
        is CustomFieldDateTimeType -> ZonedDateTime::class.asTypeName()
        is CustomFieldReferenceType -> customFieldReferenceTypeIdToClassName(fieldType.referenceTypeId)
        is CustomFieldSetType -> SET.parameterizedBy(typeNameForFieldType(fieldType.elementType, isRequired, config))
        else -> Any::class.asTypeName()
    }.copy(nullable = !isRequired)

private fun customFieldReferenceTypeIdToClassName(referenceTypeId: CustomFieldReferenceValue): ClassName =
    when (referenceTypeId) {
        CustomFieldReferenceValue.CART -> CartReference::class.asClassName()
        CustomFieldReferenceValue.CATEGORY -> CategoryReference::class.asClassName()
        CustomFieldReferenceValue.CHANNEL -> ChannelReference::class.asClassName()
        CustomFieldReferenceValue.CUSTOMER -> CustomerReference::class.asClassName()
        CustomFieldReferenceValue.KEY_VALUE_DOCUMENT -> CustomObjectReference::class.asClassName()
        CustomFieldReferenceValue.ORDER -> OrderReference::class.asClassName()
        CustomFieldReferenceValue.PRODUCT -> ProductReference::class.asClassName()
        CustomFieldReferenceValue.PRODUCT_TYPE -> ProductTypeReference::class.asClassName()
        CustomFieldReferenceValue.REVIEW -> ReviewReference::class.asClassName()
        CustomFieldReferenceValue.SHIPPING_METHOD -> ShippingMethodReference::class.asClassName()
        CustomFieldReferenceValue.STATE -> StateReference::class.asClassName()
        CustomFieldReferenceValue.ZONE -> ZoneReference::class.asClassName()
        else -> Reference::class.asClassName()
    }

private fun customFieldCompanionObject(type: Type, fieldDefinitions: List<FieldDefinition>, config: Configuration): TypeSpec =
    TypeSpec
        .companionObjectBuilder()
        .addProperties(fieldDefinitions.map {
            PropertySpec
                .builder(fieldToConstantName(type, it, config), String::class)
                .addModifiers(KModifier.CONST)
                .initializer("%S", it.name)
                .build()
        })
        .build()

private fun isFieldRequired(type: Type, fieldDefinition: FieldDefinition, config: Configuration): Boolean =
    config.isFieldRequired(type, fieldDefinition)

private fun fieldToConstantName(type: Type, fieldDefinition: FieldDefinition, config: Configuration) =
    config.fieldToPropertyName(type, fieldDefinition)
        .split(Regex("(?=\\p{Upper})"))
        .joinToString("_") { it.uppercase() }