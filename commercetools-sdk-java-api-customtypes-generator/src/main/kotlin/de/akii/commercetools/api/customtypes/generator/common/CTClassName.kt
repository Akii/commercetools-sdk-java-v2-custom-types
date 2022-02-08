package de.akii.commercetools.api.customtypes.generator.common

import com.squareup.kotlinpoet.ClassName
import de.akii.commercetools.api.customtypes.generator.Configuration
import java.util.*

sealed class CTClassName(private val packageName: String, private val ctClassName: String) {
    val className: ClassName
        get() = ClassName(packageName, ctClassName)
}

class ProductClassName(productTypeName: String, config: Configuration) :
    CTClassName(typeNameToPackageName(productTypeName, config), "${typeNameToClassName(productTypeName)}Product")

class ProductCatalogDataClassName(productTypeName: String, config: Configuration) : CTClassName(
    typeNameToPackageName(productTypeName, config),
    "${typeNameToClassName(productTypeName)}ProductCatalogData"
)

class ProductDataClassName(productTypeName: String, config: Configuration) :
    CTClassName(typeNameToPackageName(productTypeName, config), "${typeNameToClassName(productTypeName)}ProductData")

class ProductVariantClassName(productTypeName: String, config: Configuration) : CTClassName(
    typeNameToPackageName(productTypeName, config),
    "${typeNameToClassName(productTypeName)}ProductVariant"
)

class ProductVariantAttributesClassName(productTypeName: String, config: Configuration) : CTClassName(
    typeNameToPackageName(productTypeName, config),
    "${typeNameToClassName(productTypeName)}ProductVariantAttributes"
)

fun typeNameToPackageName(typeName: String, config: Configuration) =
    "${config.packageName}.${typeNameToClassName(typeName).lowercase()}"

fun typeNameToClassName(typeName: String) =
    typeName
        .split('-', '_', ' ')
        .joinToString("") { part ->
            part.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
            }
        }