@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.lib)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.dokka)
}


repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(11)) } // TODO
}


kotlin {
    explicitApi()

    androidTarget()
    jvm()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    wasmJs()

    sourceSets.all {
        val suffixIndex = name.indexOfLast { it.isUpperCase() }
        val targetName = name.substring(0, suffixIndex)
        val suffix = name.substring(suffixIndex).lowercase().takeIf { it != "main" }
        kotlin.srcDir("$targetName/${suffix ?: "src"}")
        resources.srcDir("$targetName/${suffix?.let { it + "Resources" } ?: "resources"}")

    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.io)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmMain by getting {
        }
        val jvmAndAndroidMain by creating {
            dependsOn(jvmMain)
        }
        val jvm22Main by creating {
            dependsOn(jvmMain)
        }
        val iosMain by creating {
            dependsOn(nativeMain.get())
            dependencies {
                implementation(libs.ktor.core)
                implementation(libs.ktor.darwin)
                implementation(libs.coroutines.core)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.core.wasm)
                implementation(libs.coroutines.core.wasm)
            }
        }
    }
}

android {
    namespace = "io.github.devcrocod.kotok"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

//val mainJavaToolchainVersion: String by project
//val modularJavaToolchainVersion: String by project
//
//tasks {
//    val compileJavaModuleInfo by registering(JavaCompile::class) {
//        val moduleName = "kotok"
//        val compileKotlinJvm by getting(KotlinCompile::class)
//        val sourceDir = file("jvm/java9/")
//        val targetDir = compileKotlinJvm.destinationDirectory.map { it.dir("../java9/") }
//
//        // Use a Java 11 compiler for the module info.
//        javaCompiler.set(project.javaToolchains.compilerFor {
//            languageVersion.set(JavaLanguageVersion.of(modularJavaToolchainVersion))
//        })
//
//        // Always compile kotlin classes before the module descriptor.
//        dependsOn(compileKotlinJvm)
//
//        // Add the module-info source file.
//        source(sourceDir)
//
//        // Also add the module-info.java source file to the Kotlin compile task.
//        // The Kotlin compiler will parse and check module dependencies,
//        // but it currently won't compile to a module-info.class file.
//        // Note that module checking only works on JDK 9+,
//        // because the JDK built-in base modules are not available in earlier versions
//        val javaVersion = compileKotlinJvm.kotlinJavaToolchain.javaVersion.getOrNull()
//        if (javaVersion?.isJava9Compatible == true) {
//            logger.info("Module-info checking is enabled; $compileKotlinJvm is compiled using Java $javaVersion")
//            compileKotlinJvm.source(sourceDir)
//        } else {
//            logger.info("Module-info checking is disabled")
//        }
//        
//        // Set the task outputs and destination dir
//        outputs.dir(targetDir)
//        destinationDirectory.set(targetDir)
//        
//        // Configure JVM compatibility
//        sourceCompatibility = JavaVersion.VERSION_1_9.toString()
//        targetCompatibility = JavaVersion.VERSION_1_9.toString()
//
//        // Set the Java release version.
//        options.release.set(9)
//
//        // Ignore warnings about using 'requires transitive' on automatic modules.
//        // not needed when compiling with recent JDKs, e.g. 17
//        options.compilerArgs.add("-Xlint:-requires-transitive-automatic")
//
//        // Patch the compileKotlinJvm output classes into the compilation so exporting packages works correctly.
//        options.compilerArgs.addAll(listOf("--patch-module", "$moduleName=${compileKotlinJvm.destinationDirectory.get()}"))
//
//        // Use the classpath of the compileKotlinJvm task.
//        // Also ensure that the module path is used instead of classpath.
//        classpath = compileKotlinJvm.libraries
//        modularity.inferModulePath.set(true)
//    }
//
//    val jvmJar by existing(Jar::class) {
//        manifest {
//            attributes("Multi-Release" to true)
//        }
//        from(compileJavaModuleInfo.map { it.destinationDirectory }) {
//            into("META-INF/versions/9/")
//        }
//    }
//}
