package de.akii.commercetools.api.customtypes.generator.product

import com.commercetools.api.models.common.CreatedBy
import com.commercetools.api.models.common.LastModifiedBy
import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductCatalogData
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.product_type.ProductTypeReference
import com.commercetools.api.models.review.ReviewRatingStatistics
import com.commercetools.api.models.state.StateReference
import com.commercetools.api.models.tax_category.TaxCategoryReference
import com.squareup.kotlinpoet.*
import de.akii.commercetools.api.customtypes.generator.common.*
import io.vrap.rmf.base.client.utils.Generated
import java.time.ZonedDateTime

fun generateProductFile(
    productType: ProductType,
    config: Configuration
): List<FileSpec> {
    val productClassName = ProductClassName(productType, config)
    val productCatalogDataClassName = ProductCatalogDataClassName(productType, config)
    val productDataClassName = ProductDataClassName(productType, config)
    val productVariantClassName = ProductVariantClassName(productType, config)
    val productVariantAttributesClassName = ProductVariantAttributesClassName(productType, config)
    val customProductVariantAttributesClassName = CustomProductVariantAttributesClassName(config)

    val attributeTypeSpec = productVariantAttributes(
        productVariantAttributesClassName,
        customProductVariantAttributesClassName,
        productType.attributes,
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
        .addModifiers(KModifier.DATA)
        .addSuperinterface(Product::class)
        .addAnnotation(Generated::class)
        .addAnnotation(deserializeAs(productClassName.className))
        .addCTProperties(
            SimpleCTProperty("id", String::class),
            SimpleCTProperty("key", String::class, nullable = true),
            SimpleCTProperty("version", Long::class),
            SimpleCTProperty("createdAt", ZonedDateTime::class),
            SimpleCTProperty("createdBy", CreatedBy::class),
            SimpleCTProperty("lastModifiedAt", ZonedDateTime::class),
            SimpleCTProperty("lastModifiedBy", LastModifiedBy::class),
            SimpleCTProperty("productType", ProductTypeReference::class),
            SimpleCTProperty("masterData", productCatalogDataClassName.className, castedFrom = ProductCatalogData::class),
            SimpleCTProperty("taxCategory", TaxCategoryReference::class, nullable = true),
            SimpleCTProperty("state", StateReference::class, nullable = true),
            SimpleCTProperty("reviewRatingStatistics", ReviewRatingStatistics::class, nullable = true),
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