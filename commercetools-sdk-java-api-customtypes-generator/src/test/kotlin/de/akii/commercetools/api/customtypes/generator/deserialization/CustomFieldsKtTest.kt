package de.akii.commercetools.api.customtypes.generator.deserialization

import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.core.type.TypeReference
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.model.customFieldsFile
import de.akii.commercetools.api.customtypes.generator.model.typedResourceFiles
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
        val typedResources = typedResourceFiles(config)
        val customFieldsFile = customFieldsFile(config)
        val customFieldsDeserializerFile = customFieldsDeserializerFile(config)
        val apiModulesFile = apiModulesFile(typedResources, config)

        val sourceFiles = listOf(
            SourceFile.kotlin("${customFieldsDeserializerFile.packageName}.${customFieldsDeserializerFile.name}.kt", customFieldsDeserializerFile.toString()),
            SourceFile.kotlin("${customFieldsFile.packageName}.${customFieldsFile.name}.kt", customFieldsFile.toString()),
            SourceFile.kotlin("${apiModulesFile.packageName}.${apiModulesFile.name}.kt", apiModulesFile.toString())
        ) + typedResources.map {
            SourceFile.kotlin("${it.file.packageName}.${it.file.name}.kt", it.file.toString())
        }

        val result = KotlinCompilation().apply {
            sources = sourceFiles
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile()

        Assertions.assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

}