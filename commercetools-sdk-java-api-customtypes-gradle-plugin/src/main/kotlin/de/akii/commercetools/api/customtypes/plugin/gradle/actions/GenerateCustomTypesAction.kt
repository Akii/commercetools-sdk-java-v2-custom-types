package de.akii.commercetools.api.customtypes.plugin.gradle.actions

import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.core.type.TypeReference
import de.akii.commercetools.api.customtypes.generator.Configuration
import de.akii.commercetools.api.customtypes.generator.productFiles
import de.akii.commercetools.api.customtypes.plugin.gradle.parameters.GenerateCustomTypesParameters
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.gradle.workers.WorkAction

abstract class GenerateCustomTypesAction : WorkAction<GenerateCustomTypesParameters> {

    override fun execute() {
        val productTypes = JsonUtils
            .createObjectMapper()
            .readValue(parameters.productTypesFile.get(), object : TypeReference<List<ProductType>>() {})

        val files = productFiles(Configuration(parameters.packageName.get(), productTypes))

        files.forEach {
            it.writeTo(parameters.targetDirectory.get())
        }
    }

}
