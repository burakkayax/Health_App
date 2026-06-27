plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.saglik.feature.summary"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":domain"))
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.datetime)
}

val asciiUnitTestRuntimeRootDir = File(
    System.getProperty("user.home"),
    ".gradle/saglik-feature-summary-unit-test-runtime",
)

tasks.withType<Test>().configureEach {
    val unitTestMatch = Regex("""test(.+)UnitTest""").matchEntire(name) ?: return@configureEach
    val variantTaskName = unitTestMatch.groupValues[1]
    val variantName = variantTaskName.substring(0, 1).lowercase() + variantTaskName.substring(1)
    val unitTestVariantName = "${variantName}UnitTest"

    doFirst {
        val resolvedClasspath = classpath
        val runtimeDir = File(asciiUnitTestRuntimeRootDir, name)
        val runtimeClassesDir = File(runtimeDir, "runtime-classes")
        val runtimeJarsDir = File(runtimeDir, "runtime-jars")
        delete(runtimeDir)
        copy {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            into(runtimeDir)
            into("main") {
                from(layout.buildDirectory.dir("tmp/kotlin-classes/$variantName"))
                from(layout.buildDirectory.dir("intermediates/javac/$variantName/compile${variantTaskName}JavaWithJavac/classes"))
            }
            into("test") {
                from(layout.buildDirectory.dir("tmp/kotlin-classes/$unitTestVariantName"))
                from(layout.buildDirectory.dir("intermediates/javac/$unitTestVariantName/compile${variantTaskName}UnitTestJavaWithJavac/classes"))
            }
        }
        resolvedClasspath.files.forEachIndexed { index, file ->
            if (file.isDirectory) {
                copy {
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    from(file)
                    into(runtimeClassesDir)
                }
            } else if (file.extension.equals("jar", ignoreCase = true)) {
                copy {
                    from(file)
                    into(runtimeJarsDir)
                    rename { originalName -> "$index-$originalName" }
                }
            }
        }
        workingDir = runtimeDir
        testClassesDirs = files(File(runtimeDir, "test"))
        classpath = files(
            File(runtimeDir, "test"),
            File(runtimeDir, "main"),
            runtimeClassesDir,
        ) + fileTree(runtimeJarsDir) {
            include("*.jar")
        }
    }
}
