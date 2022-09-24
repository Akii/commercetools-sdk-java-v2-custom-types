package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.product.ProductCatalogDataBuilder
import com.commercetools.api.models.product.ProductCatalogDataImpl
import com.commercetools.api.models.product.ProductData
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*

fun typedProductCatalogDataBuilderExtensionFunctions(
    typedProductCatalogDataClassName: TypedProductCatalogData,
    typedProductDataClassName: TypedProductData
): Pair<FunSpec, FunSpec> {
    val build = FunSpec
        .builder("build${typedProductCatalogDataClassName.className.simpleName}")
        .receiver(ProductCatalogDataBuilder::class)
        .addCode(
            "return %1T(this.build() as %2T, this.current as %3T, this.staged as %3T)",
            typedProductCatalogDataClassName.className,
            ProductCatalogDataImpl::class,
            typedProductDataClassName.className
        )
        .returns(typedProductCatalogDataClassName.className)
        .build()

    val buildUnchecked = FunSpec
        .builder("build${typedProductCatalogDataClassName.className.simpleName}Unchecked")
        .receiver(ProductCatalogDataBuilder::class)
        .addCode(
            "return %1T(this.buildUnchecked() as %2T, this.current as %3T, this.staged as %3T)",
            typedProductCatalogDataClassName.className,
            ProductCatalogDataImpl::class,
            typedProductDataClassName.className
        )
        .returns(typedProductCatalogDataClassName.className)
        .build()

    return build to buildUnchecked
}

fun productCatalogData(
    typedProductCatalogDataClassName: TypedProductCatalogData,
    typedProductDataClassName: TypedProductData
): TypeSpec = TypeSpec
    .classBuilder(typedProductCatalogDataClassName.className)
    .addAnnotation(generated)
    .addAnnotation(deserializeAs(typedProductCatalogDataClassName.className))
    .addModifiers(KModifier.DATA)
    .primaryConstructor(
        FunSpec
        .constructorBuilder()
        .addAnnotation(jsonCreator)
        .addParameter(
            ParameterSpec
            .builder("delegate", ProductCatalogDataImpl::class)
            .addAnnotation(jsonProperty("delegate"))
            .build()
        )
        .addParameter(
            ParameterSpec
            .builder("current", typedProductDataClassName.className)
            .addAnnotation(jsonProperty("current"))
            .build()
        )
        .addParameter(
            ParameterSpec
            .builder("staged", typedProductDataClassName.className)
            .addAnnotation(jsonProperty("staged"))
            .build()
        )
        .build()
    )
    .addSuperinterface(com.commercetools.api.models.product.ProductCatalogData::class, "delegate")
    .addProperty(
        PropertySpec
            .builder("delegate", ProductCatalogDataImpl::class)
            .addModifiers(KModifier.PRIVATE)
            .initializer("delegate")
            .build()
    )
    .addProperty(
        PropertySpec
            .builder("current", typedProductDataClassName.className)
            .mutable()
            .initializer("current")
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    .addProperty(
        PropertySpec
            .builder("staged", typedProductDataClassName.className)
            .mutable()
            .initializer("staged")
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("getCurrent")
            .returns(typedProductDataClassName.className)
            .addStatement("return this.current")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("setCurrent")
            .addParameter("current", ProductData::class)
            .addStatement("this.current = current as %T", typedProductDataClassName.className)
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("getStaged")
            .returns(typedProductDataClassName.className)
            .addStatement("return this.staged")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .addFunction(
        FunSpec
            .builder("setStaged")
            .addParameter("staged", ProductData::class)
            .addStatement("this.staged = staged as %T", typedProductDataClassName.className)
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .build()