package de.akii.commercetoolsplatform.plugin.gradle

import org.gradle.api.Project

fun Project.commercetools(configure: CTPGeneratorExtension.() -> Unit) =
    extensions.configure(CTPGeneratorExtension::class.java, configure)
