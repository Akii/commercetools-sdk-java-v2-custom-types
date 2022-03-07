package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.common.*
import com.commercetools.api.models.product.*
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun productVariant(
    productVariantClassName: ProductVariantClassName,
    productVariantAttributesClassName: ProductVariantAttributesClassName
): TypeSpec = TypeSpec
    .classBuilder(productVariantClassName.className)
    .superclass(ProductVariantImpl::class)
    .addAnnotation(Generated::class)
    .addAnnotation(deserializeAs(productVariantClassName.className))
    .addCTConstructorArguments(
        CTParameter("id", Long::class),
        CTParameter("sku", String::class, nullable = true),
        CTParameter("key", String::class, nullable = true),
        CTParameter("prices", MutableList::class, Price::class),
        CTParameter("attributes", MutableList::class, Attribute::class),
        CTProperty("typedAttributes", productVariantAttributesClassName.className, modifiers = emptyList()),
        CTParameter("price", Price::class, nullable = true),
        CTParameter("images", MutableList::class, Image::class),
        CTParameter("assets", MutableList::class, Asset::class),
        CTParameter("availability", ProductVariantAvailability::class, nullable = true),
        CTParameter("isMatchingVariant", Boolean::class, nullable = true),
        CTParameter("scopedPrice", ScopedPrice::class, nullable = true),
        CTParameter("scopedPriceDiscounted", Boolean::class, nullable = true),
    )
    .build()