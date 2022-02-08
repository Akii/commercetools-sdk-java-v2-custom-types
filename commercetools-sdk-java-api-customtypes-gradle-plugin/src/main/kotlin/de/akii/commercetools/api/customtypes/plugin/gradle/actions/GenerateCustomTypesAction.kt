package de.akii.commercetools.api.customtypes.plugin.gradle.actions

import de.akii.commercetools.api.customtypes.generator.Configuration
import de.akii.commercetools.api.customtypes.generator.generateProductTypeFiles
import de.akii.commercetools.api.customtypes.generator.types.ProductType
import de.akii.commercetools.api.customtypes.plugin.gradle.parameters.GenerateCustomTypesParameters
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.workers.WorkAction

@OptIn(ExperimentalSerializationApi::class)
abstract class GenerateCustomTypesAction : WorkAction<GenerateCustomTypesParameters> {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun execute() {
        val productTypes = json.decodeFromStream(
            ListSerializer(ProductType.serializer()),
            parameters.productTypesFile.get().inputStream()
        )

        val files = generateProductTypeFiles(productTypes, Configuration(parameters.packageName.get()))

        files.forEach {
            it.writeTo(parameters.targetDirectory.get())
        }
    }

}
