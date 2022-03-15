package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.cart.CartReference
import com.commercetools.api.models.category.CategoryReference
import com.commercetools.api.models.channel.ChannelReference
import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.Reference
import com.commercetools.api.models.common.TypedMoney
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
import io.vrap.rmf.base.client.utils.Generated
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

fun customFieldsFile(types: List<Type>, config: Configuration): FileSpec {
    val customFieldsFile = FileSpec
        .builder("${config.packageName}.custom_fields", "typedCustomFields")

    sealedOrderCustomFieldInterfaces(types, config).forEach {
        customFieldsFile.addType(it)
    }

    types.forEach {
        customFieldsFile.addType(typedCustomField(it, config))
    }

    return customFieldsFile.build()
}

private fun sealedOrderCustomFieldInterfaces(types: List<Type>, config: Configuration): List<TypeSpec> =
    types
        .flatMap { it.resourceTypeIds }
        .toSet()
        .map {
            TypeSpec
                .interfaceBuilder(resourceTypeIdToClassName(it, config))
                .addAnnotation(Generated::class)
                .addModifiers(KModifier.SEALED)
                .addSuperinterface(CustomFields::class)
                .build()
        }

private fun typedCustomField(type: Type, config: Configuration): TypeSpec {
    val className = typeToClassName(type, config)
    val fields = typedFields(type.fieldDefinitions, config)

    return TypeSpec
        .classBuilder(className)
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(className))
        .primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addAnnotation(jsonCreator)
                .addParameter(ParameterSpec
                    .builder("custom", CustomFieldsImpl::class)
                    .addAnnotation(jsonProperty("custom"))
                    .build()
                )
                .addParameter(ParameterSpec
                    .builder("typedFields", ClassName("", "Fields"))
                    .addAnnotation(jsonProperty("typedFields"))
                    .build()
                )
                .build()
        )
        .addSuperinterface(CustomFields::class, "custom")
        .addSuperinterfaces(type.resourceTypeIds.map { resourceTypeIdToClassName(it, config) })
        .addType(fields)
        .addProperty(PropertySpec
            .builder("typedFields", ClassName("", "Fields"))
            .initializer("typedFields")
            .build()
        )
        .build()
}

private fun typedFields(fieldDefinitions: List<FieldDefinition>, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder("Fields")
        .addModifiers(
            if (fieldDefinitions.isEmpty())
                emptyList()
            else
                listOf(KModifier.DATA)
        )
        .addAnnotation(Generated::class)
        .primaryConstructor(constructor(fieldDefinitions, config))
        .addProperties(fieldDefinitions.map { attribute(it, config) })
        .build()

private fun constructor(fieldDefinitions: List<FieldDefinition>, config: Configuration): FunSpec =
    FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameters(fieldDefinitions.map { parameter(it, config) })
        .build()

private fun parameter(fieldDefinition: FieldDefinition, config: Configuration): ParameterSpec =
    ParameterSpec
        .builder(
            config.attributeNameToPropertyName(fieldDefinition.name),
            typeNameForFieldType(fieldDefinition.type, config)
        )
        .addAnnotation(jsonProperty(fieldDefinition.name))
        .build()

private fun attribute(fieldDefinition: FieldDefinition, config: Configuration): PropertySpec =
    PropertySpec
        .builder(
            config.attributeNameToPropertyName(fieldDefinition.name),
            typeNameForFieldType(fieldDefinition.type, config)
        )
        .initializer(config.attributeNameToPropertyName(fieldDefinition.name))
        .build()

private fun typeNameForFieldType(fieldType: FieldType, config: Configuration): TypeName =
    when (fieldType) {
        is CustomFieldBooleanType -> Boolean::class.asTypeName()
        is CustomFieldStringType -> String::class.asTypeName()
        is CustomFieldLocalizedStringType -> LocalizedString::class.asTypeName()
        is CustomFieldEnumType -> CustomFieldEnumValue::class.asTypeName()
        is CustomFieldLocalizedEnumValue -> CustomFieldLocalizedEnumValue::class.asTypeName()
        is CustomFieldNumberType -> Int::class.asTypeName()
        is CustomFieldMoneyType -> TypedMoney::class.asTypeName()
        is CustomFieldDateType -> LocalDate::class.asTypeName()
        is CustomFieldTimeType -> LocalTime::class.asTypeName()
        is CustomFieldDateTimeType -> ZonedDateTime::class.asTypeName()
        is CustomFieldReferenceType -> customFieldReferenceTypeIdToClassName(fieldType.referenceTypeId)
        is CustomFieldSetType -> SET.parameterizedBy(typeNameForFieldType(fieldType.elementType, config))
        else -> Any::class.asTypeName()
    }.copy(nullable = true)

private fun customFieldReferenceTypeIdToClassName(referenceTypeId: CustomFieldReferenceValue): ClassName =
    when (referenceTypeId) {
        CustomFieldReferenceValue.CART -> CartReference::class.asClassName()
        CustomFieldReferenceValue.CATEGORY -> CategoryReference::class.asClassName()
        CustomFieldReferenceValue.CHANNEL -> ChannelReference::class.asClassName()
        CustomFieldReferenceValue.CUSTOMER -> CustomerReference::class.asClassName()
        CustomFieldReferenceValue.ORDER -> OrderReference::class.asClassName()
        CustomFieldReferenceValue.PRODUCT -> ProductReference::class.asClassName()
        CustomFieldReferenceValue.PRODUCT_TYPE -> ProductTypeReference::class.asClassName()
        CustomFieldReferenceValue.REVIEW -> ReviewReference::class.asClassName()
        CustomFieldReferenceValue.SHIPPING_METHOD -> ShippingMethodReference::class.asClassName()
        CustomFieldReferenceValue.STATE -> StateReference::class.asClassName()
        CustomFieldReferenceValue.ZONE -> ZoneReference::class.asClassName()
        else -> Reference::class.asClassName()
    }

fun typeToClassName(type: Type, config: Configuration): ClassName =
    ClassName(
        "${config.packageName}.custom_fields",
        classNamePrefix(type.key) + "CustomFields"
    )

// TODO: make configurable
private fun classNamePrefix(name: String): String =
    name
        .split('-')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }