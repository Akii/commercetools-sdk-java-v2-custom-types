package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductCatalogData
import com.commercetools.api.models.product.ProductData
import com.commercetools.api.models.product.ProductVariant
import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.core.type.TypeReference
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ProductKtTest {

    private val productType =
        JsonUtils
            .createObjectMapper()
            .readValue(
                javaClass.getResource("/product-types/testProductType.json"),
                object : TypeReference<ProductType>() {})

    @Test
    fun `generates custom product classes`() {
        val config = Configuration("test.package", listOf(productType), emptyList())
        val sourceFiles = productFiles(config).map {
            SourceFile.kotlin("${it.name}.kt", it.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        assertThat(
            result.classLoader.loadClass("test.package.product.TestProduct").isAssignableFrom(Product::class.java)
        )
        assertThat(
            result.classLoader.loadClass("test.package.product.TestProductCatalogData")
                .isAssignableFrom(ProductCatalogData::class.java)
        )
        assertThat(
            result.classLoader.loadClass("test.package.product.TestProductData")
                .isAssignableFrom(ProductData::class.java)
        )
        assertThat(
            result.classLoader.loadClass("test.package.product.TestProductVariant")
                .isAssignableFrom(ProductVariant::class.java)
        )

        val variantAttributeClass =
            result.classLoader.loadClass("test.package.product.TestProductVariantAttributes")

        assertThat(variantAttributeClass).hasDeclaredMethods(
            "getABoolean",
            "getAText",
            "getALocText",
            "getAnEnum",
            "getALocEnum",
            "getANumber",
            "getSomeMoney",
            "getADate",
            "getATime",
            "getDateTime",
            "getRefSet",
            "getSameForAll",
            "getNestedSecondType"
        )
    }

}