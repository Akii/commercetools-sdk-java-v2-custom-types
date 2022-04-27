package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.product_type.ProductType
import com.squareup.kotlinpoet.FileSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.model.*
import de.akii.commercetools.api.customtypes.generator.model.product.*

fun modelFiles(typedResources: List<TypedResources>, config: Configuration): List<FileSpec> =
    listOf(
        productCommonFile(config),
        typedResourcesCommonFile(config),
        typedCustomObjectsCommonFile(config)
    ) + customFieldsFiles(config) + productFiles(config) + typedResourceFiles(typedResources, config) + typedCustomObjectFiles(config)

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
    val typedProductProjectionClassName = TypedProductProjection(productType, config)

    val (buildVariant, buildVariantUnchecked) = typedProductVariantBuilderExtensionFunctions(
        typedProductVariantClassName,
        typedProductVariantAttributesClassName
    )

    val (buildData, buildDataUnchecked) = typedProductDataBuilderExtensionFunctions(
        typedProductDataClassName,
        typedProductVariantClassName
    )

    val (buildCatalogData, buildCatalogDataUnchecked) = typedProductCatalogDataBuilderExtensionFunctions(
        typedProductCatalogDataClassName,
        typedProductDataClassName
    )

    val (buildProduct, buildProductUnchecked) = typedProductBuilderExtensionFunctions(
        typedProductClassName,
        typedProductCatalogDataClassName
    )

    val (buildProductProjection, buildProductProjectionUnchecked) = typedProductProjectionBuilderExtensionFunctions(
        typedProductProjectionClassName,
        typedProductVariantClassName
    )

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

    val productProjection = productProjection(
        typedProductProjectionClassName,
        typedProductVariantClassName
    )

    return FileSpec
        .builder(typedProductClassName.className.packageName, typedProductClassName.className.simpleName)
        .addFunction(buildProduct)
        .addFunction(buildProductUnchecked)
        .addFunction(buildCatalogData)
        .addFunction(buildCatalogDataUnchecked)
        .addFunction(buildData)
        .addFunction(buildDataUnchecked)
        .addFunction(buildVariant)
        .addFunction(buildVariantUnchecked)
        .addFunction(buildProductProjection)
        .addFunction(buildProductProjectionUnchecked)
        .addType(product)
        .addType(masterDataTypeSpec)
        .addType(productDataTypeSpec)
        .addType(variantTypeSpec)
        .addType(attributeTypeSpec)
        .addType(productProjection)
        .build()
}

fun productCommonFile(config: Configuration) =
    FileSpec
        .builder("${config.packageName}.product", "common")
        .addType(typedProductInterface(config))
        .addType(typedProductVariantAttributesInterface(config))
        .build()

fun customFieldsFiles(config: Configuration): List<FileSpec> =
    config.customTypes.map {
        val (build, buildUnchecked) = typedCustomFieldsBuilderExtensionFunctions(it, config)

        FileSpec
            .builder("${config.packageName}.custom_fields", TypedCustomFields(it, config).className.simpleName)
            .addFunction(build)
            .addFunction(buildUnchecked)
            .addType(typedCustomFields(it, config))
            .build()
    }

fun typedResourceFiles(typedResource: List<TypedResources>, config: Configuration): List<FileSpec> =
    typedResource.flatMap { typedResources ->
        typedResources.resources.map {
            val (build, buildUnchecked) = typedResourceBuilderExtensionFunctions(typedResources, it, config)
            FileSpec
                .builder(typedResources.packageName, it.typedResourceClassName.simpleName)
                .addFunction(build)
                .addFunction(buildUnchecked)
                .addType(it.typedResourceSpec)
                .build()
        }
    }

fun typedResourcesCommonFile(config: Configuration) =
    FileSpec
        .builder("${config.packageName}.custom_fields", "common")
        .addType(typedResourceInterface(config))
        .build()

fun typedCustomObjectFiles(config: Configuration): List<FileSpec> =
    config.customObjectTypes
        .map { (containerName, valueClassName) ->
            val (build, buildUnchecked) = typedCustomObjectBuilderExtensionFunctions(containerName, valueClassName, config)
            val (customObjectClassName, customObject) = typedCustomObject(containerName, valueClassName, config)

            FileSpec
                .builder("${config.packageName}.custom_objects", customObjectClassName.simpleName)
                .addFunction(build)
                .addFunction(buildUnchecked)
                .addType(customObject)
                .build()
        }

fun typedCustomObjectsCommonFile(config: Configuration) =
    FileSpec
        .builder("${config.packageName}.custom_objects", "common")
        .addType(typedCustomObjectInterface(config))
        .build()