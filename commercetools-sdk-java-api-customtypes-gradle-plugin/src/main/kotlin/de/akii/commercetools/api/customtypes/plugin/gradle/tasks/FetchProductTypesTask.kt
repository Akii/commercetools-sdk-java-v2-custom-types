package de.akii.commercetools.api.customtypes.plugin.gradle.tasks

import de.akii.commercetools.api.customtypes.plugin.gradle.actions.FetchProductTypesAction
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import org.gradle.workers.ClassLoaderWorkerSpec
import org.gradle.workers.WorkQueue
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

internal const val FETCH_PRODUCT_TYPES_TASK_NAME: String = "fetchProductTypes"

abstract class FetchProductTypesTask : DefaultTask() {

    @get:Classpath
    val pluginClasspath: ConfigurableFileCollection = project.objects.fileCollection()

    @Input
    @Option(option = "clientId", description = "Client Id")
    val clientId: Property<String> = project.objects.property(String::class.java)

    @Input
    @Option(option = "clientSecret", description = "Client Secret")
    val clientSecret: Property<String> = project.objects.property(String::class.java)

    @Input
    @Option(option = "serviceRegion", description = "Service Region")
    val serviceRegion: Property<String> = project.objects.property(String::class.java)

    @Input
    @Option(option = "projectName", description = "Project Name")
    val projectName: Property<String> = project.objects.property(String::class.java)

    @OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()

    @Inject
    abstract fun getWorkerExecutor(): WorkerExecutor

    init {
        group = "commercetools"
        description = "Fetch product types from commercetools."

        outputFile.convention(project.layout.buildDirectory.file("productTypes.json"))
    }

    @TaskAction
    fun fetchProductTypesAction() {
        val productTypesFile = outputFile.asFile.get()

        val targetDirectory = productTypesFile.parentFile
        if (!targetDirectory.isDirectory && !targetDirectory.mkdirs()) {
            throw RuntimeException("Failed to create target product types directory $targetDirectory")
        }

        val workQueue: WorkQueue = getWorkerExecutor().classLoaderIsolation { workerSpec: ClassLoaderWorkerSpec ->
            workerSpec.classpath.from(pluginClasspath)
        }

        workQueue.submit(FetchProductTypesAction::class.java) { parameters ->
            parameters.clientId.set(clientId)
            parameters.clientSecret.set(clientSecret)
            parameters.serviceRegion.set(serviceRegion)
            parameters.projectName.set(projectName)
            parameters.productTypesFile.set(productTypesFile)
        }
        workQueue.await()
    }
}
