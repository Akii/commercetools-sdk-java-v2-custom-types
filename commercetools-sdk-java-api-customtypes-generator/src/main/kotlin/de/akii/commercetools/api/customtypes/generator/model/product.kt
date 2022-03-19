package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.model.product.productCatalogData
import de.akii.commercetools.api.customtypes.generator.model.product.productData
import de.akii.commercetools.api.customtypes.generator.model.product.productVariant
import de.akii.commercetools.api.customtypes.generator.model.product.productVariantAttributes
import io.vrap.rmf.base.client.utils.Generated

fun productFiles(config: Configuration): List<FileSpec> =
    config.productTypes.flatMap { productFiles(it, config) } + customProductVariantAttributesInterface(config)

private fun customProductVariantAttributesInterface(config: Configuration) =
    FileSpec
        .builder("${config.packageName}.product", "CustomProductVariantAttributes")
        .addType(
            TypeSpec
                .interfaceBuilder(CustomProductVariantAttributes(config).className)
                .addAnnotation(Generated::class)
                .build()
        )
        .build()

private fun productFiles(
    productType: ProductType,
    config: Configuration
): List<FileSpec> {
    val productClassName = Product(productType, config)
    val productCatalogDataClassName = ProductCatalogData(productType, config)
    val productDataClassName = ProductData(productType, config)
    val productVariantClassName = ProductVariant(productType, config)
    val productVariantAttributesClassName = ProductVariantAttributes(productType, config)
    val customProductVariantAttributesClassName = CustomProductVariantAttributes(config)

    val attributeTypeSpec = productVariantAttributes(
        productVariantAttributesClassName,
        customProductVariantAttributesClassName,
        productType,
        config
    )

    val variantTypeSpec = productVariant(
        productVariantClassName,
        productVariantAttributesClassName
    )

    val productDataTypeSpec = productData(
        productDataClassName,
        productVariantClassName
    )

    val masterDataTypeSpec = productCatalogData(
        productCatalogDataClassName,
        productDataClassName
    )

    val product = TypeSpec
        .classBuilder(productClassName.className)
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(productClassName.className))
        .primaryConstructor(FunSpec
            .constructorBuilder()
            .addAnnotation(jsonCreator)
            .addParameter(ParameterSpec
                .builder("delegate", ProductImpl::class)
                .addAnnotation(jsonProperty("delegate"))
                .build()
            )
            .addParameter(ParameterSpec
                .builder("masterData", productCatalogDataClassName.className)
                .addAnnotation(jsonProperty("masterData"))
                .build()
            )
            .build()
        )
        .addSuperinterface(com.commercetools.api.models.product.Product::class, "delegate")
        .addProperty(
            PropertySpec
                .builder("masterData", productCatalogDataClassName.className)
                .initializer("masterData")
                .addModifiers(KModifier.PRIVATE)
                .build()
        )
        .addFunction(
            FunSpec
                .builder("getMasterData")
                .returns(productCatalogDataClassName.className)
                .addStatement("return this.masterData")
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )
        .build()

    return listOf(
        makeFile(productClassName.className, product),
        makeFile(productCatalogDataClassName.className, masterDataTypeSpec),
        makeFile(productDataClassName.className, productDataTypeSpec),
        makeFile(productVariantClassName.className, variantTypeSpec),
        makeFile(productVariantAttributesClassName.className, attributeTypeSpec)
    )
}

private fun makeFile(className: ClassName, type: TypeSpec): FileSpec =
    FileSpec
        .builder(className.packageName, className.simpleName)
        .addType(type)
        .build()