package de.akii.commercetools.api.customtypes.plugin.gradle

import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.FETCH_PRODUCT_TYPES_TASK_NAME
import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.FetchProductTypesTask
import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.GENERATE_CUSTOM_TYPES_TASK_NAME
import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.GenerateCustomTypesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.SourceSetContainer

private const val PLUGIN_EXTENSION_NAME = "commercetoolsCustomTypesGenerator"

class CustomTypesGeneratorGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        configurePluginDependencies(project)
        registerTasks(project)

        val extension = project.extensions.create(PLUGIN_EXTENSION_NAME, CustomTypesGeneratorExtension::class.java)
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

        project.configurations.create(GENERATE_CUSTOM_TYPES_TASK_NAME) { configuration ->
            configuration.isVisible = true
            configuration.isTransitive = true
            configuration.description = "Configuration for generating commercetools custom types"

            configuration.dependencies.add(project.dependencies.create("de.akii.commercetools:commercetools-sdk-java-api-customtypes-generator:$DEFAULT_PLUGIN_VERSION"))
        }
    }

    private fun registerTasks(project: Project) {
        project.tasks.register(FETCH_PRODUCT_TYPES_TASK_NAME, FetchProductTypesTask::class.java)
        project.tasks.register(GENERATE_CUSTOM_TYPES_TASK_NAME, GenerateCustomTypesTask::class.java)
    }

    private fun processExtensionConfiguration(project: Project, extension: CustomTypesGeneratorExtension) {
        val generateCustomTypesTask =
            project.tasks.named(GENERATE_CUSTOM_TYPES_TASK_NAME, GenerateCustomTypesTask::class.java).get()
        generateCustomTypesTask.packageName.convention(project.provider { extension.packageName })
        generateCustomTypesTask.productTypeNameToSubPackageName.convention(project.provider { extension.productTypeNameToSubPackageName })
        generateCustomTypesTask.productTypeNameToClassNamePrefix.convention(project.provider { extension.productTypeNameToClassNamePrefix })
        generateCustomTypesTask.attributeNameToPropertyName.convention(project.provider { extension.attributeNameToPropertyName })
        generateCustomTypesTask.productTypesFile.set(extension.productTypesFile)
        configureDefaultProjectSourceSet(project, generateCustomTypesTask.outputDirectory)

        if (extension.clientId != null) {
            val fetchProductTypesTask =
                project.tasks.named(FETCH_PRODUCT_TYPES_TASK_NAME, FetchProductTypesTask::class.java).get()
            fetchProductTypesTask.clientId.convention(project.provider { extension.clientId })
            fetchProductTypesTask.clientSecret.convention(project.provider { extension.clientSecret })
            fetchProductTypesTask.serviceRegion.convention(project.provider { extension.serviceRegion })
            fetchProductTypesTask.projectName.convention(project.provider { extension.projectName })

            generateCustomTypesTask.dependsOn(fetchProductTypesTask)
            generateCustomTypesTask.productTypesFile.set(fetchProductTypesTask.outputFile)
        }
    }

    private fun configureTaskClasspaths(project: Project) {
        project.tasks.withType(FetchProductTypesTask::class.java).configureEach { fetchTask ->
            val configuration = project.configurations.getAt(FETCH_PRODUCT_TYPES_TASK_NAME)
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
