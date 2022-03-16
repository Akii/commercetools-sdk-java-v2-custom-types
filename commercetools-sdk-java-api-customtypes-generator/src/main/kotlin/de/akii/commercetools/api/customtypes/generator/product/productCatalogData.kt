package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.product.ProductCatalogDataImpl
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun productCatalogData(
    productCatalogDataClassName: ProductCatalogDataClassName,
    productDataClassName: ProductDataClassName
): TypeSpec = TypeSpec
    .classBuilder(productCatalogDataClassName.className)
    .superclass(ProductCatalogDataImpl::class)
    .addAnnotation(Generated::class)
    .addAnnotation(deserializeAs(productCatalogDataClassName.className))
    .addCTConstructorArguments(
        CTProperty("current", productDataClassName.className),
        CTProperty("staged", productDataClassName.className),
        CTParameter("published", Boolean::class),
        CTParameter("hasStagedChanges", Boolean::class)
    )
    .build()