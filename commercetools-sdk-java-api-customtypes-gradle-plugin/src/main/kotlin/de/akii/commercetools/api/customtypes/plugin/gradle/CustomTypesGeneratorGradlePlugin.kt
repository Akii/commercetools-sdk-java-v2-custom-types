package de.akii.commercetools.api.customtypes.plugin.gradle

import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.*
import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.FETCH_PRODUCT_TYPES_TASK_NAME
import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.GENERATE_CUSTOM_TYPES_TASK_NAME
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.SourceSetContainer

private const val PLUGIN_EXTENSION_NAME = "commercetoolsCustomTypesGenerator"

class CustomTypesGeneratorGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        configurePluginDependencies(project)
        registerTasks(project)

        val extension = project.extensions.create(PLUGIN_EXTENSION_NAME, CustomTypesGeneratorPluginExtension::class.java)
        project.afterEvaluate {
            processExtensionConfiguration(project, extension)
            configureTaskClasspaths(project)
        }
    }

    private fun configurePluginDependencies(project: Project) {
        project.configurations.create(FETCH_PRODUCT_TYPES_TASK_NAME) { configuration ->
            configuration.isVisible = true
            configuration.isTransitive = true
            configuration.description = "Configuration for fetching commercetools product-types"

            configuration.dependencies.add(project.dependencies.create("de.akii.commercetools:commercetools-sdk-java-api-customtypes-generator:$DEFAULT_PLUGIN_VERSION"))
            configuration.dependencies.add(project.dependencies.create("com.commercetools.sdk:commercetools-http-client:$DEFAULT_COMMERCETOOLS_VERSION"))
        }

        project.configurations.create(FETCH_TYPES_TASK_NAME) { configuration ->
            configuration.isVisible = true
            configuration.isTransitive = true
            configuration.description = "Configuration for fetching commercetools types"

            configuration.dependencies.add(project.dependencies.create("de.akii.commercetools:commercetools-sdk-java-api-customtypes-generator:$DEFAULT_PLUGIN_VERSION"))
            configuration.dependencies.add(project.dependencies.create("com.commercetools.sdk:commercetools-http-client:$DEFAULT_COMMERCETOOLS_VERSION"))
        }

        project.configurations.create(GENERATE_CUSTOM_TYPES_TASK_NAME) { configuration ->
            configuration.isVisible = true
            configuration.isTransitive = true
            configuration.description = "Configuration for generating commercetools custom types"

            configuration.dependencies.add(project.dependencies.create("de.akii.commercetools:commercetools-sdk-java-api-customtypes-generator:$DEFAULT_PLUGIN_VERSION"))
        }
    }

    private fun registerTasks(project: Project) {
        project.tasks.register(FETCH_PRODUCT_TYPES_TASK_NAME, FetchProductTypesTask::class.java)
        project.tasks.register(FETCH_TYPES_TASK_NAME, FetchTypesTask::class.java)
        project.tasks.register(GENERATE_CUSTOM_TYPES_TASK_NAME, GenerateCustomTypesTask::class.java)
    }

    private fun processExtensionConfiguration(project: Project, extension: CustomTypesGeneratorPluginExtension) {
        configureCustomProductTypesGeneration(project, extension)
    }

    private fun configureCustomProductTypesGeneration(project: Project, extension: CustomTypesGeneratorPluginExtension) {
        val generateCustomTypesTask = project.tasks.named(GENERATE_CUSTOM_TYPES_TASK_NAME, GenerateCustomTypesTask::class.java).get()

        if (extension.productTypesGeneratorConfigured()) {
            generateCustomTypesTask.productTypesFile.set(extension.productTypesGeneratorExtension.productTypesFile)
        }

        if (extension.typesGeneratorConfigured()) {
            generateCustomTypesTask.typesFile.set(extension.typesGeneratorExtension.typesFile)
        }

        val productTypesGeneratorExtension = extension.productTypesGeneratorExtension
        val typesGeneratorExtension = extension.typesGeneratorExtension
        val customObjectGeneratorExtension = extension.customObjectGeneratorExtension

        generateCustomTypesTask.packageName.convention(project.provider { extension.packageName })
        generateCustomTypesTask.productTypeToKey.convention(project.provider { productTypesGeneratorExtension.productTypeToKey })
        generateCustomTypesTask.productTypeToClassName.convention(project.provider { productTypesGeneratorExtension.productTypeToClassName })
        generateCustomTypesTask.attributeToPropertyName.convention(project.provider { productTypesGeneratorExtension.attributeToPropertyName })
        generateCustomTypesTask.isAttributeRequired.convention(project.provider { productTypesGeneratorExtension.isAttributeRequired })
        generateCustomTypesTask.typeToKey.convention(project.provider { typesGeneratorExtension.typeToKey })
        generateCustomTypesTask.typeToCustomFieldsClassName.convention(project.provider { typesGeneratorExtension.typeToCustomFieldsClassName })
        generateCustomTypesTask.typeToResourceClassName.convention(project.provider { typesGeneratorExtension.typeToResourceClassName })
        generateCustomTypesTask.fieldToPropertyName.convention(project.provider { typesGeneratorExtension.fieldToPropertyName })
        generateCustomTypesTask.isFieldRequired.convention(project.provider { typesGeneratorExtension.isFieldRequired })
        generateCustomTypesTask.productTypesFile.set(productTypesGeneratorExtension.productTypesFile)
        generateCustomTypesTask.containerTypes.convention(project.provider { customObjectGeneratorExtension.containerTypes })
        generateCustomTypesTask.containerNameToClassName.convention(project.provider { customObjectGeneratorExtension.containerNameToClassName })
        configureDefaultProjectSourceSet(project, generateCustomTypesTask.outputDirectory)

        if (extension.credentialsConfigured()) {
            val credentials = extension.credentialsExtension

            val fetchProductTypesTask =
                project.tasks.named(FETCH_PRODUCT_TYPES_TASK_NAME, FetchProductTypesTask::class.java).get()
            fetchProductTypesTask.clientId.convention(project.provider { credentials.clientId })
            fetchProductTypesTask.clientSecret.convention(project.provider { credentials.clientSecret })
            fetchProductTypesTask.serviceRegion.convention(project.provider { credentials.serviceRegion.toString() })
            fetchProductTypesTask.projectKey.convention(project.provider { credentials.projectKey })

            val fetchTypesTask =
                project.tasks.named(FETCH_TYPES_TASK_NAME, FetchTypesTask::class.java).get()
            fetchTypesTask.clientId.convention(project.provider { credentials.clientId })
            fetchTypesTask.clientSecret.convention(project.provider { credentials.clientSecret })
            fetchTypesTask.serviceRegion.convention(project.provider { credentials.serviceRegion.toString() })
            fetchTypesTask.projectKey.convention(project.provider { credentials.projectKey })

            if (productTypesGeneratorExtension.productTypesFile == null) {
                generateCustomTypesTask.dependsOn(fetchProductTypesTask)
                generateCustomTypesTask.productTypesFile.set(fetchProductTypesTask.outputFile)
            }

            if (typesGeneratorExtension.typesFile == null) {
                generateCustomTypesTask.dependsOn(fetchTypesTask)
                generateCustomTypesTask.typesFile.set(fetchTypesTask.outputFile)
            }
        }
    }

    private fun configureTaskClasspaths(project: Project) {
        project.tasks.withType(FetchProductTypesTask::class.java).configureEach { fetchTask ->
            val configuration = project.configurations.getAt(FETCH_PRODUCT_TYPES_TASK_NAME)
            fetchTask.pluginClasspath.setFrom(configuration)
        }

        project.tasks.withType(FetchTypesTask::class.java).configureEach { fetchTask ->
            val configuration = project.configurations.getAt(FETCH_TYPES_TASK_NAME)
            fetchTask.pluginClasspath.setFrom(configuration)
        }

        project.tasks.withType(GenerateCustomTypesTask::class.java).configureEach { fetchTask ->
            val configuration = project.configurations.getAt(GENERATE_CUSTOM_TYPES_TASK_NAME)
            fetchTask.pluginClasspath.setFrom(configuration)
        }
    }

    private fun configureDefaultProjectSourceSet(project: Project, outputDirectory: DirectoryProperty, targetSourceSet: String = "main") {
        val sourceSetContainer = project.findProperty("sourceSets") as? SourceSetContainer
        sourceSetContainer?.findByName(targetSourceSet)?.java?.srcDir(outputDirectory)
    }
}