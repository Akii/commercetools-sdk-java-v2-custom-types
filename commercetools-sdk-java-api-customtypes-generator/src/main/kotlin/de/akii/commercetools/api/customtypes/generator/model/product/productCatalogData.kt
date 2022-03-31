package de.akii.commercetools.api.customtypes.generator.model.product

import com.commercetools.api.models.product.ProductCatalogDataImpl
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*

fun productCatalogData(
    typedProductCatalogDataClassName: TypedProductCatalogData,
    typedProductDataClassName: TypedProductData
): TypeSpec = TypeSpec
    .classBuilder(typedProductCatalogDataClassName.className)
    .addAnnotation(generated)
    .addAnnotation(deserializeAs(typedProductCatalogDataClassName.className))
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
            .builder("current", typedProductDataClassName.className)
            .initializer("current")
            .addModifiers(KModifier.PRIVATE)
            .build()
    )
    .addProperty(
        PropertySpec
            .builder("staged", typedProductDataClassName.className)
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
            .builder("getStaged")
            .returns(typedProductDataClassName.className)
            .addStatement("return this.staged")
            .addModifiers(KModifier.OVERRIDE)
            .build()
    )
    .build()