package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.category.CategoryReference
import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.product.CategoryOrderHints
import com.commercetools.api.models.product.ProductData
import com.commercetools.api.models.product.ProductVariant
import com.commercetools.api.models.product.SearchKeywords
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun productData(
    productDataClassName: ProductDataClassName,
    productVariantClassName: ProductVariantClassName,
): TypeSpec = TypeSpec
    .classBuilder(productDataClassName.className)
    .addModifiers(KModifier.DATA)
    .addSuperinterface(ProductData::class)
    .addAnnotation(Generated::class)
    .addAnnotation(deserializeAs(productDataClassName.className))
    .addCTProperties(
        SimpleCTProperty("name", LocalizedString::class),
        ListCTProperty("categories", MutableList::class, CategoryReference::class, nullable = true),
        SimpleCTProperty("categoryOrderHints", CategoryOrderHints::class),
        SimpleCTProperty("description", LocalizedString::class, nullable = true),
        SimpleCTProperty("slug", LocalizedString::class, nullable = true),
        SimpleCTProperty("metaTitle", LocalizedString::class, nullable = true),
        SimpleCTProperty("metaDescription", LocalizedString::class, nullable = true),
        SimpleCTProperty("metaKeywords", LocalizedString::class, nullable = true),
        SimpleCTProperty("masterVariant", productVariantClassName.className, castedFrom = ProductVariant::class),
        ListCTProperty("variants", MutableList::class, productVariantClassName.className, castedFrom = ProductVariant::class),
        SimpleCTProperty("searchKeywords", SearchKeywords::class)
    )
    .build()
