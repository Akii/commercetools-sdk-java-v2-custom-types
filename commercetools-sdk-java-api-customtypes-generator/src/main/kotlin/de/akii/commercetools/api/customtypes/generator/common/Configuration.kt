package de.akii.commercetools.api.customtypes.generator.common

import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.Type

data class Configuration(
    val packageName: String,
    val productTypes: List<ProductType>,
    val customTypes: List<Type>,
    val productTypeNameToSubPackageName: (productTypeName: String) -> String = ::productTypeNameToSubPackageName,
    val productTypeNameToClassNamePrefix: (productTypeName: String) -> String = ::productTypeNameToClassNamePrefix,
    val attributeNameToPropertyName: (attributeName: String) -> String = ::attributeNameToPropertyName
)

fun productTypeNameToSubPackageName(productTypeName: String) =
    productTypeNameToClassNamePrefix(productTypeName).lowercase()

fun productTypeNameToClassNamePrefix(productTypeName: String): String =
    productTypeName
        .split('-', '_', ' ')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }

fun attributeNameToPropertyName(attributeName: String): String =
    attributeName
        .split('-', '_', ' ')
        .joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
        .replaceFirstChar { it.lowercase() }