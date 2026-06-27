plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.saglik.feature.profile"
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
    implementation(project(":core:healthconnect"))
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
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.datetime)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
}

val asciiDebugUnitTestRuntimeDir = File(
    System.getProperty("user.home"),
    ".gradle/saglik-feature-profile-debug-unit-test-runtime",
)

tasks.withType<Test>().configureEach {
    if (name == "testDebugUnitTest") {
        doFirst {
            val resolvedClasspath = classpath
            val runtimeClassesDir = File(asciiDebugUnitTestRuntimeDir, "runtime-classes")
            val runtimeJarsDir = File(asciiDebugUnitTestRuntimeDir, "runtime-jars")
            delete(asciiDebugUnitTestRuntimeDir)
            copy {
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                into(asciiDebugUnitTestRuntimeDir)
                into("main") {
                    from(layout.buildDirectory.dir("tmp/kotlin-classes/debug"))
                    from(layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes"))
                }
                into("test") {
                    from(layout.buildDirectory.dir("tmp/kotlin-classes/debugUnitTest"))
                    from(layout.buildDirectory.dir("intermediates/javac/debugUnitTest/compileDebugUnitTestJavaWithJavac/classes"))
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
            workingDir = asciiDebugUnitTestRuntimeDir
            testClassesDirs = files(File(asciiDebugUnitTestRuntimeDir, "test"))
            classpath = files(
                File(asciiDebugUnitTestRuntimeDir, "test"),
                File(asciiDebugUnitTestRuntimeDir, "main"),
                runtimeClassesDir,
            ) + fileTree(runtimeJarsDir) {
                include("*.jar")
            }
        }
    }
}
