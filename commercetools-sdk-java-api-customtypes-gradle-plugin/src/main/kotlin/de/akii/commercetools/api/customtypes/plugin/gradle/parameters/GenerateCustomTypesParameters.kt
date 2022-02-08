package de.akii.commercetools.api.customtypes.plugin.gradle.parameters

import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters
import java.io.File

interface GenerateCustomTypesParameters : WorkParameters {
    val productTypesFile: Property<File>
    val packageName: Property<String>
    val targetDirectory: Property<File>
}
