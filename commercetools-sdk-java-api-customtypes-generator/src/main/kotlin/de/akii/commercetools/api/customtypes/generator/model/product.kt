package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.product.ProductBuilder
import com.commercetools.api.models.product.ProductCatalogData
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*

fun typedProductInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(TypedProductInterface(config).className)
        .addAnnotation(generated)
        .build()

fun typedProductVariantAttributesInterface(config: Configuration) =
    TypeSpec
        .interfaceBuilder(TypedProductVariantAttributesInterface(config).className)
        .addAnnotation(generated)
        .build()

fun typedProductBuilderExtensionFunctions(
    typedProductClassName: TypedProduct,
    typedProductCatalogDataClassName: TypedProductCatalogData
): Pair<FunSpec, FunSpec> {
    val build = FunSpec
        .builder("build${typedProductClassName.className.simpleName}")
        .addAnnotation(throwsClassCastExceptions)
        .receiver(ProductBuilder::class)
        .addCode(
            "return %1T(this.build() as %2T, this.masterData as %3T)",
            typedProductClassName.className,
            ProductImpl::class,
            typedProductCatalogDataClassName.className
        )
        .returns(typedProductClassName.className)
        .build()

    val buildUnchecked = FunSpec
        .builder("build${typedProductClassName.className.simpleName}Unchecked")
        .addAnnotation(throwsClassCastExceptions)
        .receiver(ProductBuilder::class)
        .addCode(
            "return %1T(this.build() as %2T, this.masterData as %3T)",
            typedProductClassName.className,
            ProductImpl::class,
            typedProductCatalogDataClassName.className
        )
        .returns(typedProductClassName.className)
        .build()

    return build to buildUnchecked
}

fun typedProduct(productType: ProductType, config: Configuration): TypeSpec =
    TypeSpec
        .classBuilder(TypedProduct(productType, config).className)
        .addAnnotation(generated)
        .addAnnotation(deserializeAs(TypedProduct(productType, config).className))
        .addModifiers(KModifier.DATA)
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addAnnotation(jsonCreator)
            .addParameter(ParameterSpec
                .builder("delegate", ProductImpl::class)
                .addAnnotation(jsonProperty("delegate"))
                .build()
            )
            .addParameter(ParameterSpec
                .builder("masterData", TypedProductCatalogData(productType, config).className)
                .addAnnotation(jsonProperty("masterData"))
                .build()
            )
            .build()
        )
        .addSuperinterface(com.commercetools.api.models.product.Product::class, "delegate")
        .addSuperinterface(TypedProductInterface(config).className)
        .addType(TypeSpec
            .companionObjectBuilder()
            .addProperty(PropertySpec
                .builder("TYPE_KEY", String::class)
                .addModifiers(KModifier.CONST)
                .initializer("%S", config.productTypeToKey(productType))
                .build()
            )
            .build()
        )
        .addProperty(
            PropertySpec
                .builder("delegate", ProductImpl::class)
                .addModifiers(KModifier.PRIVATE)
                .initializer("delegate")
                .build()
        )
        .addProperty(
            PropertySpec
                .builder("masterData", TypedProductCatalogData(productType, config).className)
                .mutable()
                .initializer("masterData")
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        .addFunction(
            FunSpec
                .builder("getMasterData")
                .returns(TypedProductCatalogData(productType, config).className)
                .addStatement("return this.masterData")
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        .addFunction(
            FunSpec
                .builder("setMasterData")
                .addAnnotation(throwsClassCastExceptions)
                .addParameter("masterData", ProductCatalogData::class)
                .addStatement("this.masterData = masterData as %T", TypedProductCatalogData(productType, config).className)
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        .build()