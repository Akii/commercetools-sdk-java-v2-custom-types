package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.product.ProductCatalogData
import com.commercetools.api.models.product.ProductData
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.ProductCatalogDataClassName
import de.akii.commercetools.api.customtypes.generator.common.ProductDataClassName
import de.akii.commercetools.api.customtypes.generator.common.addCTPProperty
import de.akii.commercetools.api.customtypes.generator.common.initializerFor
import javax.annotation.processing.Generated

fun generateProductCatalogData(
    productCatalogDataClassName: ProductCatalogDataClassName,
    productDataClassName: ProductDataClassName
): TypeSpec = TypeSpec
    .classBuilder(productCatalogDataClassName.className)
    .addSuperinterface(ProductCatalogData::class)
    .addAnnotation(Generated::class)
    .addCTPProperty("current", productDataClassName.className, castedFrom = ProductData::class, initializer = initializerFor(productDataClassName.className))
    .addCTPProperty("staged", productDataClassName.className, castedFrom = ProductData::class, initializer = initializerFor(productDataClassName.className))
    .addCTPProperty("published", Boolean::class, initializer = "false")
    .addCTPProperty("hasStagedChanges", Boolean::class, initializer = "false")
    .build()