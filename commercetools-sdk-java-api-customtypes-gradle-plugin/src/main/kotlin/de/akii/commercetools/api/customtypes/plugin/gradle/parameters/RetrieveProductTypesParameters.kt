package de.akii.commercetools.api.customtypes.plugin.gradle.parameters

import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters
import java.io.File

interface RetrieveProductTypesParameters : WorkParameters {
    val clientId: Property<String>
    val clientSecret: Property<String>
    val serviceRegion: Property<String>
    val projectName: Property<String>
    val productTypesFile: Property<File>
}
