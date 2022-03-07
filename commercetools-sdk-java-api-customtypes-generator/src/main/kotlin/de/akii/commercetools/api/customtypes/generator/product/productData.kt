package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.category.CategoryReference
import com.commercetools.api.models.common.LocalizedString
import com.commercetools.api.models.product.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun productData(
    productDataClassName: ProductDataClassName,
    productVariantClassName: ProductVariantClassName,
): TypeSpec = TypeSpec
    .classBuilder(productDataClassName.className)
    .superclass(ProductDataImpl::class)
    .addAnnotation(Generated::class)
    .addAnnotation(deserializeAs(productDataClassName.className))
    .addCTConstructorArguments(
        CTParameter("name", LocalizedString::class),
        CTParameter("categories", MutableList::class, CategoryReference::class, nullable = true),
        CTParameter("categoryOrderHints", CategoryOrderHints::class),
        CTParameter("description", LocalizedString::class, nullable = true),
        CTParameter("slug", LocalizedString::class, nullable = true),
        CTParameter("metaTitle", LocalizedString::class, nullable = true),
        CTParameter("metaDescription", LocalizedString::class, nullable = true),
        CTParameter("metaKeywords", LocalizedString::class, nullable = true),
        CTProperty("masterVariant", productVariantClassName.className),
        CTProperty("variants", MutableList::class.asTypeName(), productVariantClassName.className),
        CTParameter("searchKeywords", SearchKeywords::class)
    )
    .addFunction(
        FunSpec
        .builder("getMasterVariant")
        .addModifiers(KModifier.OVERRIDE)
        .addStatement("return this.%N", "masterVariant")
        .returns(productVariantClassName.className)
        .build()
    )
    .addFunction(
        FunSpec
            .builder("getVariants")
            .addModifiers(KModifier.OVERRIDE)
            .addStatement("return this.%N", "variants")
            .returns(LIST.parameterizedBy(productVariantClassName.className))
            .build()
    )
    .build()
