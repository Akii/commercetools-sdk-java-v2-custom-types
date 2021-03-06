package de.akii.commercetools.api.customtypes.plugin.gradle.tasks

import com.commercetools.api.models.product_type.AttributeDefinition
import com.commercetools.api.models.product_type.ProductType
import com.commercetools.api.models.type.FieldDefinition
import com.commercetools.api.models.type.Type
import com.fasterxml.jackson.core.type.TypeReference
import de.akii.commercetools.api.customtypes.generate
import de.akii.commercetools.api.customtypes.generator.common.Configuration
import de.akii.commercetools.api.customtypes.generator.common.ProductClassType
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

    @InputFile
    @Optional
    val productTypesFile: RegularFileProperty = project.objects.fileProperty()

    @InputFile
    @Optional
    val typesFile: RegularFileProperty = project.objects.fileProperty()

    @Input
    @Option(option = "packageName", description = "target package name to use for generated classes")
    val packageName: Property<String> = project.objects.property(String::class.java)

    @Internal
    val productTypeToKey: Property<(ProductType) -> String> = project.objects.property(Any::class.java) as Property<(ProductType) -> String>

    @Internal
    val productTypeToClassName: Property<(ProductType, ProductClassType) -> String> = project.objects.property(Any::class.java) as Property<(ProductType, ProductClassType) -> String>

    @Internal
    val attributeToPropertyName: Property<(ProductType, AttributeDefinition) -> String> = project.objects.property(Any::class.java) as Property<(ProductType, AttributeDefinition) -> String>

    @Internal
    val isAttributeRequired: Property<(ProductType, AttributeDefinition) -> Boolean> = project.objects.property(Any::class.java) as Property<(ProductType, AttributeDefinition) -> Boolean>

    @Internal
    val typeToKey: Property<(Type) -> String> = project.objects.property(Any::class.java) as Property<(Type) -> String>

    @Internal
    val typeToCustomFieldsClassName: Property<(Type) -> String> = project.objects.property(Any::class.java) as Property<(Type) -> String>

    @Internal
    val typeToResourceClassName: Property<(Type, String) -> String> = project.objects.property(Any::class.java) as Property<(Type, String) -> String>

    @Internal
    val fieldToPropertyName: Property<(Type, FieldDefinition) -> String> = project.objects.property(Any::class.java) as Property<(Type, FieldDefinition) -> String>

    @Internal
    val isFieldRequired: Property<(Type, FieldDefinition) -> Boolean> = project.objects.property(Any::class.java) as Property<(Type, FieldDefinition) -> Boolean>

    @Internal
    val containerTypes: Property<Map<String, String>> = project.objects.property(Any::class.java) as Property<Map<String, String>>

    @Internal
    val containerNameToClassName: Property<(String, String) -> String> = project.objects.property(Any::class.java) as Property<(String, String) -> String>

    @OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    init {
        group = "commercetools"
        description = "Generate custom types for the commercetools sdk."

        outputDirectory.convention(project.layout.buildDirectory.dir("generated/source/custom-types/main"))
    }

    @TaskAction
    fun generateCustomTypesAction() {
        val targetDirectory = outputDirectory.get().asFile
        if (!targetDirectory.isDirectory && !targetDirectory.mkdirs()) {
            throw RuntimeException("Failed to generate generated source directory: $targetDirectory")
        }

        var productTypes = emptyList<ProductType>()
        var types = emptyList<Type>()

        if (productTypesFile.isPresent) {
            val productTypesFile = productTypesFile.get().asFile
            productTypes = JsonUtils
                .createObjectMapper()
                .readValue(productTypesFile, object : TypeReference<List<ProductType>>() {})
        }

        if (typesFile.isPresent) {
            val typesFile = typesFile.get().asFile
            types = JsonUtils
                .createObjectMapper()
                .readValue(typesFile, object : TypeReference<List<Type>>() {})
        }

        val config = Configuration(
            packageName.get(),
            productTypes,
            types,
            containerTypes.get(),
            productTypeToKey = { type -> productTypeToKey.get()(type) },
            productTypeToClassName = { type, classType -> productTypeToClassName.get()(type, classType) },
            attributeToPropertyName = { type, attribute -> attributeToPropertyName.get()(type, attribute) },
            isAttributeRequired = { type, attribute -> isAttributeRequired.get()(type, attribute) },
            typeToKey = { type -> typeToKey.get()(type) },
            typeToCustomFieldsClassName = { type -> typeToCustomFieldsClassName.get()(type) },
            typeToResourceClassName = { type, resourceName -> typeToResourceClassName.get()(type, resourceName) },
            fieldToPropertyName = { type, field -> fieldToPropertyName.get()(type, field) },
            isFieldRequired = { type, field -> isFieldRequired.get()(type, field) },
            containerNameToClassName = { containerName, className -> containerNameToClassName.get()(containerName, className) }
        )

        val files = generate(config)

        files.forEach {
            it.writeTo(targetDirectory)
        }
    }

    fun getIsAttributeRequired(): Property<(ProductType, AttributeDefinition) -> Boolean> =
        isAttributeRequired

    fun getIsFieldRequired(): Property<(Type, FieldDefinition) -> Boolean> =
        isFieldRequired

}