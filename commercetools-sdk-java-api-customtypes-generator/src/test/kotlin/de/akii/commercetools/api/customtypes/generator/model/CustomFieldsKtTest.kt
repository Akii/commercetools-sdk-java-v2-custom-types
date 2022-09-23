package de.akii.commercetools.api.customtypes.generator.model

import com.commercetools.api.models.type.CustomFieldReferenceValue
import com.commercetools.api.models.type.FieldType
import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.core.type.TypeReference
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.customFieldsFiles
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.reflections.Reflections

internal class CustomFieldsKtTest {

    private val types =
        JsonUtils
            .createObjectMapper()
            .readValue(
                javaClass.getResource("/types/types.json"),
                object : TypeReference<List<Type>>() {})

    private val fieldTypesHandled = listOf(
        "CustomFieldBooleanType",
        "CustomFieldStringType",
        "CustomFieldLocalizedStringType",
        "CustomFieldEnumType",
        "CustomFieldLocalizedEnumType",
        "CustomFieldNumberType",
        "CustomFieldMoneyType",
        "CustomFieldDateType",
        "CustomFieldTimeType",
        "CustomFieldDateTimeType",
        "CustomFieldReferenceType",
        "CustomFieldSetType"
    )

    private val customFieldReferenceValuesHandled = listOf(
        CustomFieldReferenceValue.CART,
        CustomFieldReferenceValue.CATEGORY,
        CustomFieldReferenceValue.CHANNEL,
        CustomFieldReferenceValue.CUSTOMER,
        CustomFieldReferenceValue.KEY_VALUE_DOCUMENT,
        CustomFieldReferenceValue.ORDER,
        CustomFieldReferenceValue.PRODUCT,
        CustomFieldReferenceValue.PRODUCT_TYPE,
        CustomFieldReferenceValue.REVIEW,
        CustomFieldReferenceValue.SHIPPING_METHOD,
        CustomFieldReferenceValue.STATE,
        CustomFieldReferenceValue.ZONE
    )

    @Test
    fun `generates typed custom fields`() {
        val config = Configuration("test.package", listOf(), types, emptyMap())
        val files = customFieldsFiles(config)
        val sourceFiles = files.map {
            SourceFile.kotlin("${it.name}.kt", it.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `field types case analysis is exhaustive`() {
        Reflections("com.commercetools.api.models.type")
            .getSubTypesOf(FieldType::class.java)
            .filter { it.isInterface }
            .forEach {
                Assertions.assertThat(fieldTypesHandled).contains(it.simpleName)
            }
    }

    @Test
    fun `custom field reference value case analysis is exhaustive`() {
        CustomFieldReferenceValue
            .values()
            .forEach {
                Assertions.assertThat(customFieldReferenceValuesHandled).contains(it)
            }
    }

}