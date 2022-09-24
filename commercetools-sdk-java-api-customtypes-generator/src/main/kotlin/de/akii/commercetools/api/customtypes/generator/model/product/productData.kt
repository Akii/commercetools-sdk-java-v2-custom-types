package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.product.ProductDataBuilder
import com.commercetools.api.models.product.ProductDataImpl
import com.commercetools.api.models.product.ProductVariant
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*

fun typedProductDataBuilderExtensionFunctions(
    typedProductDataClassName: TypedProductData,
    typedProductVariantClassName: TypedProductVariant,
): Pair<FunSpec, FunSpec> {
    val build = FunSpec
        .builder("build${typedProductDataClassName.className.simpleName}")
        .receiver(ProductDataBuilder::class)
        .addCode(
            "return %1T(this.build() as %2T, this.masterVariant as %3T, this.variants.map { it as %3T })",
            typedProductDataClassName.className,
            ProductDataImpl::class,
            typedProductVariantClassName.className
        )
        .returns(typedProductDataClassName.className)
        .build()

    val buildUnchecked = FunSpec
        .builder("build${typedProductDataClassName.className.simpleName}Unchecked")
        .receiver(ProductDataBuilder::class)
        .addCode(
            "return %1T(this.buildUnchecked() as %2T, this.masterVariant as %3T, this.variants.map { it as %3T })",
            typedProductDataClassName.className,
            ProductDataImpl::class,
            typedProductVariantClassName.className
        )
        .returns(typedProductDataClassName.className)
        .build()

    return build to buildUnchecked
}

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
            .mutable()
            .initializer("masterVariant")
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    .addProperty(
        PropertySpec
            .builder("variants", LIST.parameterizedBy(typedProductVariantClassName.className))
            .mutable()
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
            .builder("setMasterVariant")
            .addParameter("masterVariant", ProductVariant::class)
            .addStatement("this.masterVariant = masterVariant as %T", typedProductVariantClassName.className)
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
    .addFunction(
        FunSpec
            .builder("setVariants")
            .addParameter("variants", LIST.parameterizedBy(ProductVariant::class.asTypeName()))
            .addStatement("this.variants = variants.map { it as %T }", typedProductVariantClassName.className)
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("setVariants")
            .addParameter("variants", ProductVariant::class, KModifier.VARARG)
            .addStatement("this.variants = variants.asList().map { it as %T }", typedProductVariantClassName.className)
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .build()
