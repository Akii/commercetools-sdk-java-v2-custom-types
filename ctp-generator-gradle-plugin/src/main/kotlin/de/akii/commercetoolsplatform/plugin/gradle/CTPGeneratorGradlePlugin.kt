package de.akii.commercetoolsplatform.plugin.gradle

import de.akii.commercetoolsplatform.plugin.gradle.tasks.FETCH_PRODUCT_TYPES_TASK_NAME
import de.akii.commercetoolsplatform.plugin.gradle.tasks.FetchProductTypesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.SourceSetContainer

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

            configuration.dependencies.add(project.dependencies.create("commercetools-okhttp-client4:7.6.0"))
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
            fetchProductTypesTask.outputFile.convention(fetchProductTypesTask.outputFile)
        }
    }

    private fun configureTaskClasspaths(project: Project) {
        project.tasks.withType(FetchProductTypesTask::class.java).configureEach { fetchTask ->
            val configuration = project.configurations.getAt(FETCH_PRODUCT_TYPES_TASK_NAME)
            fetchTask.pluginClasspath.setFrom(configuration)
        }
    }

    private fun configureDefaultProjectSourceSet(project: Project, outputDirectory: DirectoryProperty, targetSourceSet: String = "main") {
        val sourceSetContainer = project.findProperty("sourceSets") as? SourceSetContainer
        sourceSetContainer?.findByName(targetSourceSet)?.java?.srcDir(outputDirectory)
    }
}
