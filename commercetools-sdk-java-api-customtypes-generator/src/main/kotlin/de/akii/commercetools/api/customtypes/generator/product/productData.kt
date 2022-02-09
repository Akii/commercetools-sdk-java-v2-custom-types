package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.category.CategoryReference
import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.LocalizedStringImpl
import com.commercetools.api.models.product.*
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun productData(
    productDataClassName: ProductDataClassName,
    productVariantClassName: ProductVariantClassName,
): TypeSpec = TypeSpec
    .classBuilder(productDataClassName.className)
    .addSuperinterface(ProductData::class)
    .addAnnotation(Generated::class)
    .addCTProperty("name", LocalizedString::class, initializer = initializerFor(LocalizedStringImpl::class))
    .addCTProperty("categories", MutableList::class, CategoryReference::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTProperty("categoryOrderHints", CategoryOrderHints::class, initializer = initializerFor(CategoryOrderHintsImpl::class))
    .addCTProperty("description", LocalizedString::class, true)
    .addCTProperty("slug", LocalizedString::class, true)
    .addCTProperty("metaTitle", LocalizedString::class, true)
    .addCTProperty("metaDescription", LocalizedString::class, true)
    .addCTProperty("metaKeywords", LocalizedString::class, true)
    .addCTProperty("masterVariant", productVariantClassName.className, castedFrom = ProductVariant::class, initializer = initializerFor(productVariantClassName.className))
    .addCTProperty("variants", MutableList::class, productVariantClassName.className, castedFrom = ProductVariant::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTProperty("searchKeywords", SearchKeywords::class, initializer = initializerFor(SearchKeywordsImpl::class))
    .build()
