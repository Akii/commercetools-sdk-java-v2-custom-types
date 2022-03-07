package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.product.ProductCatalogDataImpl
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
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
    .addFunction(FunSpec
        .builder("getCurrent")
        .addModifiers(KModifier.OVERRIDE)
        .addStatement("return this.%N", "current")
        .returns(productDataClassName.className)
        .build()
    )
    .addFunction(FunSpec
        .builder("getStaged")
        .addModifiers(KModifier.OVERRIDE)
        .addStatement("return this.%N", "staged")
        .returns(productDataClassName.className)
        .build()
    )
    .build()