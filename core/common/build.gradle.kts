plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.datetime)
    testImplementation(libs.junit)
}

val sourceSets = extensions.getByType<SourceSetContainer>()
val asciiTestRuntimeDir = File(
    System.getProperty("user.home"),
    ".gradle/saglik-core-common-test-runtime",
)
val syncCoreCommonTestRuntime = tasks.register<Sync>("syncCoreCommonTestRuntime") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    into(asciiTestRuntimeDir)
    into("main") {
        from(sourceSets.named("main").map { it.output })
    }
    into("test") {
        from(sourceSets.named("test").map { it.output })
    }
    into("libs") {
        from(configurations.named("testRuntimeClasspath"))
        include("*.jar")
    }
}

tasks.withType<Test>().configureEach {
    dependsOn(syncCoreCommonTestRuntime)
    workingDir = asciiTestRuntimeDir
    testClassesDirs = files(File(asciiTestRuntimeDir, "test"))
    classpath = files(
        File(asciiTestRuntimeDir, "test"),
        File(asciiTestRuntimeDir, "main"),
    ) + fileTree(File(asciiTestRuntimeDir, "libs")) {
        include("*.jar")
    }
}
