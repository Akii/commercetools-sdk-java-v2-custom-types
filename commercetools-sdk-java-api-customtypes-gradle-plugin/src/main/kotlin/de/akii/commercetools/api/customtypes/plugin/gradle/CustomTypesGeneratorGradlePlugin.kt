package de.akii.commercetools.api.customtypes.plugin.gradle

import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.*
import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.FETCH_PRODUCT_TYPES_TASK_NAME
import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.GENERATE_CUSTOM_PRODUCT_TYPES_TASK_NAME
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
            configuration.description = "Configuration for fetching commercetools product types"

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

        project.configurations.create(GENERATE_CUSTOM_PRODUCT_TYPES_TASK_NAME) { configuration ->
            configuration.isVisible = true
            configuration.isTransitive = true
            configuration.description = "Configuration for generating commercetools custom types"

            configuration.dependencies.add(project.dependencies.create("de.akii.commercetools:commercetools-sdk-java-api-customtypes-generator:$DEFAULT_PLUGIN_VERSION"))
        }
    }

    private fun registerTasks(project: Project) {
        project.tasks.register(FETCH_PRODUCT_TYPES_TASK_NAME, FetchProductTypesTask::class.java)
        project.tasks.register(FETCH_TYPES_TASK_NAME, FetchTypesTask::class.java)
        project.tasks.register(GENERATE_CUSTOM_PRODUCT_TYPES_TASK_NAME, GenerateCustomProductTypesTask::class.java)
    }

    private fun processExtensionConfiguration(project: Project, extension: CustomTypesGeneratorPluginExtension) {
        configureCustomProductTypesGeneration(project, extension)
    }

    private fun configureCustomProductTypesGeneration(project: Project, extension: CustomTypesGeneratorPluginExtension) {
        val productTypesGeneratorExtension = extension.productTypesGeneratorExtension
        val typesGeneratorExtension = extension.typesGeneratorExtension

        val generateCustomTypesTask = project.tasks.named(GENERATE_CUSTOM_PRODUCT_TYPES_TASK_NAME, GenerateCustomProductTypesTask::class.java).get()
        generateCustomTypesTask.packageName.convention(project.provider { extension.packageName })
        generateCustomTypesTask.productTypeToSubPackageName.convention(project.provider { productTypesGeneratorExtension.productTypeToSubPackageName })
        generateCustomTypesTask.productTypeToClassName.convention(project.provider { productTypesGeneratorExtension.productTypeToClassName })
        generateCustomTypesTask.productTypeAttributeToPropertyName.convention(project.provider { productTypesGeneratorExtension.productTypeAttributeToPropertyName })
        generateCustomTypesTask.fieldDefinitionToPropertyName.convention(project.provider { typesGeneratorExtension.fieldDefinitionToPropertyName })
        generateCustomTypesTask.productTypesFile.set(productTypesGeneratorExtension.productTypesFile)
        configureDefaultProjectSourceSet(project, generateCustomTypesTask.outputDirectory)

        if (extension.credentialsConfigured()) {
            val credentials = extension.credentialsExtension

            val fetchProductTypesTask =
                project.tasks.named(FETCH_PRODUCT_TYPES_TASK_NAME, FetchProductTypesTask::class.java).get()
            fetchProductTypesTask.clientId.convention(project.provider { credentials.clientId })
            fetchProductTypesTask.clientSecret.convention(project.provider { credentials.clientSecret })
            fetchProductTypesTask.serviceRegion.convention(project.provider { credentials.serviceRegion.toString() })
            fetchProductTypesTask.projectName.convention(project.provider { credentials.projectName })

            val fetchTypesTask =
                project.tasks.named(FETCH_TYPES_TASK_NAME, FetchTypesTask::class.java).get()
            fetchTypesTask.clientId.convention(project.provider { credentials.clientId })
            fetchTypesTask.clientSecret.convention(project.provider { credentials.clientSecret })
            fetchTypesTask.serviceRegion.convention(project.provider { credentials.serviceRegion.toString() })
            fetchTypesTask.projectName.convention(project.provider { credentials.projectName })

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

        project.tasks.withType(GenerateCustomProductTypesTask::class.java).configureEach { fetchTask ->
            val configuration = project.configurations.getAt(GENERATE_CUSTOM_PRODUCT_TYPES_TASK_NAME)
            fetchTask.pluginClasspath.setFrom(configuration)
        }
    }

    private fun configureDefaultProjectSourceSet(project: Project, outputDirectory: DirectoryProperty, targetSourceSet: String = "main") {
        val sourceSetContainer = project.findProperty("sourceSets") as? SourceSetContainer
        sourceSetContainer?.findByName(targetSourceSet)?.java?.srcDir(outputDirectory)
    }
}