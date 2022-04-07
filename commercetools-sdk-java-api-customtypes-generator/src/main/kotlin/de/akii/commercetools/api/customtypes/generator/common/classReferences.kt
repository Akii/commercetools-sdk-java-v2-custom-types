package de.akii.commercetools.api.customtypes.generator.common

import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.Type
import com.squareup.kotlinpoet.ClassName
import de.akii.commercetools.api.customtypes.generator.model.TypedResources

sealed class ClassReference(private val packageName: String, private val ctClassName: String) {
    val className: ClassName
        get() = ClassName(packageName, ctClassName)
}

class TypeResolver(config: Configuration) :
    ClassReference(config.packageName, "TypeResolver")

class TypedProductDeserializer(config: Configuration) :
    ClassReference("${config.packageName}.product", "TypedProductDeserializer")

class ProductTypeResolver(config: Configuration) :
    ClassReference("${config.packageName}.product", "ProductTypeResolver")

class TypedProductBeanDeserializerModifier(config: Configuration) :
    ClassReference("${config.packageName}.product", "TypedProductBeanDeserializerModifier")

class TypedProductDelegatingDeserializer(config: Configuration) :
    ClassReference("${config.packageName}.product", "TypedProductDelegatingDeserializer")

class TypedProductVariantAttributesDelegatingDeserializer(config: Configuration) :
    ClassReference("${config.packageName}.product", "TypedProductVariantAttributesDelegatingDeserializer")

class TypedProductInterface(config: Configuration) :
    ClassReference("${config.packageName}.product", "TypedProduct")

class TypedProductVariantAttributesInterface(config: Configuration) :
    ClassReference("${config.packageName}.product", "TypedProductVariantAttributes")

class TypedProduct(productType: ProductType, config: Configuration) : ClassReference(
    "${config.packageName}.product",
    config.productTypeToClassName(productType, ProductClassType.Product))

class TypedProductCatalogData(productType: ProductType, config: Configuration) : ClassReference(
    "${config.packageName}.product",
    config.productTypeToClassName(productType, ProductClassType.ProductCatalogData))

class TypedProductData(productType: ProductType, config: Configuration) : ClassReference(
    "${config.packageName}.product",
    config.productTypeToClassName(productType, ProductClassType.ProductData))

class TypedProductVariant(productType: ProductType, config: Configuration) : ClassReference(
    "${config.packageName}.product",
    config.productTypeToClassName(productType, ProductClassType.ProductVariant))

class TypedProductVariantAttributes(productType: ProductType, config: Configuration) : ClassReference(
    "${config.packageName}.product",
    config.productTypeToClassName(productType, ProductClassType.ProductVariantAttributes))

class TypedCustomFields(type: Type, config: Configuration) :
    ClassReference("${config.packageName}.custom_fields", config.typeToCustomFieldsClassName(type))

class TypedCustomFieldsBeanDeserializerModifier(config: Configuration) :
    ClassReference("${config.packageName}.custom_fields", "TypedCustomFieldsBeanDeserializerModifier")

class TypedCustomFieldsDelegatingDeserializer(config: Configuration) :
    ClassReference("${config.packageName}.custom_fields", "TypedCustomFieldsDelegatingDeserializer")

class TypedResourceInterface(config: Configuration) :
    ClassReference("${config.packageName}.custom_fields", "TypedResource")

class TypedResource(type: Type, resourceTypeName: String, config: Configuration) : ClassReference(
    "${config.packageName}.${resourceTypeNameToSubPackage(resourceTypeName)}",
    config.typeToResourceClassName(type, resourceTypeName))

class CustomFieldsTypeResolver(config: Configuration) :
    ClassReference("${config.packageName}.custom_fields", "CustomFieldsTypeResolver")

class TypedResourceDeserializer(typedResources: TypedResources) : ClassReference(
    typedResources.packageName,
    "Typed${typedResources.resourceInterface.simpleName}Deserializer")