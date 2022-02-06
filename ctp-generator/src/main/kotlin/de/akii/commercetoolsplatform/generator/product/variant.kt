package de.akii.commercetoolsplatform.generator.product

import com.commercetools.api.models.common.Asset
import com.commercetools.api.models.common.Image
import com.commercetools.api.models.common.Price
import com.commercetools.api.models.common.ScopedPrice
import com.commercetools.api.models.product.Attribute
import com.commercetools.api.models.product.ProductVariant
import com.commercetools.api.models.product.ProductVariantAvailability
import com.squareup.kotlinpoet.*
import de.akii.commercetoolsplatform.generator.common.MUTABLE_LIST_INITIALIZER
import de.akii.commercetoolsplatform.generator.common.ProductVariantAttributesClassName
import de.akii.commercetoolsplatform.generator.common.ProductVariantClassName
import de.akii.commercetoolsplatform.generator.common.addCTPProperty
import javax.annotation.processing.Generated

fun generateProductVariant(
    productVariantClassName: ProductVariantClassName,
    productVariantAttributesClassName: ProductVariantAttributesClassName
): TypeSpec = TypeSpec
    .classBuilder(productVariantClassName.className)
    .addSuperinterface(ProductVariant::class)
    .addAnnotation(Generated::class)
    .addCTPProperty("id", Long::class, initializer = "0L")
    .addCTPProperty("sku", String::class, true)
    .addCTPProperty("key", String::class, true)
    .addCTPProperty("prices", MutableList::class, Price::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTPProperty("attributes", MutableList::class, Attribute::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTPProperty("price", Price::class, true)
    .addCTPProperty("images", MutableList::class, Image::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTPProperty("assets", MutableList::class, Asset::class, initializer = MUTABLE_LIST_INITIALIZER)
    .addCTPProperty("availability", ProductVariantAvailability::class, true)
    .addCTPProperty("isMatchingVariant", Boolean::class, true)
    .addCTPProperty("scopedPrice", ScopedPrice::class, true)
    .addCTPProperty("scopedPriceDiscounted", Boolean::class, true)
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