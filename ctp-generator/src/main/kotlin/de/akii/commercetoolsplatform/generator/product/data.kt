package de.akii.commercetoolsplatform.generator.product

import com.commercetools.api.models.category.CategoryReference
import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.common.LocalizedStringImpl
import com.commercetools.api.models.product.*
import com.squareup.kotlinpoet.TypeSpec
import de.akii.commercetoolsplatform.generator.common.*
import javax.annotation.processing.Generated

fun generateProductData(
    productDataClassName: ProductDataClassName,
    productVariantClassName: ProductVariantClassName,
): TypeSpec = TypeSpec
    .classBuilder(productDataClassName.className)
    .addSuperinterface(ProductData::class)
    .addAnnotation(Generated::class)
    .addCTPProperty("name", LocalizedString::class, initializer = initializerFor(LocalizedStringImpl::class))
    .addCTPProperty("categories", MutableList::class, CategoryReference::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTPProperty("categoryOrderHints", CategoryOrderHints::class, initializer = initializerFor(CategoryOrderHintsImpl::class))
    .addCTPProperty("description", LocalizedString::class, true)
    .addCTPProperty("slug", LocalizedString::class, true)
    .addCTPProperty("metaTitle", LocalizedString::class, true)
    .addCTPProperty("metaDescription", LocalizedString::class, true)
    .addCTPProperty("metaKeywords", LocalizedString::class, true)
    .addCTPProperty("masterVariant", productVariantClassName.className, castedFrom = ProductVariant::class, initializer = initializerFor(productVariantClassName.className))
    .addCTPProperty("variants", MutableList::class, productVariantClassName.className, castedFrom = ProductVariant::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTPProperty("searchKeywords", SearchKeywords::class, initializer = initializerFor(SearchKeywordsImpl::class))
    .build()
