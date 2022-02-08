package de.akii.commercetools.api.customtypes.plugin.gradle

import org.gradle.api.Project

fun Project.commercetoolsCustomTypes(configure: CustomTypesGeneratorExtension.() -> Unit) =
    extensions.configure(CustomTypesGeneratorExtension::class.java, configure)
