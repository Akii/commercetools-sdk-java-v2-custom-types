package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.product.ProductDataImpl
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*

fun productData(
    typedProductDataClassName: TypedProductData,
    typedProductVariantClassName: TypedProductVariant,
): TypeSpec = TypeSpec
    .classBuilder(typedProductDataClassName.className)
    .addAnnotation(generated)
    .addAnnotation(deserializeAs(typedProductDataClassName.className))
    .addModifiers(KModifier.DATA)
    .primaryConstructor(FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameter(ParameterSpec
            .builder("delegate", ProductDataImpl::class)
            .addAnnotation(jsonProperty("delegate"))
            .build()
        )
        .addParameter(ParameterSpec
            .builder("masterVariant", typedProductVariantClassName.className)
            .addAnnotation(jsonProperty("masterVariant"))
            .build()
        )
        .addParameter(ParameterSpec
            .builder("variants", LIST.parameterizedBy(typedProductVariantClassName.className))
            .addAnnotation(jsonProperty("variants"))
            .build()
        )
        .build()
    )
    .addSuperinterface(com.commercetools.api.models.product.ProductData::class, "delegate")
    .addProperty(
        PropertySpec
            .builder("delegate", ProductDataImpl::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("delegate")
            .build()
    )
    .addProperty(
        PropertySpec
            .builder("masterVariant", typedProductVariantClassName.className)
            .initializer("masterVariant")
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    .addProperty(
        PropertySpec
            .builder("variants", LIST.parameterizedBy(typedProductVariantClassName.className))
            .initializer("variants")
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("getMasterVariant")
            .returns(typedProductVariantClassName.className)
            .addStatement("return this.masterVariant")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("getVariants")
            .returns(LIST.parameterizedBy(typedProductVariantClassName.className))
            .addStatement("return this.variants")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .build()
