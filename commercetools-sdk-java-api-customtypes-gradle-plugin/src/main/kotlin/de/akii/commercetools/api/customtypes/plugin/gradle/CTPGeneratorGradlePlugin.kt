package de.akii.commercetools.api.customtypes.plugin.gradle

import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.FETCH_PRODUCT_TYPES_TASK_NAME
import de.akii.commercetools.api.customtypes.plugin.gradle.tasks.FetchProductTypesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

private const val PLUGIN_EXTENSION_NAME = "ctpGenerator"

class CTPGeneratorGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        configurePluginDependencies(project)
        registerTasks(project)

        val extension = project.extensions.create(PLUGIN_EXTENSION_NAME, CTPGeneratorExtension::class.java)
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
    }

    private fun registerTasks(project: Project) {
        project.tasks.register(FETCH_PRODUCT_TYPES_TASK_NAME, FetchProductTypesTask::class.java)
    }

    private fun processExtensionConfiguration(project: Project, extension: CTPGeneratorExtension) {
        if (extension.clientId != null) {
            val fetchProductTypesTask = project.tasks.named(FETCH_PRODUCT_TYPES_TASK_NAME, FetchProductTypesTask::class.java).get()
            fetchProductTypesTask.clientId.convention(project.provider { extension.clientId })
            fetchProductTypesTask.clientSecret.convention(project.provider { extension.clientSecret })
            fetchProductTypesTask.serviceRegion.convention(project.provider { extension.serviceRegion })
            fetchProductTypesTask.projectName.convention(project.provider { extension.projectName })
        }
    }

    private fun configureTaskClasspaths(project: Project) {
        project.tasks.withType(FetchProductTypesTask::class.java).configureEach { fetchTask ->
            val configuration = project.configurations.getAt(FETCH_PRODUCT_TYPES_TASK_NAME)
            fetchTask.pluginClasspath.setFrom(configuration)
        }
    }
}
