package de.akii.commercetools.api.customtypes.generator.common

import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.ResourceTypeId
import com.squareup.kotlinpoet.ClassName

sealed class CTClassName(private val packageName: String, private val ctClassName: String) {
    val className: ClassName
        get() = ClassName(packageName, ctClassName)
}

class TypeResolver(config: Configuration) :
    CTClassName(config.packageName, "TypeResolver")

class CustomProductDeserializer(config: Configuration) :
    CTClassName("${config.packageName}.product", "CustomProductDeserializer")

class ProductTypeResolver(config: Configuration) :
    CTClassName("${config.packageName}.product", "ProductTypeResolver")

class CustomProductVariantAttributesModifier(config: Configuration) :
    CTClassName("${config.packageName}.product", "CustomProductVariantAttributesModifier")

class CustomProductVariantAttributesDelegatingDeserializer(config: Configuration) :
    CTClassName("${config.packageName}.product", "CustomProductVariantAttributesDelegatingDeserializer")

class CustomProductVariantAttributes(config: Configuration) :
    CTClassName("${config.packageName}.product", "CustomProductVariantAttributes")

class Product(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeToSubPackageName(productType)}",
    config.productTypeToClassName(productType, ProductClassType.Product)
)

class ProductCatalogData(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeToSubPackageName(productType)}",
    config.productTypeToClassName(productType, ProductClassType.ProductCatalogData)
)

class ProductData(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeToSubPackageName(productType)}",
    config.productTypeToClassName(productType, ProductClassType.ProductData)
)

class ProductVariant(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeToSubPackageName(productType)}",
    config.productTypeToClassName(productType, ProductClassType.ProductVariant)
)

class ProductVariantAttributes(productType: ProductType, config: Configuration) : CTClassName(
    "${config.packageName}.product.${config.productTypeToSubPackageName(productType)}",
    config.productTypeToClassName(productType, ProductClassType.ProductVariantAttributes)
)

class FallbackCustomFields(config: Configuration) :
    CTClassName("${config.packageName}.custom_fields", "FallbackCustomFields")

class TypedCustomFieldsDeserializer(resourceTypeId: ResourceTypeId, config: Configuration) :
    CTClassName("${config.packageName}.custom_fields", "${resourceTypeIdToClassName(resourceTypeId, config).simpleName}Deserializer")

class CustomTypeResolver(config: Configuration) :
    CTClassName("${config.packageName}.custom_fields", "CustomTypeResolver")

class TypedResourceDeserializer(config: Configuration) :
    CTClassName("${config.packageName}.custom_fields", "TypedResourceDeserializer")