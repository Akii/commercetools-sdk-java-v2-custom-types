import java.time.Instant

plugins {
    kotlin("jvm")
    `maven-publish`
}

allprojects {
    buildscript {
        repositories {
            mavenCentral()
            mavenLocal()
        }
    }

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    val kotlinVersion: String by project
    val kotlinJvmVersion: String by project

    val currentProject = this

    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = kotlinJvmVersion
                freeCompilerArgs = listOf("-Xjsr305=strict")
            }
        }

        jar {
            manifest {
                attributes["Built-By"] = "Akii"
                attributes["Build-Jdk"] = "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})"
                attributes["Build-Timestamp"] = Instant.now().toString()
                attributes["Created-By"] = "Gradle ${gradle.gradleVersion}"
                attributes["Implementation-Title"] = currentProject.name
                attributes["Implementation-Version"] = project.version
            }

            finalizedBy("publishToMavenLocal")
        }

        java {
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        val jarComponent = currentProject.components.getByName("java")
        val sourcesJar by registering(Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allSource)
        }

        publishing {
            publications {
                withType<MavenPublication> {
                    pom {
                        name.set("${currentProject.group}:${currentProject.name}")
                        url.set("https://github.com/Akii/commercetools-sdk-java-v2-custom-types")
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        developers {
                            developer {
                                name.set("Akii")
                                email.set("oss@akii.de")
                            }
                        }
                        scm {
                            connection.set("scm:git:git://github.com:Akii/commercetools-sdk-java-v2-custom-types.git")
                            developerConnection.set("scm:git:git://github.com:Akii/commercetools-sdk-java-v2-custom-types.git")
                            url.set("https://github.com/Akii/commercetools-sdk-java-v2-custom-types")
                        }
                    }
                }
                create<MavenPublication>("mavenJava") {
                    from(jarComponent)
                    artifact(sourcesJar.get())
                }
            }
        }
    }

    dependencies {
        implementation(kotlin("stdlib", kotlinVersion))
        testImplementation(kotlin("test", kotlinVersion))
    }

    tasks.test {
        useJUnitPlatform()
    }
}