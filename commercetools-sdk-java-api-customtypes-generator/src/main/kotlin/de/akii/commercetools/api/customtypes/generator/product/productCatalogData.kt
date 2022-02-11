package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.product.ProductCatalogData
import com.commercetools.api.models.product.ProductData
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun productCatalogData(
    productCatalogDataClassName: ProductCatalogDataClassName,
    productDataClassName: ProductDataClassName
): TypeSpec = TypeSpec
    .classBuilder(productCatalogDataClassName.className)
    .addSuperinterface(ProductCatalogData::class)
    .addAnnotation(Generated::class)
    .addAnnotation(deserializeAs(productCatalogDataClassName.className))
    .addCTProperties(
        SimpleCTProperty("current", productDataClassName.className, castedFrom = ProductData::class),
        SimpleCTProperty("staged", productDataClassName.className, castedFrom = ProductData::class),
        SimpleCTProperty("published", Boolean::class),
        SimpleCTProperty("hasStagedChanges", Boolean::class)
    )
    .build()