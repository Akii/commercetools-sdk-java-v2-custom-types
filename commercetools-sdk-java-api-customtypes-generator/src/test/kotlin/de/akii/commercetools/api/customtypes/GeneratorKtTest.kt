package de.akii.commercetools.api.customtypes

import com.commercetools.api.models.category.Category
import com.commercetools.api.models.category.CategoryImpl
import com.commercetools.api.models.product.Product
import com.commercetools.api.models.product.ProductImpl
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.module.SimpleModule
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.test.assertNull

internal class GeneratorKtTest {

    private val productType =
        JsonUtils
            .createObjectMapper()
            .readValue(
                javaClass.getResource("/product-types/testProductType.json"),
                object : TypeReference<ProductType>() {})

    private val types =
        JsonUtils
            .createObjectMapper()
            .readValue(
                javaClass.getResource("/types/types.json"),
                object : TypeReference<List<Type>>() {})

    private val config = Configuration("test.package", listOf(productType), types)

    private val testProducts = javaClass.getResource("/products/testProducts.json")

    private val testProduct = javaClass.getResource("/products/testProduct.json")

    private val testCategories = javaClass.getResource("/categories/testCategories.json")

    private val testCategory = javaClass.getResource("/categories/testCategory.json")

    @Test
    fun `it compiles without any product-types or custom field types`() {
        val sourceFiles = generate(Configuration("test.package", emptyList(), emptyList())).map {
            SourceFile.kotlin("${it.packageName}.${it.name}.kt", it.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `api module can deserialize custom product types`() {
        val sourceFiles = generate(config).map {
            SourceFile.kotlin("${it.packageName}.${it.name}.kt", it.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val apiModule = result.classLoader
            .loadClass("test.package.TypedProductApiModule")
            .getDeclaredConstructor()
            .newInstance() as SimpleModule

        val products = JsonUtils
            .createObjectMapper()
            .registerModule(apiModule)
            .readValue(testProducts, object : TypeReference<List<Product>>() {})

        val testProduct = products[0]
        val fallbackProduct = products[1]

        assertThat(result.classLoader.loadClass("test.package.product.TestProduct").isInstance(testProduct))
        assertThat(ProductImpl::class.java.isInstance(fallbackProduct))

        val testProductVariant = testProduct
            .masterData
            .current
            .masterVariant

        val typedAttributes = invokeMethod("getTypedAttributes", testProductVariant)!!

        assertThat(invokeMethod("getABoolean", typedAttributes) as Boolean).isTrue
        assertThat(invokeMethod("getAText", typedAttributes) as String).isEqualTo("Text!")
        assertThat(invokeMethod("getAnEnum", typedAttributes)).isNotNull
        assertThat(invokeMethod("getALocEnum", typedAttributes)).isNotNull
        assertThat(invokeMethod("getANumber", typedAttributes) as Int).isEqualTo(106)
        assertThat(invokeMethod("getSomeMoney", typedAttributes)).isNotNull
        assertThat(invokeMethod("getADate", typedAttributes)).isEqualTo(LocalDate.parse("2022-02-02"))
        assertThat(invokeMethod("getATime", typedAttributes)).isEqualTo(LocalTime.parse("12:30:00.000"))
        assertThat(invokeMethod("getDateTime", typedAttributes)).isEqualTo(ZonedDateTime.parse("2022-02-04T00:00:00.000Z"))
        assertThat(invokeMethod("getRefSet", typedAttributes)).isNull()
        assertThat(invokeMethod("getSameForAll", typedAttributes)).isNull()
        assertThat(invokeMethod("getNestedSecondType", typedAttributes)).isNull()
    }

    @Test
    fun `api module can deserialize specific custom product classes`() {
        val sourceFiles = generate(config).map {
            SourceFile.kotlin("${it.packageName}.${it.name}.kt", it.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val apiModule = result.classLoader
            .loadClass("test.package.TypedProductApiModule")
            .getDeclaredConstructor()
            .newInstance() as SimpleModule

        val testProductClass = result.classLoader.loadClass("test.package.product.TestProduct")

        val product = JsonUtils
            .createObjectMapper()
            .registerModule(apiModule)
            .readValue(testProduct, testProductClass)

        assertThat(testProductClass.isInstance(product))
    }

    @Test
    fun `api module can deserialize typed custom fields`() {
        val sourceFiles = generate(config).map {
            SourceFile.kotlin("${it.packageName}.${it.name}.kt", it.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val apiModule = result.classLoader
            .loadClass("test.package.TypedResourcesApiModule")
            .getDeclaredConstructor()
            .newInstance() as SimpleModule

        val categories = JsonUtils
            .createObjectMapper()
            .registerModule(apiModule)
            .readValue(testCategories, object : TypeReference<List<Category>>() {})

        val category1 = categories[0]
        val category2 = categories[1]

        val typeACategoryClass = result.classLoader.loadClass("test.package.category.TypeACategory")

        assertThat(category1.javaClass).isEqualTo(CategoryImpl::class.java)
        assertThat(category2.javaClass).isEqualTo(typeACategoryClass)

        assertNull(category1.custom)

        val custom2 = invokeMethod("getTypedFields", category2.custom)!!

        assertThat(invokeMethod("getABoolean", custom2) as Boolean).isFalse
    }

    @Test
    fun `api module can deserialize specific typed custom fields classes`() {
        val sourceFiles = generate(config).map {
            SourceFile.kotlin("${it.packageName}.${it.name}.kt", it.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val apiModule = result.classLoader
            .loadClass("test.package.TypedResourcesApiModule")
            .getDeclaredConstructor()
            .newInstance() as SimpleModule

        val typeACategoryClass = result.classLoader.loadClass("test.package.category.TypeACategory")

        val category = JsonUtils
            .createObjectMapper()
            .registerModule(apiModule)
            .readValue(testCategory, typeACategoryClass)

        assertThat(typeACategoryClass.isInstance(category))
    }

    private fun invokeMethod(name: String, obj: Any): Any? =
        obj.javaClass.getMethod(name).invoke(obj)

}