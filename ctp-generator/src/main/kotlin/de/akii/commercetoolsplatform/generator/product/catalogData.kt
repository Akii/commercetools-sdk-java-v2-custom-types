package de.akii.commercetoolsplatform.generator.product

import com.commercetools.api.models.product.ProductCatalogData
import com.commercetools.api.models.product.ProductData
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetoolsplatform.generator.common.ProductCatalogDataClassName
import de.akii.commercetoolsplatform.generator.common.ProductDataClassName
import de.akii.commercetoolsplatform.generator.common.addCTPProperty
import de.akii.commercetoolsplatform.generator.common.initializerFor
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