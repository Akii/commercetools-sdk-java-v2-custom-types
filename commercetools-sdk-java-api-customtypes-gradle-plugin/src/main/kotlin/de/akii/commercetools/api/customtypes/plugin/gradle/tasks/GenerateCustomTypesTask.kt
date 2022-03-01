package de.akii.commercetools.api.customtypes.plugin.gradle.tasks

import com.commercetools.api.models.product_type.ProductType
import com.fasterxml.jackson.core.type.TypeReference
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.productFiles
import io.vrap.rmf.base.client.utils.json.JsonUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option

internal const val GENERATE_CUSTOM_TYPES_TASK_NAME: String = "generateCustomTypes"

@Suppress("UNCHECKED_CAST")
abstract class GenerateCustomTypesTask : DefaultTask() {

    @get:Classpath
    val pluginClasspath: ConfigurableFileCollection = project.objects.fileCollection()

    @Input
    @Optional
    @Option(option = "productTypesFile", description = "JSON file containing product types")
    val customProductTypesFile: Property<String> = project.objects.property(String::class.java)

    @InputFile
    val productTypesFile: RegularFileProperty = project.objects.fileProperty()

    @Input
    @Option(option = "packageName", description = "target package name to use for generated classes")
    val packageName: Property<String> = project.objects.property(String::class.java)

    @Input
    val productTypeNameToSubPackageName: Property<(String) -> String> = project.objects.property(Any::class.java) as Property<(String) -> String>

    @Input
    val productTypeNameToClassNamePrefix: Property<(String) -> String> = project.objects.property(Any::class.java) as Property<(String) -> String>

    @Input
    val attributeNameToPropertyName: Property<(String) -> String> = project.objects.property(Any::class.java) as Property<(String) -> String>

    @Input
    @Optional
    @Option(option = "outputDirectory", description = "target package name to use for generated classes")
    val customOutputDirectory: Property<String> = project.objects.property(String::class.java)

    @OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    init {
        group = "commercetools"
        description = "Generate custom types for the commercetools sdk."

        outputDirectory.convention(project.layout.buildDirectory.dir("generated/source/custom-types/main"))
    }

    @TaskAction
    fun generateCustomTypesAction() {
        val targetDirectory =
            if (customOutputDirectory.isPresent)
                project.layout.projectDirectory.file(customOutputDirectory.get()).asFile
            else
                outputDirectory.get().asFile

        if (!targetDirectory.isDirectory && !targetDirectory.mkdirs()) {
            throw RuntimeException("Failed to generate generated source directory: $targetDirectory")
        }

        val productTypesFile =
            if (customProductTypesFile.isPresent)
                project.layout.projectDirectory.file(customProductTypesFile.get()).asFile
            else
                productTypesFile.get().asFile

        val productTypes = JsonUtils
            .createObjectMapper()
            .readValue(productTypesFile, object : TypeReference<List<ProductType>>() {})

        val config = Configuration(
            packageName.get(),
            productTypes,
            productTypeNameToSubPackageName = { productTypeNameToSubPackageName.get()(it) },
            productTypeNameToClassNamePrefix = { productTypeNameToClassNamePrefix.get()(it) },
            attributeNameToPropertyName = { attributeNameToPropertyName.get()(it) },
        )

        val files = productFiles(config)

        files.forEach {
            it.writeTo(targetDirectory)
        }
    }
}
