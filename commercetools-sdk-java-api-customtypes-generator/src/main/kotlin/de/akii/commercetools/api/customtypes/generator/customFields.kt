package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.TypedMoney
import com.commercetools.api.models.type.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

// TODO: see https://github.com/commercetools/commercetools-sdk-java-v2/issues/273
val resourceTypeIds = listOf(
    ResourceTypeId.ADDRESS,
    ResourceTypeId.ASSET,
    // ResourceTypeId.CART,
    ResourceTypeId.CART_DISCOUNT,
    ResourceTypeId.CATEGORY,
    ResourceTypeId.CHANNEL,
    ResourceTypeId.CUSTOMER,
    ResourceTypeId.CUSTOMER_GROUP,
    ResourceTypeId.CUSTOM_LINE_ITEM,
    // ResourceTypeId.DELIVERY,
    ResourceTypeId.DISCOUNT_CODE,
    ResourceTypeId.PAYMENT_INTERFACE_INTERACTION,
    ResourceTypeId.INVENTORY_ENTRY,
    ResourceTypeId.LINE_ITEM,
    ResourceTypeId.ORDER,
    ResourceTypeId.ORDER_EDIT,
    ResourceTypeId.PAYMENT,
    ResourceTypeId.PRODUCT_PRICE,
    // ResourceTypeId.PRODUCT_SELECTION,
    // ResourceTypeId.RETURN_ITEM,
    ResourceTypeId.REVIEW,
    ResourceTypeId.SHIPPING_METHOD,
    ResourceTypeId.SHOPPING_LIST,
    ResourceTypeId.STORE,
    ResourceTypeId.SHOPPING_LIST_TEXT_LINE_ITEM
    // ResourceTypeId.TRANSACTION
)

fun customFieldsFile(types: List<Type>, config: Configuration): FileSpec {
    val customFieldsFile = FileSpec
        .builder("${config.packageName}.custom_fields", "typedCustomFields")

    sealedOrderCustomFieldInterfaces(config).forEach {
        customFieldsFile.addType(it)
    }

    types.forEach {
        customFieldsFile.addType(typedCustomField(it, config))
    }

    return customFieldsFile.build()
}

private fun sealedOrderCustomFieldInterfaces(config: Configuration): List<TypeSpec> =
    resourceTypeIds.map {
        TypeSpec
            .interfaceBuilder(resourceTypeIdToClassName(it, config))
            .addAnnotation(Generated::class)
            .addModifiers(KModifier.SEALED)
            .addSuperinterface(CustomFields::class)
            .build()
    }

private fun typedCustomField(type: Type, config: Configuration): TypeSpec {
    val fields = typedFields(type.fieldDefinitions, config)

    return TypeSpec
        .classBuilder(typeToClassName(type, config))
        .addAnnotation(Generated::class)
        .superclass(CustomFieldsImpl::class)
        .addSuperinterfaces(type.resourceTypeIds.map { resourceTypeIdToClassName(it, config) })
        .addCTConstructorArguments(
            CTParameter("type", TypeReference::class),
            CTParameter("fields", FieldContainer::class),
            CTProperty("typedFields", ClassName("", "Fields"), modifiers = emptyList())
        )
        .addType(fields)
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
        .addAnnotation(jsonProperty(config.attributeNameToPropertyName(fieldDefinition.name)))
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
        is CustomFieldReferenceType -> referenceTypeIdToClassName(fieldType.referenceTypeId)
        is CustomFieldSetType -> SET.parameterizedBy(typeNameForFieldType(fieldType.elementType, config))
        else -> Any::class.asTypeName()
    }.copy(nullable = true)

private fun typeToClassName(type: Type, config: Configuration): ClassName =
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