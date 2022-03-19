package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.common.CreatedBy
import com.commercetools.api.models.common.LastModifiedBy
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.product_type.ProductTypeReference
import com.commercetools.api.models.review.ReviewRatingStatistics
import com.commercetools.api.models.state.StateReference
import com.commercetools.api.models.tax_category.TaxCategoryReference
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import de.akii.commercetools.api.customtypes.generator.model.product.productCatalogData
import de.akii.commercetools.api.customtypes.generator.model.product.productData
import de.akii.commercetools.api.customtypes.generator.model.product.productVariant
import de.akii.commercetools.api.customtypes.generator.model.product.productVariantAttributes
import io.vrap.rmf.base.client.utils.Generated
import java.time.ZonedDateTime

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
        .superclass(ProductImpl::class)
        .addAnnotation(deserializeAs(productClassName.className))
        .addCTConstructorArguments(
            CTParameter("id", String::class),
            CTParameter("key", String::class, nullable = true),
            CTParameter("version", Long::class),
            CTParameter("createdAt", ZonedDateTime::class),
            CTParameter("createdBy", CreatedBy::class),
            CTParameter("lastModifiedAt", ZonedDateTime::class),
            CTParameter("lastModifiedBy", LastModifiedBy::class),
            CTParameter("productType", ProductTypeReference::class),
            CTProperty("masterData", productCatalogDataClassName.className),
            CTParameter("taxCategory", TaxCategoryReference::class, nullable = true),
            CTParameter("state", StateReference::class, nullable = true),
            CTParameter("reviewRatingStatistics", ReviewRatingStatistics::class, nullable = true),
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