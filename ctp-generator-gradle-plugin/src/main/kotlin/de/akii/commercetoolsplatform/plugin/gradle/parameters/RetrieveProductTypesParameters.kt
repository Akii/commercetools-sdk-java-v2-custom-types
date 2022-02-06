package de.akii.commercetoolsplatform.plugin.gradle.parameters

import com.commercetools.api.defaultconfig.ServiceRegion
import org.gradle.api.provider.Property
import org.gradle.workers.WorkParameters
import java.io.File

interface RetrieveProductTypesParameters : WorkParameters {
    val clientId: Property<String>
    val clientSecret: Property<String>
    val serviceRegion: Property<ServiceRegion>
    val projectName: Property<String>
    val productTypesFile: Property<File>
}
