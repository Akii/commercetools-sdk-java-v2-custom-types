package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.product.ProductDataImpl
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated

fun productData(
    productDataClassName: ProductData,
    productVariantClassName: ProductVariant,
): TypeSpec = TypeSpec
    .classBuilder(productDataClassName.className)
    .addAnnotation(Generated::class)
    .addAnnotation(deserializeAs(productDataClassName.className))
    .primaryConstructor(FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameter(ParameterSpec
            .builder("delegate", ProductDataImpl::class)
            .addAnnotation(jsonProperty("delegate"))
            .build()
        )
        .addParameter(ParameterSpec
            .builder("masterVariant", productVariantClassName.className)
            .addAnnotation(jsonProperty("masterVariant"))
            .build()
        )
        .addParameter(ParameterSpec
            .builder("variants", LIST.parameterizedBy(productVariantClassName.className))
            .addAnnotation(jsonProperty("variants"))
            .build()
        )
        .build()
    )
    .addSuperinterface(com.commercetools.api.models.product.ProductData::class, "delegate")
    .addProperty(
        PropertySpec
            .builder("masterVariant", productVariantClassName.className)
            .initializer("masterVariant")
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    .addProperty(
        PropertySpec
            .builder("variants", LIST.parameterizedBy(productVariantClassName.className))
            .initializer("variants")
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("getMasterVariant")
            .returns(productVariantClassName.className)
            .addStatement("return this.masterVariant")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("getVariants")
            .returns(LIST.parameterizedBy(productVariantClassName.className))
            .addStatement("return this.variants")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .build()
