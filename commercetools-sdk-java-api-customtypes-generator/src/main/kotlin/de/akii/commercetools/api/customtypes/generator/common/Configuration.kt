package de.akii.commercetools.api.customtypes.generator.common

import com.commercetools.api.models.product_type.AttributeDefinition
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.FieldDefinition
import com.commercetools.api.models.type.Type

enum class ProductClassType {
    Product, ProductCatalogData, ProductData, ProductVariant, ProductVariantAttributes
}

data class Configuration(
    val packageName: String,
    val productTypes: List<ProductType>,
    val customTypes: List<Type>,
    val productTypeToClassName: (productType: ProductType, productClassType: ProductClassType) -> String = ::productTypeToClassName,
    val productTypeAttributeToPropertyName: (productType: ProductType, attribute: AttributeDefinition) -> String = ::productTypeAttributeToPropertyName,
    val fieldDefinitionToPropertyName: (type: Type, fieldDefinition: FieldDefinition) -> String = ::fieldDefinitionToPropertyName
)

fun productTypeToSubPackageName(productType: ProductType) =
    productType
        .name
        .split('-', '_')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
        .lowercase()

fun productTypeToClassName(productType: ProductType, productClassType: ProductClassType): String =
    productType
        .name
        .split('-', '_')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        } + productClassType.name

fun productTypeAttributeToPropertyName(@Suppress("UNUSED_PARAMETER") productType: ProductType, attribute: AttributeDefinition): String =
    attribute
        .name
        .split('-', '_')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
        .replaceFirstChar { it.lowercase() }

fun fieldDefinitionToPropertyName(@Suppress("UNUSED_PARAMETER") type: Type, fieldDefinition: FieldDefinition): String =
    fieldDefinition
        .name
        .split('-', '_')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
        .replaceFirstChar { it.lowercase() }