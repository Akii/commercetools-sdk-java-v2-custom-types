package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.common.*
import com.commercetools.api.models.product.*
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.common.Product
import de.akii.commercetools.api.customtypes.generator.common.ProductVariant
import io.vrap.rmf.base.client.utils.Generated

fun productVariant(
    productVariantClassName: ProductVariant,
    productVariantAttributesClassName: ProductVariantAttributes
): TypeSpec = TypeSpec
    .classBuilder(productVariantClassName.className)
    .addAnnotation(Generated::class)
    .addAnnotation(deserializeAs(productVariantClassName.className))
    .primaryConstructor(FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameter(ParameterSpec
            .builder("delegate", ProductVariantImpl::class)
            .addAnnotation(jsonProperty("delegate"))
            .build()
        )
        .addParameter(ParameterSpec
            .builder("typedAttributes", productVariantAttributesClassName.className)
            .addAnnotation(jsonProperty("typedAttributes"))
            .build()
        )
        .build()
    )
    .addSuperinterface(com.commercetools.api.models.product.ProductVariant::class, "delegate")
    .addProperty(
        PropertySpec
            .builder("typedAttributes", productVariantAttributesClassName.className)
            .initializer("typedAttributes")
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("getTypedAttributes")
            .returns(productVariantAttributesClassName.className)
            .addStatement("return this.typedAttributes")
            .build()
    )
    .build()