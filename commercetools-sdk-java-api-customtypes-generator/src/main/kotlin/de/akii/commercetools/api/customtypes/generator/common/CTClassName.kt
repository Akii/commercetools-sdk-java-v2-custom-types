package de.akii.commercetools.api.customtypes.generator.common

import com.commercetools.api.models.product_type.ProductType
import com.squareup.kotlinpoet.ClassName
import de.akii.commercetools.api.customtypes.generator.TypedResourceFile

sealed class CTClassName(private val packageName: String, private val ctClassName: String) {
    val className: ClassName
        get() = ClassName(packageName, ctClassName)
}

class CustomProductDeserializerClassName(config: Configuration) :
    CTClassName("${config.packageName}.product", "CustomProductDeserializer")

class CustomProductVariantAttributesModifierClassName(config: Configuration) :
    CTClassName("${config.packageName}.product", "CustomProductVariantAttributesModifier")

class CustomProductVariantAttributesDelegatingDeserializerClassName(config: Configuration) :
    CTClassName("${config.packageName}.product", "CustomProductVariantAttributesDelegatingDeserializer")

class CustomProductVariantAttributesClassName(config: Configuration) :
    CTClassName("${config.packageName}.product", "CustomProductVariantAttributes")

class ProductClassName(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeNameToSubPackageName(productType.name)}",
    "${config.productTypeNameToClassNamePrefix(productType.name)}Product"
)

class ProductCatalogDataClassName(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeNameToSubPackageName(productType.name)}",
    "${config.productTypeNameToClassNamePrefix(productType.name)}ProductCatalogData"
)

class ProductDataClassName(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeNameToSubPackageName(productType.name)}",
    "${config.productTypeNameToClassNamePrefix(productType.name)}ProductData"
)

class ProductVariantClassName(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeNameToSubPackageName(productType.name)}",
    "${config.productTypeNameToClassNamePrefix(productType.name)}ProductVariant"
)

class ProductVariantAttributesClassName(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeNameToSubPackageName(productType.name)}",
    "${config.productTypeNameToClassNamePrefix(productType.name)}ProductVariantAttributes"
)

class TypedCustomFieldsDeserializerClassName(config: Configuration) :
    CTClassName("${config.packageName}.custom_fields", "TypedCustomFieldsDeserializer")

class TypedResourceDeserializerClassName(typedResource: TypedResourceFile, config: Configuration) :
    CTClassName("${config.packageName}.custom_fields", "${typedResource.resourceInterface.simpleName}Deserializer")