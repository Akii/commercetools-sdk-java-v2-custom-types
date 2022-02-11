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
    .addModifiers(KModifier.DATA)
    .addSuperinterface(ProductVariant::class)
    .addAnnotation(Generated::class)
    .addAnnotation(deserializeAs(productVariantClassName.className))
    .addCTProperties(
        SimpleCTProperty("id", Long::class),
        SimpleCTProperty("sku", String::class, nullable = true),
        SimpleCTProperty("key", String::class, nullable = true),
        ListCTProperty("prices", MutableList::class, Price::class),
        ListCTProperty("attributes", MutableList::class, Attribute::class),
        SimpleCTProperty("typedAttributes", productVariantAttributesClassName.className, modifiers = emptyList()),
        SimpleCTProperty("price", Price::class, nullable = true),
        ListCTProperty("images", MutableList::class, Image::class),
        ListCTProperty("assets", MutableList::class, Asset::class),
        SimpleCTProperty("availability", ProductVariantAvailability::class, nullable = true),
        SimpleCTProperty("isMatchingVariant", Boolean::class, nullable = true),
        SimpleCTProperty("scopedPrice", ScopedPrice::class, nullable = true),
        SimpleCTProperty("scopedPriceDiscounted", Boolean::class, nullable = true),
    )
    .build()