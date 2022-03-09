package de.akii.commercetools.api.customtypes.generator

import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.core.type.TypeReference
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class AddressKtTest {

    private val types =
        JsonUtils
            .createObjectMapper()
            .readValue(
                javaClass.getResource("/types/types.json"),
                object : TypeReference<List<Type>>() {})

    @Test
    fun `generates typed addresses`() {
        val config = Configuration("test.package", listOf(), types)
        val files = listOf(
            customFieldsFile(types, config),
            typedAddressFile(config)
        )

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

}