package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.product.*
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.common.TypedProductVariant

fun productVariant(
    typedProductVariantClassName: TypedProductVariant,
    typedProductVariantAttributesClassName: TypedProductVariantAttributes
): TypeSpec = TypeSpec
    .classBuilder(typedProductVariantClassName.className)
    .addAnnotation(generated)
    .addAnnotation(deserializeAs(typedProductVariantClassName.className))
    .addModifiers(KModifier.DATA)
    .primaryConstructor(FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameter(ParameterSpec
            .builder("delegate", ProductVariantImpl::class)
            .addAnnotation(jsonProperty("delegate"))
            .build()
        )
        .addParameter(ParameterSpec
            .builder("typedAttributes", typedProductVariantAttributesClassName.className)
            .addAnnotation(jsonProperty("typedAttributes"))
            .build()
        )
        .build()
    )
    .addSuperinterface(com.commercetools.api.models.product.ProductVariant::class, "delegate")
    .addProperty(
        PropertySpec
            .builder("delegate", ProductVariantImpl::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("delegate")
            .build()
    )
    .addProperty(
        PropertySpec
            .builder("typedAttributes", typedProductVariantAttributesClassName.className)
            .initializer("typedAttributes")
            .build()
    )
    .build()