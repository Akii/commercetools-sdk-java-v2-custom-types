package de.akii.commercetools.api.customtypes.plugin.gradle

import org.gradle.api.Project

fun Project.commercetoolsCustomTypes(configure: CustomTypesGeneratorPluginExtension.() -> Unit) =
    extensions.configure(CustomTypesGeneratorPluginExtension::class.java, configure)