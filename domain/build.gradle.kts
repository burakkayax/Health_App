plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    testImplementation(libs.junit)
}

val sourceSets = extensions.getByType<SourceSetContainer>()
val asciiTestRuntimeDir = File(
    System.getProperty("user.home"),
    ".gradle/saglik-domain-test-runtime",
)
val syncDomainTestRuntime = tasks.register<Sync>("syncDomainTestRuntime") {
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
    dependsOn(syncDomainTestRuntime)
    workingDir = asciiTestRuntimeDir
    testClassesDirs = files(File(asciiTestRuntimeDir, "test"))
    classpath = files(
        File(asciiTestRuntimeDir, "test"),
        File(asciiTestRuntimeDir, "main"),
    ) + fileTree(File(asciiTestRuntimeDir, "libs")) {
        include("*.jar")
    }
}
