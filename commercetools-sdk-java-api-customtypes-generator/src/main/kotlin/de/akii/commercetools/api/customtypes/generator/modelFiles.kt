package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.product_type.ProductType
import com.squareup.kotlinpoet.FileSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.model.*
import de.akii.commercetools.api.customtypes.generator.model.product.productCatalogData
import de.akii.commercetools.api.customtypes.generator.model.product.productData
import de.akii.commercetools.api.customtypes.generator.model.product.productVariant
import de.akii.commercetools.api.customtypes.generator.model.product.productVariantAttributes

fun modelFiles(typedResources: List<TypedResources>, config: Configuration): List<FileSpec> =
    listOf(
        productCommonFile(config),
        customFieldsFile(config),
        typedResourcesCommonFile(config)
    ) + productFiles(config) + typedResourceFiles(typedResources)

fun productFiles(config: Configuration): List<FileSpec> =
    config.productTypes.map {
        productFile(it, config)
    }

fun productFile(
    productType: ProductType,
    config: Configuration
): FileSpec {
    val typedProductClassName = TypedProduct(productType, config)
    val typedProductCatalogDataClassName = TypedProductCatalogData(productType, config)
    val typedProductDataClassName = TypedProductData(productType, config)
    val typedProductVariantClassName = TypedProductVariant(productType, config)
    val typedProductVariantAttributesClassName = TypedProductVariantAttributes(productType, config)
    val typedProductVariantAttributesInterfaceClassName = TypedProductVariantAttributesInterface(config)

    val attributeTypeSpec = productVariantAttributes(
        typedProductVariantAttributesClassName,
        typedProductVariantAttributesInterfaceClassName,
        productType,
        config
    )

    val variantTypeSpec = productVariant(
        typedProductVariantClassName,
        typedProductVariantAttributesClassName
    )

    val productDataTypeSpec = productData(
        typedProductDataClassName,
        typedProductVariantClassName
    )

    val masterDataTypeSpec = productCatalogData(
        typedProductCatalogDataClassName,
        typedProductDataClassName
    )

    val product = typedProduct(productType, config)

    return FileSpec
        .builder(typedProductClassName.className.packageName, typedProductClassName.className.simpleName)
        .addType(product)
        .addType(masterDataTypeSpec)
        .addType(productDataTypeSpec)
        .addType(variantTypeSpec)
        .addType(attributeTypeSpec)
        .build()
}

fun productCommonFile(config: Configuration) =
    FileSpec
        .builder("${config.packageName}.product", "common")
        .addType(typedProductInterface(config))
        .addType(typedProductVariantAttributesInterface(config))
        .build()

fun customFieldsFile(config: Configuration): FileSpec {
    val customFieldsFile = FileSpec
        .builder("${config.packageName}.typed_resources", "typedCustomFields")

    config.customTypes.forEach {
        customFieldsFile.addType(typedCustomField(it, config))
    }

    return customFieldsFile.build()
}

fun typedResourceFiles(typedResource: List<TypedResources>): List<FileSpec> =
    typedResource.flatMap { typedResources ->
        typedResources.resources.map {
            FileSpec
                .builder(typedResources.packageName, it.typedResourceClassName.simpleName)
                .addType(it.typedResourceSpec)
                .build()
        }
    }

fun typedResourcesCommonFile(config: Configuration) =
    FileSpec
        .builder("${config.packageName}.typed_resources", "common")
        .addType(typedResourceInterface(config))
        .build()