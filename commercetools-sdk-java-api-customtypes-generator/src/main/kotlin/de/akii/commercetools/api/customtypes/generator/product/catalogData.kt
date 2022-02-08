package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.product.ProductCatalogData
import com.commercetools.api.models.product.ProductData
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.ProductCatalogDataClassName
import de.akii.commercetools.api.customtypes.generator.common.ProductDataClassName
import de.akii.commercetools.api.customtypes.generator.common.addCTProperty
import de.akii.commercetools.api.customtypes.generator.common.initializerFor
import io.vrap.rmf.base.client.utils.Generated

fun generateProductCatalogData(
    productCatalogDataClassName: ProductCatalogDataClassName,
    productDataClassName: ProductDataClassName
): TypeSpec = TypeSpec
    .classBuilder(productCatalogDataClassName.className)
    .addSuperinterface(ProductCatalogData::class)
    .addAnnotation(Generated::class)
    .addCTProperty("current", productDataClassName.className, castedFrom = ProductData::class, initializer = initializerFor(productDataClassName.className))
    .addCTProperty("staged", productDataClassName.className, castedFrom = ProductData::class, initializer = initializerFor(productDataClassName.className))
    .addCTProperty("published", Boolean::class, initializer = "false")
    .addCTProperty("hasStagedChanges", Boolean::class, initializer = "false")
    .build()