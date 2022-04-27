package de.akii.commercetools.api.customtypes.generator.common

import com.commercetools.api.models.product_type.AttributeDefinition
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.FieldDefinition
import com.commercetools.api.models.type.Type

enum class ProductClassType {
    Product,
    ProductCatalogData,
    ProductData,
    ProductVariant,
    ProductVariantAttributes,
    ProductProjection
}

data class Configuration(
    val packageName: String,
    val productTypes: List<ProductType>,
    val customTypes: List<Type>,
    val customObjectTypes: Map<String, String>,

    val productTypeToKey: (productType: ProductType) -> String = ::productTypeToKey,
    val productTypeToClassName: (productType: ProductType, productClassType: ProductClassType) -> String = ::productTypeToClassName,
    val attributeToPropertyName: (productType: ProductType, attribute: AttributeDefinition) -> String = ::attributeToPropertyName,
    val isAttributeRequired: (productType: ProductType, attribute: AttributeDefinition) -> Boolean = ::isAttributeRequired,

    val typeToKey: (type: Type) -> String = ::typeToKey,
    val typeToCustomFieldsClassName: (type: Type) -> String = ::typeToCustomFieldsClassName,
    val typeToResourceClassName: (type: Type, referenceTypeName: String) -> String = ::typeToResourceClassName,
    val fieldToPropertyName: (type: Type, fieldDefinition: FieldDefinition) -> String = ::fieldToPropertyName,
    val isFieldRequired: (type: Type, fieldDefinition: FieldDefinition) -> Boolean = ::isFieldRequired,

    val containerNameToClassName: (containerName: String, className: String) -> String = ::containerNameToClassName
)

fun productTypeToKey(productType: ProductType): String =
    productType.key!!

fun productTypeToClassName(productType: ProductType, productClassType: ProductClassType): String =
    productType
        .key!!
        .split('-', '_')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        } + productClassType.name

fun attributeToPropertyName(@Suppress("UNUSED_PARAMETER") productType: ProductType, attribute: AttributeDefinition): String =
    attribute
        .name
        .split('-', '_')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
        .replaceFirstChar { it.lowercase() }

fun isAttributeRequired(
    @Suppress("UNUSED_PARAMETER") productType: ProductType,
    @Suppress("UNUSED_PARAMETER") attribute: AttributeDefinition): Boolean = false

fun typeToKey(type: Type): String =
    type.key

fun typeToCustomFieldsClassName(type: Type): String =
    "${classNamePrefix(type.key)}CustomFields"

fun typeToResourceClassName(type: Type, referenceTypeName: String): String =
    "${classNamePrefix(type.key)}${classNamePrefix(referenceTypeName)}"

fun fieldToPropertyName(@Suppress("UNUSED_PARAMETER") type: Type, fieldDefinition: FieldDefinition): String =
    fieldDefinition
        .name
        .split('-', '_')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
        .replaceFirstChar { it.lowercase() }

fun isFieldRequired(
    @Suppress("UNUSED_PARAMETER") type: Type,
    @Suppress("UNUSED_PARAMETER") fieldDefinition: FieldDefinition): Boolean = false

fun containerNameToClassName(
    @Suppress("UNUSED_PARAMETER") containerName: String,
    className: String): String = "${className.substringAfterLast('.')}CustomObject"