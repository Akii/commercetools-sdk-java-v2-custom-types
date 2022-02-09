package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.common.Asset
import com.commercetools.api.models.common.Image
import com.commercetools.api.models.common.Price
import com.commercetools.api.models.common.ScopedPrice
import com.commercetools.api.models.product.Attribute
import com.commercetools.api.models.product.ProductVariant
import com.commercetools.api.models.product.ProductVariantAvailability
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.MUTABLE_LIST_INITIALIZER
import de.akii.commercetools.api.customtypes.generator.common.ProductVariantAttributesClassName
import de.akii.commercetools.api.customtypes.generator.common.ProductVariantClassName
import de.akii.commercetools.api.customtypes.generator.common.addCTProperty
import io.vrap.rmf.base.client.utils.Generated

fun productVariant(
    productVariantClassName: ProductVariantClassName,
    productVariantAttributesClassName: ProductVariantAttributesClassName
): TypeSpec = TypeSpec
    .classBuilder(productVariantClassName.className)
    .addSuperinterface(ProductVariant::class)
    .addAnnotation(Generated::class)
    .addCTProperty("id", Long::class, initializer = "0L")
    .addCTProperty("sku", String::class, true)
    .addCTProperty("key", String::class, true)
    .addCTProperty("prices", MutableList::class, Price::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTProperty("attributes", MutableList::class, Attribute::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTProperty("price", Price::class, true)
    .addCTProperty("images", MutableList::class, Image::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTProperty("assets", MutableList::class, Asset::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTProperty("availability", ProductVariantAvailability::class, true)
    .addCTProperty("isMatchingVariant", Boolean::class, true)
    .addCTProperty("scopedPrice", ScopedPrice::class, true)
    .addCTProperty("scopedPriceDiscounted", Boolean::class, true)
    .addProperty(typedAttributesProperty(productVariantAttributesClassName))
    .addFunction(typedAttributesGetter(productVariantAttributesClassName))
    .build()

private fun typedAttributesProperty(productVariantAttributesClassName: ProductVariantAttributesClassName) =
    PropertySpec
        .builder("typedAttributes", productVariantAttributesClassName.className.copy(nullable = true))
        .initializer("null")
        .addModifiers(KModifier.PRIVATE)
        .build()

private fun typedAttributesGetter(productVariantAttributesClassName: ProductVariantAttributesClassName) =
    FunSpec
        .builder("getTypedAttributes")
        .returns(productVariantAttributesClassName.className)
        .addStatement("return this.typedAttributes!!")
        .build()