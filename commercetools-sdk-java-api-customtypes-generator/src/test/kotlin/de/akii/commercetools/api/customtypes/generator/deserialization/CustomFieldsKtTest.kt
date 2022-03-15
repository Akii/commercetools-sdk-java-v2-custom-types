package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.core.type.TypeReference
import com.squareup.kotlinpoet.FileSpec
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.customFieldsFile
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class CustomFieldsKtTest {

    private val types =
        JsonUtils
            .createObjectMapper()
            .readValue(
                javaClass.getResource("/types/types.json"),
                object : TypeReference<List<Type>>() {})

    @Test
    fun `generates a deserializer for custom fields`() {
        val config = Configuration("test.package", listOf(), types)
        val deserializerFile = FileSpec
            .builder("test.package", "customFieldsDeserializer")
            .addType(customFieldsDeserializer(config))
            .build()
        val customFieldsFile = customFieldsFile(types, config)
        val sourceFiles = listOf(
            SourceFile.kotlin("${deserializerFile.name}.kt", deserializerFile.toString()),
            SourceFile.kotlin("${customFieldsFile.name}.kt", customFieldsFile.toString())
        )

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

}