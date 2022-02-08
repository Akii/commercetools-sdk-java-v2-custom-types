package de.akii.commercetools.api.customtypes.plugin.gradle.tasks

import de.akii.commercetools.api.customtypes.plugin.gradle.actions.GenerateCustomTypesAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import org.gradle.workers.ClassLoaderWorkerSpec
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

internal const val GENERATE_CUSTOM_TYPES_TASK_NAME: String = "generateCustomTypes"

abstract class GenerateCustomTypesTask : DefaultTask() {

    @get:Classpath
    val pluginClasspath: ConfigurableFileCollection = project.objects.fileCollection()

    @InputFile
    val productTypesFile: RegularFileProperty = project.objects.fileProperty()

    @Input
    @Option(option = "packageName", description = "target package name to use for generated classes")
    val packageName: Property<String> = project.objects.property(String::class.java)

    @OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty()

    @Inject
    abstract fun getWorkerExecutor(): WorkerExecutor

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

        val workQueue: WorkQueue = getWorkerExecutor().classLoaderIsolation { workerSpec: ClassLoaderWorkerSpec ->
            workerSpec.classpath.from(pluginClasspath)
        }

        workQueue.submit(GenerateCustomTypesAction::class.java) { parameters ->
            parameters.productTypesFile.set(productTypesFile.get().asFile)
            parameters.packageName.set(packageName.get())
            parameters.targetDirectory.set(targetDirectory)
        }

        workQueue.await()
    }
}
