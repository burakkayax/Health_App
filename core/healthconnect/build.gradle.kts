plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.saglik.core.healthconnect"
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
    implementation(project(":core:model"))
    implementation(libs.androidx.health.connect.client)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}

val asciiUnitTestRuntimeRootDir = File(
    System.getProperty("user.home"),
    ".gradle/saglik-core-healthconnect-unit-test-runtime",
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
