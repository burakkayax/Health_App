plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.saglik.data.repository"
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
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:healthconnect"))
    implementation(project(":core:model"))
    implementation(project(":data:local"))
    implementation(project(":domain"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    testImplementation(libs.junit)
}

val asciiDebugUnitTestRuntimeDir = File(
    System.getProperty("user.home"),
    ".gradle/saglik-data-repository-debug-unit-test-runtime",
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
