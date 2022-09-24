package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.product.ProductProjectionBuilder
import com.commercetools.api.models.product.ProductProjectionImpl
import com.commercetools.api.models.product.ProductVariant
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import de.akii.commercetools.api.customtypes.generator.common.*

fun typedProductProjectionInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(TypedProductProjectionInterface(config).className)
        .addAnnotation(generated)
        .build()

fun typedProductProjectionBuilderExtensionFunctions(
    typedProductProjectionClassName: TypedProductProjection,
    typedProductVariantClassName: TypedProductVariant,
): Pair<FunSpec, FunSpec> {
    val build = FunSpec
        .builder("build${typedProductProjectionClassName.className.simpleName}")
        .receiver(ProductProjectionBuilder::class)
        .addCode(
            "return %1T(this.build() as %2T, this.masterVariant as %3T, this.variants.map { it as %3T })",
            typedProductProjectionClassName.className,
            ProductProjectionImpl::class,
            typedProductVariantClassName.className
        )
        .returns(typedProductProjectionClassName.className)
        .build()

    val buildUnchecked = FunSpec
        .builder("build${typedProductProjectionClassName.className.simpleName}Unchecked")
        .receiver(ProductProjectionBuilder::class)
        .addCode(
            "return %1T(this.buildUnchecked() as %2T, this.masterVariant as %3T, this.variants.map { it as %3T })",
            typedProductProjectionClassName.className,
            ProductProjectionImpl::class,
            typedProductVariantClassName.className
        )
        .returns(typedProductProjectionClassName.className)
        .build()

    return build to buildUnchecked
}

fun productProjection(
    typedProductProjectionClassName: TypedProductProjection,
    typedProductVariantClassName: TypedProductVariant,
    config: Configuration
): TypeSpec = TypeSpec
    .classBuilder(typedProductProjectionClassName.className)
    .addAnnotation(generated)
    .addAnnotation(deserializeAs(typedProductProjectionClassName.className))
    .addModifiers(KModifier.DATA)
    .primaryConstructor(FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameter(ParameterSpec
            .builder("delegate", ProductProjectionImpl::class)
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
    .addSuperinterface(com.commercetools.api.models.product.ProductProjection::class, "delegate")
    .addSuperinterface(TypedProductProjectionInterface(config).className)
    .addProperty(
        PropertySpec
            .builder("delegate", ProductProjectionImpl::class)
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
